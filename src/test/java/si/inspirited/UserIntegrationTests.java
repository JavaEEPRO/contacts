package si.inspirited;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import si.inspirited.persistence.model.Role;
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.RoleRepository;
import si.inspirited.persistence.repositories.UserRepository;
import si.inspirited.security.ActiveUserStore;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private final String URL_STATUS = "/status";
    private final String URL_LOGOUT = "/logout";

    private final String SOME_LOGIN = "SomeLogin";

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void addUser_whenCouldBeFoundInStorage_thenCorrect() {
        assertNotNull(mockMvc);
        String userStub = getUserStub(SOME_LOGIN);
        Long sizeOfStorageBeforeOperation = userRepository.count();
        try {
            mockMvc.perform(post(URL_USERS).content(userStub));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long sizeOfStorageAfterOperation = userRepository.count();
        assertTrue(sizeOfStorageBeforeOperation < sizeOfStorageAfterOperation);
        User receivedFromStorage = userRepository.findByLogin(SOME_LOGIN);
        assertNotNull(receivedFromStorage);
    }

    @Test
    public void addCoupleUsersWithTheSameNames_whenStoredOnlyTheFirst_thenCorrect() {
        String oneOfCoupleWithDuplicatedLogin = getUserStub(SOME_LOGIN);
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
        getRegisteredAndAuthenticated(SOME_LOGIN);
        assertTrue(activeUserStore.users.contains(SOME_LOGIN));
    }

    @Test
    public void sendingGetStatus_whenServerRespondsWithIsAuthenticated_thenCorrect() throws UnsupportedEncodingException, JSONException {
        MvcResult resultWhenNotAuthenticated = null;
        try {
            resultWhenNotAuthenticated = mockMvc.perform(get(URL_STATUS))
                                         .andDo(MockMvcResultHandlers.print())
                                         .andExpect(cookie().doesNotExist("welcome"))
                                         .andReturn();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String responseWhenNotAuthenticated = resultWhenNotAuthenticated != null? resultWhenNotAuthenticated.getResponse().getContentAsString() : "";

        assertNotEquals(SOME_LOGIN, new JSONObject(responseWhenNotAuthenticated).getString("username"));
        assertFalse(Boolean.parseBoolean(new JSONObject(responseWhenNotAuthenticated).getString("isAuthenticated")));
        try {
                    getRegisteredAndAuthenticated(SOME_LOGIN)
                   .andExpect(cookie().exists("welcome"))
                   .andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MvcResult resultWhenAuthenticated = null;
        try {
            resultWhenAuthenticated = mockMvc
                    .perform(get(URL_STATUS))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String responseWhenAuthenticated = resultWhenAuthenticated != null? resultWhenAuthenticated.getResponse().getContentAsString() : "";
        String usernameFromResponse = new JSONObject(responseWhenAuthenticated).getString("username");
        boolean isAuthenticated = Boolean.parseBoolean(new JSONObject(responseWhenAuthenticated).getString("isAuthenticated"));
        assertEquals(SOME_LOGIN, usernameFromResponse);
        assertTrue(isAuthenticated);
    }

    @Test
    public void patchExistingUser_whenNewStateIsFoundStored_thenCorrect() throws Exception {
       getRegisteredAndAuthenticated(SOME_LOGIN)
               .andExpect(cookie().exists("welcome"))
               .andExpect(status().isOk());

       User currentTestUser = userRepository.findByLogin(SOME_LOGIN);
       Long id = currentTestUser.getId();
       String mockUserForPatch = "{\"id\":" + id + ",\"firstName\":\"AnotherFirstName\",\"lastName\":\"AnotherLastName\",\"login\":\"AnotherLogin\",\"password\":\"password\"}";
        try {
            mockMvc.perform(patch(URL_USERS + "/" + id).content(mockUserForPatch));
        } catch (Exception e) {
            e.printStackTrace();
        }
        User patchedUser = userRepository.findByLogin("AnotherLogin");
        assertEquals(id, patchedUser.getId());
        assertNotEquals(currentTestUser.getLogin(), patchedUser.getLogin());
        assertEquals("AnotherLogin", patchedUser.getLogin());
    }

    @Test
    public void patchExistingUserWithTryingToChangeRoles_whenAdminCanEditRoles_AndRegularUserCanNot_thenCorrect() throws Exception {
        try {
            mockMvc.perform(post(URL_USERS).content(getUserStub("OffBeatUser")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        getRegisteredAndAuthenticated(SOME_LOGIN)                       //regular user session
                .andExpect(cookie().exists("welcome"))
                .andExpect(status().isOk());
        User regularUserBeforePatch = userRepository.findByLogin(SOME_LOGIN);
        Long regularUserId = regularUserBeforePatch.getId();
        assertNotNull(regularUserBeforePatch.getRoles());
        assertTrue(regularUserBeforePatch.getRoles().stream().anyMatch((role)->"ROLE_USER".equals(role.getAuthority())));
        assertFalse(regularUserBeforePatch.getRoles().stream().anyMatch((role)->"ROLE_ADMIN".equals(role.getAuthority())));
        String regularUserForPatch = "{\"id\":" + regularUserId + ",\"firstName\":\"RegularUserFirstName\",\"lastName\":\"RegularUserLastName\",\"login\":\"RegularUserLogin\",\"password\":\"password\",\"roles\":\"null\"}";
        try {
            mockMvc.perform(patch(URL_USERS + "/" + regularUserId).content(regularUserForPatch));
        } catch (Exception e) {
            e.printStackTrace();
        }
        User patchedRegularUser = userRepository.findById(regularUserId).get();
        assertNotNull(patchedRegularUser.getRoles());
        assertTrue(patchedRegularUser.getRoles().stream().anyMatch((role)->"ROLE_USER".equals(role.getAuthority())));
        assertFalse(patchedRegularUser.getRoles().stream().anyMatch((role)->"ROLE_ADMIN".equals(role.getAuthority())));
        for (Role role : regularUserBeforePatch.getRoles()) {
            assertTrue(patchedRegularUser.getRoles().stream().anyMatch((r)->(role.getAuthority()).equals(r.getAuthority())));
        }
        try {
            mockMvc.perform(get(URL_LOGOUT))                           //logging out as reg. user
                    .andExpect(cookie().doesNotExist("welcome"))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
        }                                                              //admin session
        mockMvc.perform(post(URL_LOGIN).param("username","test").param("password", "test"))
                .andExpect(cookie().exists("welcome"))
                .andExpect(status().isOk());
        User adminBeforePatch = userRepository.findByLogin("test");
        Long adminId = adminBeforePatch.getId();
        assertNotNull(adminBeforePatch.getRoles());
        assertTrue(adminBeforePatch.getRoles().stream().anyMatch((role)->"ROLE_ADMIN".equals(role.getAuthority())));
        String adminForPatch = "{\"id\":" + adminId + ",\"firstName\":\"AdminFirstName\",\"lastName\":\"AdminLastName\",\"login\":\"AdminLogin\",\"password\":\"password\",\"roles\":null}";
        try {
            mockMvc.perform(patch(URL_USERS + "/" + adminId).content(adminForPatch));
        } catch (Exception e) {
            e.printStackTrace();
        }
        User patchedAdmin = userRepository.findByLogin("AdminLogin");
        assertNotNull(patchedAdmin.getRoles());
        assertFalse(patchedAdmin.getRoles().stream().anyMatch((role)->"ROLE_ADMIN".equals(role.getAuthority())));
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
    private ResultActions getRegisteredAndAuthenticated(String username) {
        ResultActions res = null;
        try {
            mockMvc.perform(post(URL_USERS).content(getUserStub(username)));
            res = mockMvc.perform(post(URL_LOGIN).param("username",username).param("password", "password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

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
