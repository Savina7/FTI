package Savina.ftiApp.security;

import Savina.ftiApp.entity.Role;
import Savina.ftiApp.entity.User;
import Savina.ftiApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sherben si ure mes Spring Security dhe DB-se.
 * Spring Security therret loadUserByUsername() me email-in e nxjerre nga JWT,
 * dhe merr UserDetails (me rolet) per te populluar SecurityContext.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Perdoruesi nuk u gjet: " + email));

        final Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getRoleName)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

        final String storedPassword = user.getPassword() != null ? user.getPassword() : "";
        final String userEmail = user.getEmail();

        return new UserDetails() {
            @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
            @Override public String getPassword() { return storedPassword; }
            @Override public String getUsername() { return userEmail; }
            @Override public boolean isAccountNonExpired() { return true; }
            @Override public boolean isAccountNonLocked() { return true; }
            @Override public boolean isCredentialsNonExpired() { return true; }
            @Override public boolean isEnabled() { return true; }
        };
    }
}
