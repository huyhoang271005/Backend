package com.example.hello.Users.User.Repository;

import com.example.hello.Users.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmails_Email(String email);
    Boolean existsByUsername(String username);
}
