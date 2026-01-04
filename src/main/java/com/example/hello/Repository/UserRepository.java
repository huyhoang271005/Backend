package com.example.hello.Repository;

import com.example.hello.Feature.Authentication.DataProjection.HomeInfo;
import com.example.hello.Entity.User;
import com.example.hello.Feature.Authentication.DataProjection.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Boolean existsByEmails_Email(String email);
    Boolean existsByUsername(String username);
    Boolean existsByUsernameAndUserIdNot(String username, UUID id);

    @Query("""
            select u.userId as userId, u.username as username,
                        p.fullName as fullName, p.imageUrl as imageUrl, p.createdAt as createdAt
            from User u
            join u.profile p
            order by p.createdAt desc
           """)
    Page<UserInfo> getListUser(Pageable pageable);

    List<User> findByRole_RoleId(UUID roleRoleId);

    @Query("""
            select u.username as username, p.imageUrl as imageUrl, u.role.roleName as roleName
            from User u
            join u.profile p
            where u.userId = :userId
            """)
    HomeInfo getHomeInfo(UUID userId);


}
