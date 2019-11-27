package si.inspirited;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import si.inspirited.persistence.model.Role;
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.RoleRepository;
import si.inspirited.persistence.repositories.UserRepository;
import si.inspirited.security.ActiveUserStore;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ActiveUserStore activeUserStore;

    private final String URL_USERS = "/users";
    private final String URL_LOGIN = "/login";

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void addUser_whenCouldBeFoundInStorage_thenCorrect() {
        assertNotNull(mockMvc);
        String mockUserLogin = "Name";
        String userStub = getUserStub(mockUserLogin);
        Long sizeOfStorageBeforeOperation = userRepository.count();
        try {
            mockMvc.perform(post(URL_USERS).content(userStub));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long sizeOfStorageAfterOperation = userRepository.count();
        assertTrue(sizeOfStorageBeforeOperation < sizeOfStorageAfterOperation);
        User receivedFromStorage = userRepository.findByLogin(mockUserLogin);
        assertNotNull(receivedFromStorage);
    }

    @Test
    public void addCoupleUsersWithTheSameNames_whenStoredOnlyTheFirst_thenCorrect() {
        String duplicatedUsersLogin = "Duplicated";
        String oneOfCoupleWithDuplicatedLogin = getUserStub(duplicatedUsersLogin);
        Long sizeOfStorageBeforeOperations = userRepository.count();
        try {
            mockMvc.perform(post(URL_USERS).content(oneOfCoupleWithDuplicatedLogin));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long sizeOfStorageAfterFirstUserPosted = userRepository.count();
        try {
            mockMvc.perform(post(URL_USERS).content(oneOfCoupleWithDuplicatedLogin));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long sizeOfStorageAfterSecondUserPosted = userRepository.count();

        assertTrue(sizeOfStorageBeforeOperations < sizeOfStorageAfterFirstUserPosted);
        assertEquals(sizeOfStorageAfterFirstUserPosted, sizeOfStorageAfterSecondUserPosted);
    }

    @Test
    public void checkNewAddedUsersForReceivingRolesOnRegistration_whenFirstAddedUserIsAdmin_andNextAreRegularUsers_thenCorrect() {
        String mockAdminName = "Admin";
        String adminStub = getUserStub(mockAdminName);
        try {
            mockMvc.perform(post(URL_USERS).content(adminStub));
        } catch (Exception e) {
            e.printStackTrace();
        }
        User returnedAdmin = userRepository.findByLogin(mockAdminName);
        assertNotNull(returnedAdmin.getRoles());
        boolean isAdmin = false;
        for (Role role : returnedAdmin.getRoles()) {
            if ("ROLE_ADMIN".equals(role.getAuthority())) { isAdmin = true; }
        }
        assertTrue(isAdmin);

        registerCoupleRegularUsers("RegularUser");
        Iterable<User> allRegistered = userRepository.findAll();
        for (User user : allRegistered) {
            String currentUserName = user.getLogin();
            if (!mockAdminName.equals(currentUserName)) {
                boolean isUser = false;
                for (Role role : user.getRoles()) {
                    if ("ROLE_USER".equals(role.getAuthority())) {
                        isUser = true;
                    }
                }
                assertTrue(isUser);
            }
        }
    }

    @Test
    public void loggingInAsAnonymous_whenFoundLoggedIn_thenCorrect() {
        String someLogin = "SomeLogin";
        String somePassword = "password";
        String someUser = getUserStub(someLogin);
        try {
            mockMvc.perform(post(URL_USERS).content(someUser));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int sizeBeforeLoginOperation = activeUserStore.users.size();
        try {
            mockMvc.perform(post(URL_LOGIN).param("username",someLogin).param("password", somePassword));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int sizeAfterLoginOperation = activeUserStore.users.size();

        assertTrue(sizeBeforeLoginOperation < sizeAfterLoginOperation);
        assertTrue(activeUserStore.users.contains(someLogin));
    }

    @After
    public void flushUserStorage() {
        userRepository.deleteAll();
        User testAdmin = new User("test", "test");
        testAdmin.setFirstName("test");
        testAdmin.setLastName("test");
        testAdmin.setRoles(new ArrayList<>(Arrays.asList(roleRepository.findByName("ROLE_ADMIN"), roleRepository.findByName("ROLE_USER"))));
        userRepository.save(testAdmin);
    }

    //
    private String getUserStub(final String login) {
        return "{\"login\":\"" + login + "\",\"password\":\"password\"}";
    }

    private void registerCoupleRegularUsers(final String prefixOfLogin) {
        for (int i = 0; i < 12; i++) {
            try {
                mockMvc.perform(post(URL_USERS).content(getUserStub(prefixOfLogin + i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
