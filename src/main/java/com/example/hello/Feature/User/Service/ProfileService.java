package com.example.hello.Feature.User.Service;

import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.Infrastructure.Cloudinary.FolderCloudinary;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.UserMapper;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.User.dto.EmailResponse;
import com.example.hello.Feature.User.dto.ProfileRequest;
import com.example.hello.Feature.User.dto.ProfileResponse;
import com.example.hello.Feature.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
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
        log.info("Found profile successfully");
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
        profile.setBirthday(profile.getBirthday());
        profile.setFullName(profileRequest.getFullName());
        if(avatar != null) {
            if(profile.getImageId() != null){
                cloudinaryService.deleteImage(profile.getImageId());
            }
            CloudinaryResponse uploadImage = cloudinaryService.uploadImage(avatar, FolderCloudinary.user.name());
            profile.setImageUrl(uploadImage.getUrl());
            profile.setImageId(uploadImage.getPublicId());
        }
        log.info("Profile updated successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
