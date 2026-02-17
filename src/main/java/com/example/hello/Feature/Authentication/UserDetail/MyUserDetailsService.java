package com.example.hello.Feature.Authentication.UserDetail;

import com.example.hello.Infrastructure.Cache.RolePermissionCacheService;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.User.Repository.EmailRepository;
import com.example.hello.Entity.Email;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    EmailRepository emailRepository;
    RolePermissionCacheService  rolePermissionCacheService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Email email = emailRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(StringApplication.FIELD.EMAIL + StringApplication.FIELD.NOT_EXIST));
        return new MyUserDetails(email, rolePermissionCacheService.getPermissionsCache(email.getUser().getRole().getRoleId()));
    }
}
