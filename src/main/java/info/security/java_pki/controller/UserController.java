package info.security.java_pki.controller;

import info.security.java_pki.component.AES;
import info.security.java_pki.component.RSAKeyPairGenerator;
import info.security.java_pki.component.RSAUtil;
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

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static info.security.java_pki.JavaPkiApplication.privateKey;
import static info.security.java_pki.JavaPkiApplication.publicKey;

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
    public String registerPost(User user) throws NoSuchAlgorithmException {


        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        user.setAesKey(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

        setPrivatePublicKeys(user);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        userRepository.save(user);
        return "redirect:/login";
    }

    private void setPrivatePublicKeys(User user) throws NoSuchAlgorithmException {

        RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();


        byte[] publicKey = rsaKeyPairGenerator.getPublicKey().getEncoded();
        byte[] privateKey = rsaKeyPairGenerator.getPrivateKey().getEncoded();

        //encoding keys to Base64 text format so that we can send public key via REST API
        String rsaPublicKeyBase64 = new String(Base64.getEncoder().encode(publicKey));
        String rsaPrivateKeyBase64 = new String(Base64.getEncoder().encode(privateKey));

        //saving keys to user object for later use
        user.setRsaPublicKey(rsaPublicKeyBase64);
        user.setRsaPrivateKey(rsaPrivateKeyBase64);
    }
}
