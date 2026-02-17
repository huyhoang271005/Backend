package com.example.hello.Feature.Contact;

import com.example.hello.Entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {
    Page<Contact> findAllByUser_UserIdOrderByUpdatedAtDesc(UUID userId, Pageable pageable);
}