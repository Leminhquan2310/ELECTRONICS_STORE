package com.electronics_store.security;

import com.electronics_store.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    // Additional methods to get user info
    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public String getPhone() {
        return user.getPhone();
    }

    public String getAddress() {
        return user.getAddress();
    }

    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }

    public LocalDateTime getCreatedAt() {
        return user.getCreatedAt();
    }

    public boolean isEmailVerified() {
        return user.getEmailVerified();
    }

    public User.UserStatus getStatus() {
        return user.getStatus();
    }

    public boolean hasRole(String roleName) {
        return user.hasRole(roleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return user.getId().equals(that.user.getId());
    }

    @Override
    public int hashCode() {
        return user.getId().hashCode();
    }

    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "id=" + user.getId() +
                ", username='" + user.getUsername() + '\'' +
                ", email='" + user.getEmail() + '\'' +
                ", enabled=" + isEnabled() +
                '}';
    }
}