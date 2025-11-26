package com.example.hello.Users.Authentication.Repository;

import com.example.hello.Users.Authentication.Entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailRepository extends JpaRepository<Email, UUID> {
    Optional<Email> findByEmail(String email);
}
