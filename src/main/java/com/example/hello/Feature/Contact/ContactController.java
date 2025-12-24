package com.example.hello.Feature.Contact;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("contacts")
public class ContactController {
    ContactService contactService;

    @GetMapping
    public ResponseEntity<?> getContacts(@AuthenticationPrincipal UUID userId, Pageable pageable) {
        return ResponseEntity.ok(contactService.getContacts(userId, pageable));
    }

    @PostMapping
    public ResponseEntity<?> addContact(@AuthenticationPrincipal UUID userId, @RequestBody ContactDTO contactDTO) {
        return ResponseEntity.ok(contactService.addContact(userId, contactDTO));
    }

    @PutMapping
    public ResponseEntity<?> updateContact(@AuthenticationPrincipal UUID userId, @RequestBody ContactDTO contactDTO) {
        return ResponseEntity.ok(contactService.updateContact(userId, contactDTO));
    }

    @DeleteMapping("{contactId}")
    public ResponseEntity<?> deleteContact(@AuthenticationPrincipal UUID userId, @PathVariable UUID contactId) {
        return ResponseEntity.ok(contactService.deleteContact(userId, contactId));
    }
}
