package com.example.hello.Feature.Attribute;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("attributes")
public class AttributeController {
    AttributeService attributeService;

    @PreAuthorize("hasAuthority('ADD_ATTRIBUTE')")
    @PostMapping
    public ResponseEntity<?> addAttribute(@RequestBody AttributeDTO attributeDTO) {
        return ResponseEntity.ok(attributeService.addAttribute(attributeDTO));
    }

    @GetMapping
    public ResponseEntity<?> getAttribute(Pageable pageable) {
        return ResponseEntity.ok(attributeService.getAllAttributes(pageable));
    }

    @PreAuthorize("hasAuthority('UPDATE_ATTRIBUTE')")
    @PutMapping
    public ResponseEntity<?> updateAttribute(@RequestBody AttributeDTO attributeDTO) {
        return ResponseEntity.ok(attributeService.updateAttribute(attributeDTO));
    }

    @PreAuthorize("hasAuthority('DELETE_ATTRIBUTE')")
    @DeleteMapping("{attributeId}")
    public ResponseEntity<?> deleteAttribute(@PathVariable UUID attributeId) {
        return ResponseEntity.ok(attributeService.deleteAttribute(attributeId));
    }
}
