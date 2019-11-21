package si.inspirited.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.UserRepository;

@Component
@RepositoryEventHandler
public class CustomUserEventHandler {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @HandleBeforeCreate
    public void handleUserCreate(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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