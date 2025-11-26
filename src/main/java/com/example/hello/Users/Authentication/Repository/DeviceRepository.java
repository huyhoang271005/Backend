package com.example.hello.Users.Authentication.Repository;

import com.example.hello.Users.Authentication.Entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
}
