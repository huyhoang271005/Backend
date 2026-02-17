package com.example.hello.Feature.User.Repository;

import com.example.hello.Entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
}
