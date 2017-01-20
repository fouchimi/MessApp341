package com.social.messapp34.model;

import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by ousmane on 1/18/17.
 */

public class Conversation {

    public static final int STATUS_SENDING = 0;

    public static final int STATUS_SENT = 1;

    public static final int STATUS_FAILED = 2;

    private String msg;

    private Date date;

    private int status = STATUS_SENT;

    public String senderId;

    public Conversation(){}

    public Conversation(String msg, Date date, String senderId){
        this.msg = msg;
        this.date = date;
        this.senderId = senderId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isSent(){
        return ParseUser.getCurrentUser().getObjectId().equals(senderId);
    }
}
