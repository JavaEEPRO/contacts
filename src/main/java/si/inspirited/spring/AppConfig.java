package si.inspirited.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import si.inspirited.security.ActiveUserStore;

@Configuration
public class AppConfig {

    @Bean
    public ActiveUserStore activeUserStore() {
        return new ActiveUserStore();
    }
}