package com.example.hello.Feature.User.Service;

import com.example.hello.Feature.User.DTO.EmailResponse;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.Authentication.DTO.EmailRequest;
import com.example.hello.Entity.Email;
import com.example.hello.Repository.EmailRepository;
import com.example.hello.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    UserRepository userRepository;
    EmailRepository emailRepository;

    @Transactional
    public Response<EmailResponse> addEmail(UUID userId, EmailRequest emailRequest) {
        //Kiểm tra tồn tại user
        var user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST)
        );
        //Kiểm tra email nếu tồn tại đá exception
        var email = emailRepository.findByEmail(emailRequest.getEmail()).orElse(null);
        if (email != null) {
            throw new ConflictException(StringApplication.FIELD.EMAIL +  StringApplication.FIELD.EXISTED);
        }
        //Save email mới
        var userEmail = Email.builder()
                .email(emailRequest.getEmail())
                .validated(false)
                .user(user)
                .build();
        emailRepository.save(userEmail);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                EmailResponse.builder()
                        .emailId(userEmail.getEmailId())
                        .email(userEmail.getEmail())
                        .validated(false)
                        .build()
        );
    }

    @Transactional
    public Response<Void> deleteEmail(UUID emailId) {
        //Kiểm tra tồn tại email
        var userEmail = emailRepository.findById(emailId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.EMAIL +  StringApplication.FIELD.NOT_EXIST)
        );
        //Kiểm tra danh sách email của user
        var user = userEmail.getUser();
        //Lấy nhưng email nào đã xác thực
        List<Email> listEmail = user.getEmails().stream()
                .filter(email -> email.getValidated() == true)
                .toList();
        //Nếu email đã xác thực < 2 đá exception
        if(listEmail.size() < 2 && userEmail.getValidated()) {
            throw new ConflictException(StringApplication.ERROR.VERIFIED_EMAIL_MUST_EXIST);
        }
        //Xoá email
        user.getEmails().remove(userEmail);
        emailRepository.delete(userEmail);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
