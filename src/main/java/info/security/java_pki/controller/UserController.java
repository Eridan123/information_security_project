package info.security.java_pki.controller;

import info.security.java_pki.model.User;
import info.security.java_pki.repository.RoleRepository;
import info.security.java_pki.repository.UserRepository;
import info.security.java_pki.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    RoleRepository roleRepository;

    @GetMapping("/login")
    public String login(Model model){

        return "/signin";
    }

    @GetMapping("/signup")
    public String registerGet(Model model){

        model.addAttribute("user", new User());

        return "/signup";
    }

    @PostMapping("/signup")
    public String registerPost(User user){

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        userRepository.save(user);
        return "redirect:/login";
    }
}
