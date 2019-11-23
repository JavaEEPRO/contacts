package si.inspirited;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserIntegrationTests {

    @Autowired
    UserRepository userRepository;

    private RestTemplate restTemplate;
    private HttpHeaders headers;

    @Before
    public void setup() {
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    }

    @Test
    public void addUser_whenCouldBeFoundInStorage_thenCorrect() throws JsonProcessingException {
        final String uri = "http://127.0.0.1:8080/users";
        String input = "{\"login\":\"Name\",\"password\":\"password\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        Long sizeOfStorageBeforeOperation = userRepository.count();
        ResponseEntity<User> returnedUser = restTemplate.postForEntity(uri, new HttpEntity<>(objectMapper.writeValueAsString(input)), User.class);
        Long sizeOfStorageAfterOperation = userRepository.count();

       assertNotEquals(sizeOfStorageBeforeOperation, sizeOfStorageAfterOperation);
       assertNotNull(returnedUser);
    }
}
