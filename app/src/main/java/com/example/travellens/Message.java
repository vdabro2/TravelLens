package com.example.travellens;

public class Message {
    private String sender;
    private String receiver;
    private String message;
    private String photoUrl;
    private String dateAndTime;

    public Message(){}
    public Message(String sender, String receiver, String message, String dateAndTime) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.dateAndTime = dateAndTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }
}
