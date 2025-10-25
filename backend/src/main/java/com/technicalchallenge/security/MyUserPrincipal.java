package com.technicalchallenge.security;

import com.technicalchallenge.model.ApplicationUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MyUserPrincipal implements UserDetails {

    private ApplicationUser applicationUser;

    public MyUserPrincipal(ApplicationUser applicationUser) {
        this.applicationUser = applicationUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // User privileges e.g. "BOOK_TRADE"
        List<GrantedAuthority> authorities = applicationUser.getUserPrivileges().stream()
                .map(privilege -> new SimpleGrantedAuthority(privilege.getPrivilege().getName()))
                .collect(Collectors.toList());

        // User role e.g. "ADMIN", "TRADER_SALES" "SUPERUSER"
        String role = applicationUser.getUserProfile().getUserType();

        // Prefix role with "ROLE_" to differentiate it from other types of authorities
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return  authorities;
    }

    @Override
    public String getPassword() {
        return applicationUser.getPassword();
    }

    @Override
    public String getUsername() {
        return applicationUser.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return applicationUser.isActive();
    }
}
