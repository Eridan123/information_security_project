package info.security.java_pki.controller;

import info.security.java_pki.component.AES;
import info.security.java_pki.component.RSAUtil;
import info.security.java_pki.model.Messages;
import info.security.java_pki.model.User;
import info.security.java_pki.repository.MessagesRepository;
import info.security.java_pki.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.messaging.simp.annotation.SendToUser;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessagesRepository messagesRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        CustomDateEditor editor = new CustomDateEditor(new SimpleDateFormat("dd.MM.yyyy"), true);
        binder.registerCustomEditor(Date.class, editor);
    }



        @GetMapping("/")
        public String chatWindow(Model model){

            List<User> userList = userRepository.findAll();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            model.addAttribute("username",auth.getName());
            model.addAttribute("list",userList);

            return "/messenger";
        }

        @MessageMapping("/chat.sendMessageTo")
        @SendToUser("/queue/public")
        public Messages sendMessageTo(@Payload Messages chatMessage, Principal principal) throws Exception
        {
            chatMessage.setSenderfullname(userRepository.findUserByUsername(chatMessage.getSender()).getUsername());
            chatMessage.setReceiverfullname(userRepository.findUserByUsername(chatMessage.getReceiver()).getUsername());
            return chatMessage;
        }

        @MessageMapping("/chat.sendMessage")
        @SendTo("/topic/public")
        public Messages sendMessage(@Payload Messages chatMessage, Principal principal) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
            Object o = principal;
            User receiver = userRepository.findUserByUsername(chatMessage.getReceiver());
            chatMessage.setSenderfullname(userRepository.findUserByUsername(chatMessage.getSender()).getUsername());
            chatMessage.setReceiverfullname(receiver.getUsername());

            KeyGenerator keyGen = null;
            try {
                keyGen = KeyGenerator.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();


            String encryptedMessage = AES.encrypt(chatMessage.getContent(),Base64.getEncoder().encodeToString(secretKey.getEncoded()));
            byte[] encryptAesKey = RSAUtil.encrypt(Base64.getEncoder().encodeToString(secretKey.getEncoded()),receiver.getRsaPublicKey());

            chatMessage.setContent(encryptedMessage);
            chatMessage.setEncrypted_aes_key(encryptAesKey);

            messagesRepository.save(chatMessage);
            return chatMessage;
        }

        @MessageMapping("/chat.addUser")
        @SendTo("/topic/public")
        public Messages addUser(@Payload Messages chatMessage, SimpMessageHeaderAccessor headerAccessor)
        {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
            return chatMessage;
        }

        @RequestMapping("/chat/messages")
        @ResponseBody
        public List<Messages> getMessages(@RequestParam String sender, @RequestParam String receiver) throws GeneralSecurityException, IOException {
            User sentUser = userRepository.findUserByUsername(sender);
            User receivedUser = userRepository.findUserByUsername(receiver);

            KeyFactory kf = KeyFactory.getInstance("RSA"); // or "EC" or whatever

            List<Messages> messages = messagesRepository.findAllByReceiverAndSenderOrSenderAndReceiver(receiver,sender,receiver,sender);
            for (Messages message :messages){
                String encryptedMessage= "";
                if(message.getReceiver().equals(sender)){
                    String decryptAesKey = RSAUtil.decrypt(message.getEncrypted_aes_key(),loadPrivateKey(sentUser.getRsaPrivateKey()));
                    encryptedMessage = AES.decrypt(message.getContent(),decryptAesKey);
                }
                else{
                    String decryptAesKey = RSAUtil.decrypt(message.getEncrypted_aes_key(),loadPrivateKey(receivedUser.getRsaPrivateKey()));
                    encryptedMessage = AES.decrypt(message.getContent(),decryptAesKey);
                }

                message.setContent(encryptedMessage);
            }

            return messages;
        }

    public static PrivateKey loadPrivateKey(String key64) throws     GeneralSecurityException, IOException {
        byte[] clear = Base64.getDecoder().decode(key64.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;

    }
}
