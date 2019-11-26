package si.inspirited;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
class ContactsApplicationTests {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Test
	void contextLoads() {
		assertNotNull(webApplicationContext);
	}
}
