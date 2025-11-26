package com.example.hello.Infrastructure.Cloudinary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinaryResponse {
    @JsonProperty("public_id")
    String publicId;
    String url;
}
