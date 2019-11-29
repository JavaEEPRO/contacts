package si.inspirited.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import si.inspirited.persistence.model.User;
import si.inspirited.persistence.repositories.UserRepository;
import java.security.Principal;

@Controller
public class StatusController {

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public String isAuthenticated(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isEmpty()) { return getMessage(""); }
        User currentUser = userRepository.findByLogin(principal.getName());
        return getMessage(currentUser.getLogin());
    }

    private String getMessage(String name) {
        boolean isAuthenticated;
        if (name == null || "".equals(name)) { name = "null"; isAuthenticated = false; }
        else { name = "\"" + name + "\""; isAuthenticated = true; }
        return "{\"user\":" + name + ",\"isAuthenticated\":" + isAuthenticated + "}";
    }
}
