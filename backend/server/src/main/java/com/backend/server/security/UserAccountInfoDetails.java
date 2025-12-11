package com.backend.server.security;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.backend.server.models.Account;
import com.backend.server.models.Role;

public class UserAccountInfoDetails implements UserDetails {
    private String email;
    private String password;
    private List <GrantedAuthority> authorities;

    public UserAccountInfoDetails(Account account, Role role) {
        this.email = account.getEmail();
        this.password = account.getPassword();
        this.authorities = List.of(role.getRoleName().split(","))
        .stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
