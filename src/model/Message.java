package model;

/**
 * Created by radoslawjarzynka on 12.05.15.
 */
public class Message {
    private Integer id;
    private Integer userId;
    private Integer senderId;
    private String message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(Integer userId, Integer senderId, String message) {
        this.userId = userId;
        this.senderId = senderId;
        this.message = message;
    }
}
