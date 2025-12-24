package com.example.hello.Feature.Attribute;

import com.example.hello.Entity.Attribute;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.AttributeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttributeService {
    AttributeRepository attributeRepository;

    @Transactional
    public Response<AttributeDTO> addAttribute(AttributeDTO attributeDTO) {
        var attribute = attributeRepository.existsByAttributeName(attributeDTO.getAttributeName());
        if(attribute) {
            throw new ConflictException(StringApplication.FIELD.ATTRIBUTE + StringApplication.FIELD.EXISTED);
        }
        attributeRepository.save(Attribute.builder()
                .attributeName(attributeDTO.getAttributeName())
                .build());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                AttributeDTO.builder()
                        .attributeId(attributeDTO.getAttributeId())
                        .attributeName(attributeDTO.getAttributeName())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<AttributeDTO>> getAllAttributes(Pageable pageable) {
        var attributes = attributeRepository.findAllByPageable(pageable);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        attributes.hasNext(),
                        attributes.getContent().stream()
                                .map(attributeInfo -> AttributeDTO.builder()
                                        .attributeId(attributeInfo.getAttributeId())
                                        .attributeName(attributeInfo.getAttributeName())
                                        .build())
                                .toList()
                )
        );
    }

    @Transactional
    public Response<Void> updateAttribute(AttributeDTO attributeDTO) {
        if(!attributeRepository.existsById(attributeDTO.getAttributeId())){
            throw new ConflictException(StringApplication.FIELD.ATTRIBUTE + StringApplication.FIELD.NOT_EXIST);
        }
        attributeRepository.save(Attribute.builder()
                        .attributeId(attributeDTO.getAttributeId())
                        .attributeName(attributeDTO.getAttributeName())
                .build());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> deleteAttribute(UUID attributeId) {
        attributeRepository.findById(attributeId).orElseThrow(
                ()-> new  EntityNotFoundException(StringApplication.FIELD.ATTRIBUTE + StringApplication.FIELD.NOT_EXIST)
        );
        attributeRepository.deleteById(attributeId);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}
