package si.inspirited;

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
import si.inspirited.persistence.repositories.UserRepository;

import java.util.List;

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

    final String URL_USERS = "/users";

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
            if (!mockAdminName.equals(user.getLogin())) {
                boolean isUser = false;
                for (Role role : user.getRoles()) {
                    if ("USER_ROLE".equals(role.getAuthority())) {
                        isUser = true;
                    }
                    assertTrue(isUser);
                }

            }
        }
    }

    private String getUserStub(String login) {
        return "{\"login\":\"" + login + "\",\"password\":\"password\"}";
    }

    private void registerCoupleRegularUsers(String prefixOfLogin) {
        for (int i = 0; i < 12; i++) {
            try {
                mockMvc.perform(post(URL_USERS).content(getUserStub(prefixOfLogin + i)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
