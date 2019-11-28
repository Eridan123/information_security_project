package info.security.java_pki.repository;

import info.security.java_pki.model.Messages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessagesRepository extends JpaRepository<Messages,Long> {
    List<Messages> findAllByReceiverAndSenderOrSenderAndReceiver(String receiver, String sender, String sender1, String receiver1);
}
