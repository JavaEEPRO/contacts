package si.inspirited.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import si.inspirited.persistence.model.Privilege;
import si.inspirited.persistence.model.Role;
import si.inspirited.persistence.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import si.inspirited.persistence.repositories.PrivilegeRepository;
import si.inspirited.persistence.repositories.RoleRepository;
import si.inspirited.persistence.repositories.UserRepository;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // create initial privileges
        final Privilege userPrivilege = createPrivilegeIfNotFound("USER_PRIVILEGE");
        final Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        final Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        final Privilege passwordPrivilege = createPrivilegeIfNotFound("CHANGE_PASSWORD_PRIVILEGE");
        final Privilege exploreAllGroupsPrivilege = createPrivilegeIfNotFound("EXPLORE_ALL_GROUPS_PRIVILEGE");

        // create initial roles
        final List<Privilege> adminPrivileges = new ArrayList<Privilege>(Arrays.asList(userPrivilege, readPrivilege, writePrivilege, passwordPrivilege, exploreAllGroupsPrivilege));
        final List<Privilege> userPrivileges = new ArrayList<Privilege>(Arrays.asList(userPrivilege, readPrivilege, passwordPrivilege));
        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        final Role userRole = createRoleIfNotFound("ROLE_USER", userPrivileges);

        // create initial user
        createUserIfNotFound("test", "test", "test", "test", new ArrayList<Role>(Arrays.asList(adminRole)));

        alreadySetup = true;
    }

    @Transactional
    private final Privilege createPrivilegeIfNotFound(final String name) {
        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilege = privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    private final Role createRoleIfNotFound(final String name, final Collection<Privilege> privileges) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
        }
        role.setPrivileges(privileges);
        role = roleRepository.save(role);
        return role;
    }

    @Transactional
    private final User createUserIfNotFound(final String login, final String firstName, final String lastName, final String password, final Collection<Role> roles) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(password));
            user.setLogin(login);
            user.setEnabled(true);
        }
        user.setRoles(roles);
        user = userRepository.save(user);
        return user;
    }
}
