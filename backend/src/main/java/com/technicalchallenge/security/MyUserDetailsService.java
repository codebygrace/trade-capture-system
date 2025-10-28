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

    /**
     * Used to retrieve an ApplicationUser and its privileges from ApplicationUserRepository.
     * If a match is found then it's wrapped in a MyUserPrinciple.
     * If no match is found then an exception is thrown
     * @param username loginId for the user
     * @return a populated UserDetails object representing the authenticated user
     * @throws UsernameNotFoundException if no user could be found with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return applicationUserRepository.findByLoginIdWithPrivileges(username)
                .map(applicationUser -> new MyUserPrincipal(applicationUser))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}

