package com.songtec;

public class ChatMessage {

    int idGroup;

    String msgType;

    String chatText;

    String userEmail;

    String userName;

    String userProfileUrl;

    String chatTime;

    public ChatMessage(int idGroup, String msgType, String chatText, String userEmail, String userName, String userProfileUrl, String chatTime) {
        this.idGroup = idGroup;
        this.msgType = msgType;
        this.chatText = chatText;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userProfileUrl = userProfileUrl;
        this.chatTime = chatTime;
    }
}
