package com.example.hello.Repository;

import com.example.hello.Entity.Device;
import com.example.hello.Entity.Session;
import com.example.hello.DataProjection.SessionInfo;
import com.example.hello.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByUserAndDevice(User user, Device device);
    @Query("""
        select s.sessionId as sessionId, s.revoked as revoked,
                s.validated as validated, s.lastLogin as lastLogin,
                s.createdAt createdAt, d.deviceName as deviceName, d.userAgent as userAgent,
                d.deviceType as deviceType, s.ipAddress as ipAddress
        from Session s join
            s.device d
            join s.user u
        where u.userId = :userId
        order by s.lastLogin desc
       """)
    Page<SessionInfo> getSessions(UUID userId, Pageable pageable);
}
