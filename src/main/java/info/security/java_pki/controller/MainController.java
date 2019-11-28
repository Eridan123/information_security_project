package info.security.java_pki.controller;

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
import org.springframework.beans.propertyeditors.CustomDateEditor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
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
        public Messages sendMessage(@Payload Messages chatMessage, Principal principal)
        {
            Object o = principal;
            chatMessage.setSenderfullname(userRepository.findUserByUsername(chatMessage.getSender()).getUsername());
            chatMessage.setReceiverfullname(userRepository.findUserByUsername(chatMessage.getReceiver()).getUsername());
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
        public List<Messages> getMessages(@RequestParam String sender, @RequestParam String receiver)
        {
            return messagesRepository.findAllByReceiverAndSenderOrSenderAndReceiver(receiver,sender,receiver,sender);
        }
}
