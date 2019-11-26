package si.inspirited.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import si.inspirited.persistence.model.Role;
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.RoleRepository;
import si.inspirited.persistence.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;

@Component
@RepositoryEventHandler
public class CustomUserEventHandler {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @HandleBeforeCreate
    public void handleUserCreate(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.isNew()) {
            user.setEnabled(true);
            if (userRepository.count() == 1) {
                user.setRoles(new ArrayList<>(Arrays.asList(roleRepository.findByName("ROLE_ADMIN"))));
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
    }
}