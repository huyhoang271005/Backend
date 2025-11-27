package com.example.hello.Users.Authentication.Service;

import com.example.hello.Infrastructure.Email.EmailVerifyService;
import com.example.hello.Users.Authentication.Entity.VerificationTokens;
import com.example.hello.Users.Authentication.Enum.VerificationTypes;
import com.example.hello.Infrastructure.Email.VerificationTokensRepository;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Infrastructure.Security.CorsConfig;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Users.Authentication.DTO.DeviceResponse;
import com.example.hello.Users.Authentication.DTO.EmailRequest;
import com.example.hello.Users.Authentication.DTO.PasswordRequest;
import com.example.hello.Users.Authentication.Repository.DeviceRepository;
import com.example.hello.Users.Authentication.Repository.EmailRepository;
import com.example.hello.Users.Authentication.Repository.SessionRepository;
import com.example.hello.Users.Authentication.Entity.Device;
import com.example.hello.Users.Authentication.Entity.Session;
import com.example.hello.Users.User.Enum.UserStatus;
import com.example.hello.Users.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerifyService {
    EmailVerifyService  emailVerifyService;
    EmailRepository emailRepository;
    VerificationTokensRepository  verificationTokensRepository;
    DeviceRepository deviceRepository;
    SessionRepository sessionRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    int timeExpired = 24;   // Hours

    private void checkResendRateLimit(UUID typeId, VerificationTypes type){
        //Tìm yêu cầu xác thực dựa trên typeId(có thể là deviceId, emailId...) và loại xác thực
        var verificationToken = verificationTokensRepository.findByVerificationTypeAndTypeId(type, typeId);
        if(!verificationToken.isEmpty()){
            //Nếu còn hạn sử dụng
            if(verificationToken.getLast().getExpiredAt().isAfter(LocalDateTime.now())) {
                //Nếu thời gian vẫn còn nằm trong thời hạn kể từ lần cuối yêu cầu xác thực
                var secondsSinceLast = Duration.between(verificationToken.getLast().getCreatedAt(), LocalDateTime.now()).getSeconds();
                // Thời gian phải chờ để gửi yêu cầu xác thực lần tiếp theo (tính theo s)
                var timeWait = 1*60*Math.pow(2, verificationTokensRepository.countByVerificationTypeAndTypeId(type, typeId));
                //Nếu thời gian gửi yêu cầu cuối bé hơn thời gian chờ đá exception
                if(secondsSinceLast < timeWait){
                    throw new UnprocessableEntityException(StringApplication.FIELD.WAIT_AFTER +
                            (int)(timeWait - secondsSinceLast) + StringApplication.FIELD.SECONDS);
                }
            }
        }
    }
    @Transactional
    public Response<Void> sendVerifyEmail(EmailRequest emailRequest) {
        //Tìm email
        var userEmail = emailRepository.findByEmail(emailRequest.getEmail()).orElseThrow(() ->
                new EntityNotFoundException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.NOT_EXIST)
        );
        //Kiểm tra tình trạng xác thực
        if(userEmail.getValidated()){
            throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.VERIFIED);
        }
        //Check số lần gửi yêu cầu xác thực (có thể bắt chờ đợi nếu cần)
        checkResendRateLimit(userEmail.getEmailId(), VerificationTypes.VERIFY_EMAIL);
        //Sinh yêu cầu xác thực và lưu vào db
        var verificationToken = VerificationTokens.builder()
                .expiredAt(LocalDateTime.now().plusHours(timeExpired))
                .verificationType(VerificationTypes.VERIFY_EMAIL)
                .typeId(userEmail.getEmailId())
                .user(userEmail.getUser())
                .build();
        verificationTokensRepository.save(verificationToken);
        //Gửi email xác thực tới user dẫn tới trang xác thực
        emailVerifyService.sendEmail(emailRequest.getEmail(),
                StringApplication.FIELD.VERIFY + StringApplication.FIELD.EMAIL, userEmail.getUser().getProfile().getFullName(),
                CorsConfig.BASE_URL +
                        "/WebProject/auth/verified?verificationId=" + verificationToken.getVerificationTokenId(),
                timeExpired + StringApplication.FIELD.HOURS);
        return new Response<>(
                true,
                StringApplication.SUCCESS.CHECK_EMAIL,
                null
        );
    }


    @Transactional
    public Response<DeviceResponse> sendVerifyDevice(EmailRequest emailRequest, UUID deviceId,
                                                     String userAgent, String deviceType, String deviceName) {
        //Kiểm tra email
        var userEmail = emailRepository.findByEmail(emailRequest.getEmail()).orElseThrow(() ->
                new EntityNotFoundException(StringApplication.FIELD.EMAIL +  StringApplication.FIELD.NOT_EXIST)
        );
        //Kiểm tra email xác thực chưa
        if(!userEmail.getValidated()){
            throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.UNVERIFIED);
        }
        //Nếu deviceId có trên máy (đã từng xác thực) thì check số lần gửi yêu cầu
        if(deviceId != null){
            checkResendRateLimit(deviceId, VerificationTypes.VERIFY_DEVICE);
        }
        //Tìm device trên db nếu không tồn tại sinh device mới và lưu vào db
        var device = Optional.ofNullable(deviceId)
                .flatMap(deviceRepository::findById)
                .orElseGet(()->
                        Device.builder()
                                .deviceName(deviceName)
                                .deviceType(deviceType)
                                .userAgent(userAgent)
                                .build());
        deviceRepository.save(device);
        //Lấy dữ liệu user và session(nếu không có thì tạo mới session)
        var user = userEmail.getUser();
        var session = sessionRepository.findByUserAndDevice(user, device)
                .orElseGet(()->
                        Session.builder()
                                .device(device)
                                .user(user)
                                .revoked(false)
                                .validated(false)
                                .build());
        if(session.getValidated()){
            //Nếu session đã được xác thực đá exception
            throw new ConflictException(StringApplication.FIELD.DEVICE + StringApplication.FIELD.VERIFIED);
        }
        sessionRepository.save(session);
        //Tạo yêu cầu xác thực và lưu vào db
        var verificationToken = VerificationTokens.builder()
                .expiredAt(LocalDateTime.now().plusHours(timeExpired))
                .verificationType(VerificationTypes.VERIFY_DEVICE)
                .typeId(session.getSessionId())
                .user(user)
                .build();
        verificationTokensRepository.save(verificationToken);
        //Gửi email xác thực tới user
        emailVerifyService.sendEmail(emailRequest.getEmail(),
                StringApplication.FIELD.VERIFY + StringApplication.FIELD.DEVICE,
                userEmail.getUser().getProfile().getFullName(),
                CorsConfig.BASE_URL +
                        "/WebProject/auth/verified?verificationId=" + verificationToken.getVerificationTokenId(),
                timeExpired + StringApplication.FIELD.HOURS);
        return new Response<>(
                true,
                StringApplication.SUCCESS.CHECK_EMAIL,
                new DeviceResponse(session.getDevice().getDeviceId())
        );
    }

    @Transactional
    public Response<Void> verify(String token) {
        //Kiểm tra yêu cầu có hợp lệ không
        var tokenVerify = verificationTokensRepository.findByVerificationTokenId(UUID.fromString(token))
                .orElseThrow(()-> new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID));
        //Kiểm tra thời hạn yêu cầu
        if(LocalDateTime.now().isAfter(tokenVerify.getExpiredAt())) {
            throw new ConflictException(StringApplication.FIELD.REQUEST +  StringApplication.FIELD.EXPIRED);
        }
        //Nếu là xác thực email
        if(tokenVerify.getVerificationType() ==  VerificationTypes.VERIFY_EMAIL) {
            //Tìm email
            var userEmail = emailRepository.findById(tokenVerify.getTypeId()).orElseThrow(()->
                    new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.INVALID)
            );
            //Kiểm tra xác thực email đá exception nếu xác thực rồi
            if(userEmail.getValidated()){
                throw new ConflictException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.VERIFIED);
            }
            //Xác thực thành công
            userEmail.setValidated(true);
            emailRepository.save(userEmail);
            var user = userEmail.getUser();
            //Nếu trạng thái người dùng là đang xác thực thì set sang hoạt động
            if(user.getUserStatus() == UserStatus.PENDING){
                user.setUserStatus(UserStatus.ACTIVE);
            }
            userRepository.save(user);
            //Xoá yêu cầu xác thực
            verificationTokensRepository.deleteByUser_UserIdAndVerificationType(tokenVerify.getUser().getUserId(),
                    VerificationTypes.VERIFY_EMAIL);
            return new  Response<>(
                    true,
                    StringApplication.FIELD.VERIFIED_SUCCESS,
                    null
            );
        }
        //Nếu là xác thực session
        else if (tokenVerify.getVerificationType() == VerificationTypes.VERIFY_DEVICE) {
            //Tìm session
            var session = sessionRepository.findById(tokenVerify.getTypeId()).orElseThrow(()->
                    new EntityNotFoundException(StringApplication.FIELD.SESSION_LOGIN + StringApplication.FIELD.INVALID));
            //Set xác thực thành công
            session.setValidated(true);
            sessionRepository.save(session);
            //Xoá yêu cầu xác thực
            verificationTokensRepository.deleteByUser_UserIdAndVerificationType(tokenVerify.getUser().getUserId(),
                    VerificationTypes.VERIFY_DEVICE);
            return new  Response<>(
                    true,
                    StringApplication.FIELD.VERIFIED_SUCCESS,
                    null
            );
        }
        throw new UnprocessableEntityException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID);
    }

    @Transactional
    public Response<Void> sendVerifyChangePassword(EmailRequest emailRequest){
        //Tìm email
        var userEmail = emailRepository.findByEmail(emailRequest.getEmail()).orElseThrow(() ->
                new EntityNotFoundException(StringApplication.FIELD.EMAIL +  StringApplication.FIELD.NOT_EXIST)
        );
        //Kiểm tra email đã xác thực chưa
        if(!userEmail.getValidated()){
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
                .user(user)
                .verificationType(VerificationTypes.VERIFY_CHANGE_PASSWORD)
                .expiredAt(LocalDateTime.now().plusMinutes(minExpired))
                .typeId(user.getUserId())
                .build();
        verificationTokensRepository.save(verifyToken);
        //Gửi email dẫn tới trang thay đổi mật khẩu
        emailVerifyService.sendEmail(userEmail.getEmail(),
                StringApplication.FIELD.VERIFY + StringApplication.FIELD.CHANGE_PASSWORD,
                user.getProfile().getFullName(),
                CorsConfig.BASE_URL +
                        "/WebProject/auth/change-password?token="+verifyToken.getVerificationTokenId(),
                minExpired + StringApplication.FIELD.MINUTES);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> changePassword(PasswordRequest passwordRequest, String token){
        // Kiểm tra yêu cầu tồn tại không
        var tokenVerify = verificationTokensRepository.findByVerificationTokenId(UUID.fromString(token))
                .orElseThrow(()->
                        new ConflictException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID));
        //Kiểm tra thời gian hết hạn
        if(LocalDateTime.now().isAfter(tokenVerify.getExpiredAt())) {
            throw new ConflictException(StringApplication.FIELD.REQUEST +  StringApplication.FIELD.EXPIRED);
        }
        //Set password mới kèm bcrypt
        var user = tokenVerify.getUser();
        user.setPassword(passwordEncoder.encode(passwordRequest.getPassword()));
        userRepository.save(user);
        //Xoá yêu cầu xác thực
        verificationTokensRepository.deleteByUser_UserIdAndVerificationType(user.getUserId(),
                VerificationTypes.VERIFY_CHANGE_PASSWORD);
        return new Response<>(
                true,
                StringApplication.FIELD.CHANGE_PASSWORD + StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
