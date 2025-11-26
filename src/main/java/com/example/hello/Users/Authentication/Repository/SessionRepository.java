package com.example.hello.Users.Authentication.Repository;

import com.example.hello.Users.Authentication.Entity.Device;
import com.example.hello.Users.Authentication.Entity.Session;
import com.example.hello.Users.User.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByUserAndDevice(User user, Device device);
}
