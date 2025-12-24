package com.example.hello.Feature.Authentication.UserDetail;

import com.example.hello.Entity.Email;
import com.example.hello.Enum.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public record MyUserDetails(Email email) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var role = email.getUser().getRole();
        return role.getRolePermission().stream()
                .map(rolePermission -> rolePermission.getPermission().getPermissionName())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return email.getUser().getPassword();
    }

    @Override
    public String getUsername() {
        return email.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return email.getUser().getUserStatus() == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return email.getValidated();
    }
}
