package com.example.travellens;

import com.parse.ParseFile;

public class Message {
    private String sender;
    private String receiver;
    private String message;
    private String dateAndTime;
    private String photo = null;

    public Message(){}

    public Message(String sender, String receiver, String message, String dateAndTime, String photo) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.dateAndTime = dateAndTime;
        this.photo = photo;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
