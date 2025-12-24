package com.example.hello.Infrastructure.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.hello.Infrastructure.Exception.FileUploadException;
import com.example.hello.Infrastructure.Exception.FileUploadIOException;
import com.example.hello.Middleware.StringApplication;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {
    Cloudinary cloudinary;


    public CloudinaryResponse uploadImage(MultipartFile file, String folder) {
        if(!isImage(file) && file.isEmpty()) {
            throw new FileUploadException(StringApplication.ERROR.UPLOAD_ERROR);
        }
        try {
            Map<?,?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "quality", "auto",
                    "fetch_format", "auto",
                    "width", 800,
                    "crop", "limit"));
            return new CloudinaryResponse((String) result.get("public_id"), (String) result.get("secure_url"));
        } catch (IOException e) {
            throw new FileUploadIOException(StringApplication.ERROR.UPLOAD_IO_ERROR + e.getMessage());
        }
    }

    public Boolean deleteImage(String publicId) {
        try {
            Map<?,?> result = cloudinary.uploader()
                    .destroy(publicId, ObjectUtils.emptyMap());

            return result.get("result").equals("ok");
        } catch (IOException e) {
            throw new FileUploadIOException(StringApplication.ERROR.DELETE_IO_ERROR);
        }
    }


    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

}
