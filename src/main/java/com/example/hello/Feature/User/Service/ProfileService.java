package com.example.hello.Feature.User.Service;

import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.FileUploadIOException;
import com.example.hello.Mapper.UserMapper;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.User.DTO.EmailResponse;
import com.example.hello.Feature.User.DTO.ProfileRequest;
import com.example.hello.Feature.User.DTO.ProfileResponse;
import com.example.hello.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {
    UserRepository userRepository;
    CloudinaryService cloudinaryService;
    UserMapper userMapper;

    @Transactional(readOnly = true)
    public Response<ProfileResponse> getProfile(UUID userId) {
        //Kiểm tra tồn tại user
        var user = userRepository.findById(userId).orElseThrow(()->
                new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST));
        //Lấy dữ liệu email từ entity gán và response
        var emails = user.getEmails().stream()
                .map(email -> EmailResponse.builder()
                        .emailId(email.getEmailId())
                        .email(email.getEmail())
                        .validated(email.getValidated())
                        .build())
                .toList();
        //Lấy dữ liệu profile
        var profile = user.getProfile();
        var profileResponse = userMapper.toProfileResponse(profile);
        profileResponse.setEmails(emails);
        profileResponse.setUsername(user.getUsername());
        profileResponse.setRoleName(user.getRole().getRoleName());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                profileResponse
        );
    }

    @Transactional
    public Response<Void> updateProfile(UUID userId, ProfileRequest profileRequest, MultipartFile avatar) {
        //Kiểm trả tồn tại của user
        var user =  userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.USER + StringApplication.FIELD.NOT_EXIST)
        );
        if(userRepository.existsByUsernameAndUserIdNot(user.getUsername(), userId)) {
            throw new ConflictException(StringApplication.FIELD.USERNAME + StringApplication.FIELD.EXISTED);
        }
        //Set dữ liệu profile và user
        user.setUsername(profileRequest.getUsername());
        var profile = user.getProfile();
        profile.setGender(profileRequest.getGender());
        profile.setBirthday(LocalDate.parse(profileRequest.getBirthday()));
        profile.setFullName(profileRequest.getFullName());
        if(avatar != null) {
            if(profile.getImageId() != null){
                Boolean deleteImage = cloudinaryService.deleteImage(profile.getImageId());
                if(!deleteImage){
                    throw new FileUploadIOException(StringApplication.ERROR.UPLOAD_IO_ERROR);
                }
            }
            CloudinaryResponse uploadImage = cloudinaryService.uploadImage(avatar, "user1");
            profile.setImageUrl(uploadImage.getUrl());
            profile.setImageId(uploadImage.getPublicId());
        }
        user.setProfile(profile);
        userRepository.save(user);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
