package com.songtec;

import java.util.HashMap;
import java.util.Iterator;

class ChatRoom {

    private int id; // 룸 ID


    //서버에 접속한 클라이언트의 해쉬맵 : 유저 이메일 - 소켓
    private HashMap<String, ChatUser> userMap;


    private ChatUser roomOwner; // 방장
    private String roomName; // 방 이름

    public ChatRoom(int roomId) { // 아무도 없는 방을 생성할 때
        this.id = roomId;

        userMap = new HashMap<String, ChatUser>();
    }

//    public ChatRoom(ChatUser user) { // 유저가 방을 만들때
//        userList = new ArrayList();
//        user.enterRoom(this);
//        userList.add(user); // 유저를 추가시킨 후
//        this.roomOwner = user; // 방장을 유저로 만든다.
//    }

//    public ChatRoom(List users) { // 유저 리스트가 방을 생성할
//        this.userList = users; // 유저리스트 복사
//
//        // 룸 입장
//        for(ChatUser user : users){
//            user.enterRoom(this);
//        }
//
//        this.roomOwner = userList.get(0); // 첫번째 유저를 방장으로 설정
//    }

    public void enterUser(ChatUser user) {

        userMap.put(user.getUserEmail(), user);

    }

    /**
     * 해당 유저를 방에서 내보냄
     *
     * @param user 내보낼 유저
     */
    public void exitUser(ChatUser user) {

        userMap.remove(user.getUserEmail());

        if (userMap.size() < 1) { // 모든 인원이 다 방을 나갔다면
            RoomManager.removeRoom(this); // 이 방을 제거한다.
        }


    }

    /**
     * 해당 룸의 유저를 다 퇴장시키고 삭제함
     */
    public void close() {

        Iterator<String> iterator = userMap.keySet().iterator();

        while(iterator.hasNext()){

            //유저 맵에서 유저를 꺼내고, 퇴장 처리한다.
            ChatUser user = userMap.get(iterator.next());
            user.exitRoom(this);

        }

        this.userMap.clear();
        this.userMap = null;
    }

    // 게임 로직

    /**
     * 해당 byte 배열을 방의 모든 유저에게 전송
     *
     * @param data 보낼 data
     */
    public void broadcast(byte[] data) {

        Iterator<String> iterator = userMap.keySet().iterator();

        while(iterator.hasNext()){

            //유저 맵에서 유저를 꺼내고, 퇴장 처리한다.
            ChatUser user = userMap.get(iterator.next());
            // ex) user.SendData(data);
//            try {
//				user.sock.getOutputStream().write(data); // 이런식으로 바이트배열을 보낸다.
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        }

    }



    public void printRoomInfo() {


        System.out.println("*** 방" + getId() + " 의 정보입니다. ***");
        try{
            Iterator<String> iterator = userMap.keySet().iterator();

            while(iterator.hasNext()){

                //유저 맵에서 유저를 꺼내고, 퇴장 처리한다.
                ChatUser chatUser = userMap.get(iterator.next());
                System.out.print("id =" + chatUser.getUserEmail() + " // socket = " + chatUser.getSocket() + " // ");
                System.out.println(" name =" + chatUser.getUserName());
            }
            System.out.println("*** 방 정보 출력 완료 ***");
        }
        catch(NullPointerException e) {
            System.out.println("*** 방이 비었습니다. ***");
        }



    }

    public HashMap<String, ChatUser> getUserMap() {
        return userMap;
    }

    public int getUserSize() { // 유저의 수를 리턴
        return userMap.size();
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public ChatUser getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(ChatUser roomOwner) {
        this.roomOwner = roomOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoom chatRoom = (ChatRoom) o;

        return id == chatRoom.id;
    }

    @Override
    public int hashCode() {
        return id;
    }


}
