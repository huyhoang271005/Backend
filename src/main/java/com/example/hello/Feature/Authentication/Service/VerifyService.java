package com.example.hello.Feature.Authentication.Service;

import com.example.hello.Infrastructure.Email.EmailVerifyService;
import com.example.hello.Entity.VerificationTokens;
import com.example.hello.Enum.VerificationTypes;
import com.example.hello.Feature.User.dto.Address;
import com.example.hello.Infrastructure.Security.AppProperties;
import com.example.hello.Mapper.SessionMapper;
import com.example.hello.Feature.Authentication.Repository.VerificationTokensRepository;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.Authentication.dto.DeviceResponse;
import com.example.hello.Feature.Authentication.dto.EmailRequest;
import com.example.hello.Feature.Authentication.dto.PasswordRequest;
import com.example.hello.Feature.User.Repository.DeviceRepository;
import com.example.hello.Feature.User.Repository.EmailRepository;
import com.example.hello.Feature.User.Repository.SessionRepository;
import com.example.hello.Entity.Device;
import com.example.hello.Entity.Session;
import com.example.hello.Enum.UserStatus;
import com.example.hello.SseEmitter.SseService;
import com.example.hello.SseEmitter.SseTopicName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerifyService {
    EmailVerifyService emailVerifyService;
    EmailRepository emailRepository;
    VerificationTokensRepository verificationTokensRepository;
    DeviceRepository deviceRepository;
    SessionRepository sessionRepository;
    PasswordEncoder passwordEncoder;
    SseService sseService;
    AppProperties appProperties;
    int timeExpired = 24;   // Hours
    private final SessionMapper sessionMapper;

    private void checkResendRateLimit(UUID typeId, VerificationTypes type) {
        //Tìm yêu cầu xác thực dựa trên typeId(có thể là deviceId, emailId...) và loại xác thực
        var verificationToken = verificationTokensRepository.findByVerificationTypeAndTypeIdOrderByCreatedAtDesc(type, typeId);
        if (!verificationToken.isEmpty()) {
            //Nếu còn hạn sử dụng
            log.info("Verification token not empty {}", verificationToken);
            if (verificationToken.getFirst().getExpiredAt().isAfter(Instant.now())) {
                //Nếu thời gian vẫn còn nằm trong thời hạn kể từ lần cuối yêu cầu xác thực
                var secondsSinceLast = Duration.between(verificationToken.getFirst().getCreatedAt(), Instant.now()).getSeconds();
                // Thời gian phải chờ để gửi yêu cầu xác thực lần tiếp theo (tính theo s)
                var timeWait = 1 * 60 * Math.pow(2, verificationTokensRepository.countByVerificationTypeAndTypeId(type, typeId));
                //Nếu thời gian gửi yêu cầu cuối bé hơn thời gian chờ đá exception
                if (secondsSinceLast < timeWait) {
                    throw new UnprocessableEntityException(StringApplication.FIELD.WAIT_AFTER +
                            (int) (timeWait - secondsSinceLast) + StringApplication.FIELD.SECONDS);
                }
            }
        }
        log.info("Verification token is empty");
    }

    @Transactional
    public Response<Void> sendVerifyEmail(EmailRequest emailRequest,
                                          Address address) {
        //Tìm email
        var userEmail = emailRepository.findByEmail(emailRequest.getEmail()).orElseThrow(() ->
                new EntityNotFoundException(StringApplication.FIELD.EMAIL +
                        StringApplication.FIELD.NOT_EXIST));
        //Kiểm tra tình trạng xác thực
        if (userEmail.getValidated()) {
            throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.VERIFIED);
        }
        //Check số lần gửi yêu cầu xác thực (có thể bắt chờ đợi nếu cần)
        checkResendRateLimit(userEmail.getEmailId(), VerificationTypes.VERIFY_EMAIL);
        //Sinh yêu cầu xác thực và lưu vào db
        var verificationToken = VerificationTokens.builder()
                .expiredAt(Instant.now().plus(timeExpired, ChronoUnit.HOURS))
                .verificationType(VerificationTypes.VERIFY_EMAIL)
                .typeId(userEmail.getEmailId())
                .user(userEmail.getUser())
                .build();
        verificationTokensRepository.save(verificationToken);
        log.info("Verification email generated");
        //Gửi email xác thực tới user dẫn tới trang xác thực
        emailVerifyService.sendEmail(emailRequest.getEmail(),
                StringApplication.FIELD.VERIFY + StringApplication.FIELD.EMAIL,
                userEmail.getUser().getProfile().getFullName(),
                StringApplication.FIELD.ADD_NEW_EMAIL, address,
                appProperties.getFrontendUrl() + "/auth/verified?verificationId=" + verificationToken.getVerificationTokenId(),
                timeExpired + StringApplication.FIELD.HOURS);
        log.info("Email verification email has been sent async");
        return new Response<>(true, StringApplication.SUCCESS.CHECK_EMAIL, null);
    }


    @Transactional
    public Response<DeviceResponse> sendVerifyDevice(EmailRequest emailRequest, UUID deviceId, String userAgent,
                                                     String deviceType, String deviceName, String ip, Address address) {
        //Kiểm tra email
        var userEmail = emailRepository.findByEmail(emailRequest.getEmail()).orElseThrow(() ->
                new EntityNotFoundException(StringApplication.FIELD.EMAIL +
                        StringApplication.FIELD.NOT_EXIST));
        //Kiểm tra email xác thực chưa
        if (!userEmail.getValidated()) {
            throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.UNVERIFIED);
        }
        //Tìm device trên db nếu không tồn tại sinh device mới và lưu vào db
        var device = Optional.ofNullable(deviceId).flatMap(deviceRepository::findById).orElseGet(() ->
                Device.builder().deviceName(deviceName).deviceType(deviceType).userAgent(userAgent).build());
        log.info("Device has been found or generated");
        deviceRepository.save(device);
        //Lấy dữ liệu user và session(nếu không có thì tạo mới session)
        var user = userEmail.getUser();
        var session = sessionRepository.findByUserAndDevice(user, device).orElseGet(() ->
                Session.builder()
                        .device(device)
                        .user(user)
                        .ipAddress(ip)
                        .revoked(true)
                        .validated(false)
                        .build());
        log.info("Session has been found or generated");
        sessionMapper.updateSession(address, session);
        log.info("Address session updated");
        //Nếu deviceId có trên máy (đã từng xác thực) thì check số lần gửi yêu cầu
        checkResendRateLimit(session.getSessionId(), VerificationTypes.VERIFY_DEVICE);
        if (session.getValidated()) {
            //Nếu session đã được xác thực đá exception
            log.info("Session has been validated");
            throw new ConflictException(StringApplication.FIELD.DEVICE + StringApplication.FIELD.VERIFIED);
        }
        sessionRepository.save(session);
        //Tạo yêu cầu xác thực và lưu vào db
        var verificationToken = VerificationTokens.builder()
                .expiredAt(Instant.now().plus(timeExpired, ChronoUnit.HOURS))
                .verificationType(VerificationTypes.VERIFY_DEVICE)
                .typeId(session.getSessionId())
                .user(user)
                .build();
        log.info("Verification device generated");
        verificationTokensRepository.save(verificationToken);
        //Gửi email xác thực tới user
        emailVerifyService.sendEmail(emailRequest.getEmail(),
                StringApplication.FIELD.VERIFY + StringApplication.FIELD.DEVICE,
                userEmail.getUser().getProfile().getFullName(),
                StringApplication.FIELD.LOGIN_NEW_DEVICE, address,
                appProperties.getFrontendUrl() + "/auth/verified?verificationId=" + verificationToken.getVerificationTokenId(),
                timeExpired + StringApplication.FIELD.HOURS);
        log.info("Device verification email has been sent async");
        return new Response<>(true,
                StringApplication.SUCCESS.CHECK_EMAIL,
                new DeviceResponse(session.getDevice().getDeviceId(), session.getSessionId()));
    }

    @Transactional
    public Response<Void> verify(String token) {
        //Kiểm tra yêu cầu có hợp lệ không
        var tokenVerify = verificationTokensRepository.findByVerificationTokenId(UUID.fromString(token))
                .orElseThrow(() -> new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID));
        //Kiểm tra thời hạn yêu cầu
        if (Instant.now().isAfter(tokenVerify.getExpiredAt())) {
            log.info("Token has expired {}", tokenVerify.getExpiredAt());
            throw new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.EXPIRED);
        }
        //Nếu là xác thực email
        if (tokenVerify.getVerificationType() == VerificationTypes.VERIFY_EMAIL) {
            log.info("Verify email");
            //Tìm email
            var userEmail = emailRepository.findById(tokenVerify.getTypeId()).orElseThrow(() ->
                    new EntityNotFoundException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.NOT_EXIST));
            //Kiểm tra xác thực email đá exception nếu xác thực rồi
            if (userEmail.getValidated()) {
                throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.VERIFIED);
            }
            //Xác thực thành công
            userEmail.setValidated(true);
            var user = userEmail.getUser();
            //Nếu trạng thái người dùng là đang xác thực thì set sang hoạt động
            if (user.getUserStatus() == UserStatus.PENDING) {
                user.setUserStatus(UserStatus.ACTIVE);
                log.info("User has been activated {}", user.getUserId());
            }
            //Xoá yêu cầu xác thực
            verificationTokensRepository.deleteByUser_UserIdAndTypeId(
                    tokenVerify.getUser().getUserId(), tokenVerify.getTypeId());
            log.info("Verification email deleted");
            return new Response<>(true, StringApplication.FIELD.VERIFIED_SUCCESS, null);
        }
        //Nếu là xác thực session
        else if (tokenVerify.getVerificationType() == VerificationTypes.VERIFY_DEVICE) {
            log.info("Verify device");
            //Tìm session
            var session = sessionRepository.findById(tokenVerify.getTypeId()).orElseThrow(
                    () -> new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN +
                            StringApplication.FIELD.INVALID));
            //Set xác thực thành công
            session.setValidated(true);
            log.info("Set validated is true");
            //Xoá yêu cầu xác thực
            verificationTokensRepository.deleteByUser_UserIdAndTypeId(
                    tokenVerify.getUser().getUserId(), tokenVerify.getTypeId());
            log.info("Verification device deleted");
            sseService.sendSse(SseTopicName.verified.name(), true, List.of(session.getSessionId()));
            return new Response<>(true, StringApplication.FIELD.VERIFIED_SUCCESS, null);
        }
        throw new UnprocessableEntityException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID);
    }

    @Transactional
    public Response<Void> sendVerifyChangePassword(EmailRequest emailRequest, Address address) {
        //Tìm email
        var userEmail = emailRepository.findByEmail(emailRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.EMAIL +
                        StringApplication.FIELD.NOT_EXIST));
        //Kiểm tra email đã xác thực chưa
        if (!userEmail.getValidated()) {
            throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.UNVERIFIED);
        }
        //Lấy dữ liệu user
        var user = userEmail.getUser();
        //Kiểm tra các yêu cầu xác thực
        checkResendRateLimit(user.getUserId(), VerificationTypes.VERIFY_CHANGE_PASSWORD);
        //Thời gian hết hạn yêu cầu xác thực (phút)
        int minExpired = 15;
        //Sinh yêu cầu xác thực
        var verifyToken = VerificationTokens.builder()
                .user(user).verificationType(VerificationTypes.VERIFY_CHANGE_PASSWORD)
                .expiredAt(Instant.now().plus(minExpired, ChronoUnit.MINUTES))
                .typeId(user.getUserId())
                .build();
        verificationTokensRepository.save(verifyToken);
        log.info("Verification change password generated");
        //Gửi email dẫn tới trang thay đổi mật khẩu
        emailVerifyService.sendEmail(userEmail.getEmail(),
                StringApplication.FIELD.VERIFY + StringApplication.FIELD.CHANGE_PASSWORD,
                user.getProfile().getFullName(),
                StringApplication.FIELD.CHANGE_PASSWORD, address,
                appProperties.getFrontendUrl() + "/auth/change-password?token=" + verifyToken.getVerificationTokenId(),
                minExpired + StringApplication.FIELD.MINUTES);
        log.info("Verification change password has been sent async");
        return new Response<>(true, StringApplication.FIELD.SUCCESS, null);
    }

    @Transactional
    public Response<Void> changePassword(PasswordRequest passwordRequest, String token) {
        // Kiểm tra yêu cầu tồn tại không
        var tokenVerify = verificationTokensRepository.findByVerificationTokenId(UUID.fromString(token))
                .orElseThrow(() -> new ConflictException(StringApplication.FIELD.REQUEST +
                        StringApplication.FIELD.INVALID));
        //Kiểm tra thời gian hết hạn
        if (Instant.now().isAfter(tokenVerify.getExpiredAt())) {
            log.info("Token has expired");
            throw new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.EXPIRED);
        }
        //Set password mới kèm bcrypt
        var user = tokenVerify.getUser();
        user.setPassword(passwordEncoder.encode(passwordRequest.getPassword()));
        log.info("Password was change");
        //Xoá yêu cầu xác thực
        verificationTokensRepository.deleteByUser_UserIdAndTypeId(user.getUserId(),
                tokenVerify.getTypeId());
        log.info("Verification change password deleted");
        return new Response<>(true, StringApplication.FIELD.CHANGE_PASSWORD + StringApplication.FIELD.SUCCESS, null);
    }
}
