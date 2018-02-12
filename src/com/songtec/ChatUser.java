package com.songtec;

import java.io.DataOutputStream;
import java.net.Socket;

class ChatUser {


    private int idUser; 			// Unique ID


    private String userEmail;
    private String userName;	// 닉네임

    //데이터 아웃스트림림
    private DataOutputStream dataOutputStream;


    private ChatRoom room; 		// 유저가 속한 룸이다.
    private Socket socket; 		// 소켓 object


    // 게임에 관련된 변수 설정
    // ...
    //




    public ChatUser() { // 아무런 정보가 없는 깡통 유저를 만들 때
    }

    /**
     * 유저 생성
     * @param userName 닉네임
     */
    public ChatUser(String userName) { // 닉네임 정보만 가지고 생성
        this.userName = userName;
    }

    /**
     * 유저 생성
     * @param idUser ID
     * @param userName 닉네임
     */
    public ChatUser(int idUser, String userName, DataOutputStream outputStream) { // UID, 닉네임 정보를 가지고 생성
        this.idUser = idUser;
        this.userName = userName;
        this.dataOutputStream = outputStream;
    }


    public ChatUser(String userEmail, String userName, Socket socket) { // UID, 닉네임 정보를 가지고 생성
        this.userEmail = userEmail;
        this.userName = userName;
        this.socket = socket;
    }

    /**
     * 방에 입장시킴
     * @param room  입장할 방
     */
    public void enterRoom(ChatRoom room) {

        room.enterUser(this); // 룸에 유저를 입장 처리함

    }

    /**
     * 방에서 퇴장
     * @param room 퇴장할 방
     */
    public void exitRoom(ChatRoom room){
        room.exitUser(this);

    }

    public String getUserEmail() {
        return userEmail;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }


    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    /*
        equals와 hashCode를 override 해줘야, 동일유저를 비교할 수 있다
        비교할 때 -> gameUser 간 equals 비교, list에서 find 등
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatUser chatUser = (ChatUser) o;

        return idUser == chatUser.idUser;
    }

    @Override
    public int hashCode() {
        return idUser;
    }
}
