package info.security.java_pki;

import info.security.java_pki.component.RSAKeyPairGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

@SpringBootApplication
public class JavaPkiApplication {

    public static PrivateKey privateKey;
    public static PublicKey publicKey;
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
        privateKey = keyPairGenerator.getPrivateKey();
        publicKey = keyPairGenerator.getPublicKey();
        SpringApplication application = new SpringApplication(JavaPkiApplication.class);
        application.setAdditionalProfiles("ssl");
        application.run(args);

    }

}
