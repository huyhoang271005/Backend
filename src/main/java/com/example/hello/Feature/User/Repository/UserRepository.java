package com.example.hello.Feature.User.Repository;

import com.example.hello.Feature.User.dto.HomeInfo;
import com.example.hello.Entity.User;
import com.example.hello.Feature.User.dto.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmails_Email(String email);
    Boolean existsByUsername(String username);
    Boolean existsByUsernameAndUserIdNot(String username, UUID id);
    Optional<User> findByEmails_Email(String email);

    @Query("""
            select u.userId as userId, u.username as username,
                        p.fullName as fullName, p.imageUrl as imageUrl, p.createdAt as createdAt
            from Email e
            join e.user u
            join u.profile p
            where e.email = :email or :email is null
            order by p.createdAt desc
           """)
    Page<UserInfo> getListUser(String email, Pageable pageable);

    List<User> findByRole_RoleId(UUID roleRoleId);

    @Query("""
            select u.username as username, p.imageUrl as imageUrl, u.role.roleName as roleName
            from User u
            join u.profile p
            where u.userId = :userId
            """)
    HomeInfo getHomeInfo(UUID userId);


}
