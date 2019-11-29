package info.security.java_pki.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "messages")
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Date date = new Date();
    private String content;
    private byte[] encrypted_aes_key;
    private String sender;
    private String receiver;

    @Transient
    private String senderfullname;
    @Transient
    private String receiverfullname;

    //region get-set
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }



    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getEncrypted_aes_key() {
        return encrypted_aes_key;
    }

    public void setEncrypted_aes_key(byte[] encrypted_aes_key) {
        this.encrypted_aes_key = encrypted_aes_key;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSenderfullname() {
        return senderfullname;
    }

    public void setSenderfullname(String senderfullname) {
        this.senderfullname = senderfullname;
    }

    public String getReceiverfullname() {
        return receiverfullname;
    }

    public void setReceiverfullname(String receiverfullname) {
        this.receiverfullname = receiverfullname;
    }

    //endregion
}
