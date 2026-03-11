package com.revworkforce.admin.dto;

public class NotificationSendRequest {

    private String message;
    private Boolean sendToAll;
    private Long recipientUserId;

    public String getMessage() {
        return message;
    }

    public Boolean getSendToAll() {
        return sendToAll;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSendToAll(Boolean sendToAll) {
        this.sendToAll = sendToAll;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }
}
