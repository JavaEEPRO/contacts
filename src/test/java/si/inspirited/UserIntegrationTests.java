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
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.UserRepository;

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

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    public void addUser_whenCouldBeFoundInStorage_thenCorrect() {
        assertNotNull(mockMvc);
        final String uri = "/users";
        String userStub = "{\"login\":\"Name\",\"password\":\"password\"}";
        Long sizeOfStorageBeforeOperation = userRepository.count();
        try {
            mockMvc.perform(post(uri).content(userStub));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long sizeOfStorageAfterOperation = userRepository.count();
       assertTrue(sizeOfStorageBeforeOperation < sizeOfStorageAfterOperation);
       User receivedFromStorage = userRepository.findByLogin("Name");
       assertNotNull(receivedFromStorage);
    }
}
