package com.songtec;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

class RoomManager {


    //현재 룸 목록을 해쉬맵으로 사용 중이므로, 추후 삭제할 것.

    private static HashMap<Integer, ChatRoom> roomMap;
    private static AtomicInteger atomicInteger;

    static {
        atomicInteger = new AtomicInteger();
        roomMap = new HashMap<Integer, ChatRoom>();
    }

    public RoomManager() {

    }

    /**
     * 빈 룸을 생성
     *
     * @return ChatRoom
     */
//    public static ChatRoom createRoom() { // 룸을 새로 생성(빈 방)
//        int roomId = atomicInteger.incrementAndGet();// room id 채번
//        ChatRoom room = new ChatRoom(roomId);
//
//
//        System.out.println("Room Created!");
//        return room;
//    }


    public static ChatRoom createRoomById(int roomId) { // 룸을 새로 생성(빈 방)

        ChatRoom room = new ChatRoom(roomId);

        roomMap.put(roomId, room);

        System.out.println("Room " + roomId + " Created!");
        return room;
    }


    public static ChatRoom getRoom(ChatRoom chatRoom) {

        return roomMap.get(chatRoom.getId());

    }

    // 그룹 id 값으로 그룹을 가져오는 메소드
    public static ChatRoom getRoomById(int roomId) {


        // 그룹 리스트로부터 그룹을 가져온다면
        if (roomMap.get(roomId) != null) {
            return roomMap.get(roomId);

        } else {
            return null;

        }
    }


    /**
     * 전달받은 룸을 제거
     *
     * @param room 제거할 룸
     */
    public static void removeRoom(ChatRoom room) {
        room.close();
        roomMap.remove(room.getId());

        System.out.println("Room Deleted!");
    }

    /**
     * 방의 현재 크기를 리턴
     *
     * @return 현재 size
     */
    public static int roomCount() {
        return roomMap.size();
    }
}
