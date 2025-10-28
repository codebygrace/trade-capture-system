package com.technicalchallenge.security;

import com.technicalchallenge.repository.ApplicationUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Responsible for retrieving a UserDetails object from the database
 */
@Service
public class MyUserDetailsService implements UserDetailsService {


    private final ApplicationUserRepository applicationUserRepository;

    public MyUserDetailsService(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return applicationUserRepository.findByLoginId(username)
                .map(applicationUser -> new MyUserPrincipal(applicationUser))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}

