package com.example.hello.Feature.User.Repository;

import com.example.hello.Entity.Device;
import com.example.hello.Entity.Session;
import com.example.hello.Feature.User.dto.SessionInfo;
import com.example.hello.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByUserAndDevice(User user, Device device);
    @Query("""
        select s.sessionId as sessionId, s.revoked as revoked,
                s.validated as validated, s.lastLogin as lastLogin,
                s.createdAt createdAt, d.deviceName as deviceName, d.userAgent as userAgent,
                d.deviceType as deviceType, s.ipAddress as ipAddress, s.city as city,
                s.country as country, s.region as region, s.timezone as timezone
        from Session s join
            s.device d
            join s.user u
        where u.userId = :userId
        order by s.lastLogin desc
       """)
    Page<SessionInfo> getSessions(UUID userId, Pageable pageable);


    @Query("""
            select s
            from Token t
            join t.session s
            where t.tokenValue = :tokenValue
            """)
    Optional<Session> findSessionByTokenValue(String tokenValue);

    @Modifying
    @Query("""
            delete from Session s
            where s.validated = false and s.createdAt < :threshold
            """)
    void deleteSessionExpired(Instant threshold);

    @Modifying
    @Query("""
            delete from Device d
            where not exists (
                        select s
                        from Session s
                        where s.device = d
                        )
            """)
    void deleteDeviceNull();

    @Modifying
    @Query("""
            update Session s
            set s.revoked = true
            where s.lastLogin <= :timeAgo
            """)
    void revokeExpiredSessions(Instant timeAgo);

    void deleteByRevokedAndLastLoginBefore(Boolean revoked, Instant timeAgo);
}
