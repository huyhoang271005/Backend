package com.example.hello.Infrastructure.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.hello.Infrastructure.Exception.FileUploadException;
import com.example.hello.Infrastructure.Exception.FileUploadIOException;
import com.example.hello.Middleware.StringApplication;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {
    Cloudinary cloudinary;


    public CloudinaryResponse uploadImage(MultipartFile file, String folder) {
        if(!isImage(file) || file.isEmpty()) {
            log.error("Image {} is empty or file not image", file.getOriginalFilename());
            throw new FileUploadException(StringApplication.ERROR.UPLOAD_ERROR);
        }
        try {
            Map<?,?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "Ecommerce/" + folder,
                    "quality", "auto",
                    "fetch_format", "auto",
                    "width", 800,
                    "crop", "limit"));
            log.info("Image upload successfully");
            return new CloudinaryResponse((String) result.get("public_id"), (String) result.get("secure_url"));
        } catch (IOException e) {
            log.error("Image {} upload failed", file.getOriginalFilename());
            throw new FileUploadIOException(StringApplication.ERROR.UPLOAD_IO_ERROR + e.getMessage());
        }
    }

    public void deleteImages(List<String> publicIds) {
        if(publicIds == null || publicIds.isEmpty()) {
            log.info("Image list is empty");
            return;
        }
        try {
            Map<?,?> result = cloudinary.api().deleteResources(publicIds
                            .stream()
                            .filter(Objects::nonNull)
                            .toList(),
                    ObjectUtils.emptyMap());
            log.info("Result delete with cloudinary is {}", result);
            Map<?, ?> deletedObject = (Map<?, ?>) result.get("deleted");
            deletedObject.forEach((k,v)->{
                if(k instanceof String key && v instanceof String value) {
                    log.info("Image {} {}", key, value);
                }
            });
        } catch (Exception e) {
            log.error("Error when delete images", e);
        }
    }


    public boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        log.info("Content type is {}", contentType);
        return contentType != null && contentType.startsWith("image/");
    }

}
