package org.oplearn.project.security;

import lombok.RequiredArgsConstructor;
import org.oplearn.project.entity.User;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    User user = userRepository.findByUsernameAndIsDeletedFalse(username)
      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new CustomUserDetails(user);
  }
}
