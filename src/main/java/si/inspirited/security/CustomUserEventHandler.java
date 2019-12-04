package si.inspirited.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import si.inspirited.persistence.model.Role;
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.RoleRepository;
import si.inspirited.persistence.repositories.UserRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Component
@RepositoryEventHandler
public class CustomUserEventHandler {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

//    @Autowired
//    IAuthenticationFacade authenticationFacade;

    @HandleBeforeCreate
    public void handleUserCreate(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.isNew()) {
            user.setEnabled(true);
            if (userRepository.count() == 1) {
                user.setRoles(new ArrayList<>(Arrays.asList(roleRepository.findByName("ROLE_ADMIN"), roleRepository.findByName("ROLE_USER"))));
            } else {
                user.setRoles(new ArrayList<>(Arrays.asList(roleRepository.findByName("ROLE_USER"))));
            }
        }
    }

    @HandleBeforeSave
    public void handleUserUpdate(User user) {
        if (user.getPassword() == null || user.getPassword().equals("")) {
            User storedUser = userRepository.findById(user.getId()).isPresent() ? userRepository.findById(user.getId()).get() : new User();
            user.setPassword(storedUser.getPassword());
        }
        else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        Optional<User> currentUser = userRepository.findById(user.getId());
        boolean isAdmin = false;
        if (currentUser.get().getRoles() != null)
        {isAdmin = currentUser.orElse(new User()).getRoles().stream().anyMatch((role)->"ROLE_ADMIN".equals(role.getAuthority()));}
        if (!isAdmin) {
            user.setRoles(currentUser.get().getRoles());
        }
    }
}