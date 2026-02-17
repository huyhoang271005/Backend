package com.example.hello.Feature.User.Repository;

import com.example.hello.Entity.Email;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailRepository extends JpaRepository<Email, UUID> {
    @EntityGraph(attributePaths = {"user"})
    Optional<Email> findByEmail(String email);
}
