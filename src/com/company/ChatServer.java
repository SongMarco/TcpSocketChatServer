package com.company;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;


//채팅 서버의 코드다. 인텔리제이 환경에서 디버그했고,
//리눅스 서버에서 javac로 컴파일 후 구동한다.

//외부 라이브러리로 json-simple.jar 를 사용하였다.
//이 파일은 자바 환경에서 json-simple을 사용하기 위한 라이브러리다.

/*
컴파일 방법

1. 터미널에서 javac 로 컴파일

    아래 명령어로 자바 파일을 컴파일 한다. 이 때,
    같은 폴더에 json-simple jar 파일을 같이 두어야 컴파일이 된다.


    >> javac -d . -cp "json-simple-1.1.1.jar" ChatServer.java

    명령어 해석
    javac : 자바 컴파일
    -d : 패키징 컴파일 예약어(패키지를 쓸 것이다)
    . : 현재 디렉토리에서 컴파일

    -cp "XXX" : "XXX"로 클래스 패스 지정
    ChatServer.java : 이 자바 파일을 컴파일 함

    정리하면 -> ChatServer.java 자바 파일을, "json-simple-1.1.1.jar" 클래스 패스와 함께
    현재 폴더에서 패키징 컴파일을 해라.


대체 명령어

    >> javac -d . -cp "./*" ChatServer.java
    해석 : 현재 폴더에서 ChatServer.java 와 다른 모든 jar 파일의 클래스를 패키징 컴파일 하라
    (jar 클래스 패스를 추가함)


2. java 실행 -> json-simple 라이브러리 jar와 함께 실행

    >> java -cp ".:json-simple-1.1.1.jar" com.company.ChatServer

    해석 : 자바 실행, 클래스 패스를 현재 폴더(.) 와, json-simple-1.1.1.jar 로 명시.
    "" 내부의 각 클래스 패스는 : 글자로 구분한다. "" 내부 내용은 띄어쓰기가 금지됨

    정리하면 -> 현재 폴더와 json-simple-1.1.1.jar 를 클래스 패스로 하여, com.company.ChatServer 를 실행하라

아래 명령어로도 실행이 가능

    >> java -cp ".:./*" com.company.ChatServer

    해석 : 현재 폴더와 현재 폴더의 다른 모든 jar 클래스 패스를 클래스 패스로 지정하고, ChatServer 실행하라





서버는 클라이언트와 json 구조의 메세지를 주고 받게 된다.

아래는 json 메세지의 구조다.

	1. 그룹 id : 방 구별에 사용
	2. 메세지 타입 : 방 입장, 방 나감, 채팅 메시지, 채팅 잠시 나감(화면 전환), 잠시 나감-> 재개
	3. 채팅 내용(채팅 메시지 타입일 때만 사용됨)
	4. 유저 이메일 ( 유저 구분에 id 처럼 사용 )
	5. 유저 이름 (클라이언트 채팅 창에 표시됨)
	6. 유저 프로필 URL (클라이언트에서 이미지 표시)
	7. 메세지 작성 시간 (클라이언트에서 시간 표시)



 */


public class ChatServer {


    //서버에 접속한 클라이언트의 해쉬맵 : 유저 이메일 - 아웃풋 스트림으로 구성됨
    HashMap<String, DataOutputStream> clients;


    //서버 소켓
    private ServerSocket serverSocket = null;

    //메인 메소드 - 챗 서버를 구동한다
    public static void main(String[] args) {
        new ChatServer().start();
    }


    //채팅 서버의 생성자
    public ChatServer() {

        // 클라이언트 해쉬맵 생성자(Key, value) 선언

        //유저의 이름과, 해당 유저가 생성한 소켓의 아웃풋 스트림으로 구성됨
        clients = new HashMap<String, DataOutputStream>();

//        roomManager = new RoomManager();

        // clients 동기화
        Collections.synchronizedMap(clients);

    }

    private void start() {

        // 9999 포트로 서버 연결
        int port = 9999;

        //소켓 초기화
        Socket socket = null;

        try {
            // 서버소켓 생성후 while문으로 진입하여 accept(대기)하고 접속시 ip주소를 획득하고 출력한뒤
            // MultiThread를 생성한다.
            serverSocket = new ServerSocket(port);

            //서버가 대기중이라는 메세지를 출력
            System.out.println("서버가 접속 대기중입니다 ...");

            //서버가 클라이언트로부터의 연결을 기다린다.
            while (true) {

                // 클라이언트로부터의 소켓 생성을 listen 한다.
                socket = serverSocket.accept();

                //클라이언트의 ip 값을 가져온다.
                InetAddress ip = socket.getInetAddress();

                //ip 값을 출력한다.
                System.out.println("ip 주소 +" + ip + "로 사용자가 연결되었습니다.");

                //해당 클라이언트의 메시지를 수신하기 위한 서버의 스레드를 구동
                new ServerChatThread(socket).start();
            }
        } catch (IOException e) {
//            System.out.println("뭔가 이상하다");
            System.out.println(e);
        }
    }

//    class ChatRoom {
//
//        //그룹 id값
//        int idGroup;
//
//        //채팅 방의 유저 해쉬맵. 유저 이메일-아웃풋 스트림으로 짝지어짐.
//        HashMap<String, DataOutputStream> roomUsers;
//
//        // 채팅방의 생성자 : id를 기입하고, 유저 목록 초기화
//        public ChatRoom(int idGroup){
//
//            this.idGroup = idGroup;
//            this.roomUsers = new HashMap<String, DataOutputStream>();
//
//        }
//
//        //방에 유저가 들어왔을 때 해쉬맵에 추가
//        public void addUser(String userEmail, DataOutputStream outputStream){
//
//            roomUsers.put(userEmail, outputStream );
//
//        }
//
//        //방에서 유저가 나갈 때 해쉬맵에서 제거
//        //방에 유저가 들어왔을 때 해쉬맵에 추가
//        public void outUser(String userEmail){
//
//            roomUsers.remove(userEmail);
//
//        }
//
//        public void printInfo(){
//
//            System.out.println("idGroup = "+idGroup);
//
//            Iterator<String> iterator = roomUsers.keySet().iterator();
//// 반복자를 이용해서 출력
//            while (iterator.hasNext()) {
//                String key = (String)iterator.next(); // 키 얻기
//                System.out.print("key="+key+" / value="+roomUsers.get(key));  // 출력
//            }
//
//        }
//
//
//
//    }


    //서버가 클라이언트로부터 메시지를 수신하기 위한 스레드

    /*
    채팅방 한 곳에서, 한 명의 유저로부터 메세지를 수신하기 위한 하나의 스레드다.

    방에서 유저가 나간다면 클라이언트에서 소켓을 닫게 되어 ioexception 이 발생하며,
    방 객체에서 유저 객체를 제거하여, 퇴장 처리한다.
     */

    class ServerChatThread extends Thread {

        //클라이언트와 연결되는 소켓
        Socket socket = null;

        int idGroup;
        //채팅 데이터 관련 정보들 : 메시지 타입, 유저


        ChatMessage chatMessage;


        //소켓에서 생성된 스트림.
        DataInputStream input; //서버로 들어오는 스트림
        DataOutputStream output; // 서버에서 클라이언트로 가는 스트림

        //서버 스레드의 생성자. 처음 사용자의 메시지를 수신할 때 생성한 소켓을 받아 스트림 지정
        public ServerChatThread(Socket socket) {
            this.socket = socket;


            try {
                // 해당 클라이언트와 데이터를 주고받을 스트림을 생성한다.
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //클라이언트에서 서버로 들어온
        //문자열 형태의 json 에서 필요한 데이터(방 정보, 유저 정보 등)를 추출하는 메소드
        public ChatMessage parseJson(String jsonString) {


            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) jsonParser.parse(jsonString);

//
//                String msgType;
//
//                String chatText;
//
//                String userEmail;
//
//                String userName;
//
//                String userProfileUrl;
//
//                String chatTime;

                int idGroup = Integer.parseInt(jsonObject.get("idGroup").toString());

                String msgType = jsonObject.get("msgType").toString();

                String chatText = jsonObject.get("chatText").toString();

                String userEmail = jsonObject.get("userEmail").toString();

                String userName = jsonObject.get("userName").toString();

                String userProfileUrl = jsonObject.get("userProfileUrl").toString();

                String chatTime = jsonObject.get("chatTime").toString();


                System.out.println("변환한 json : " + jsonObject.toJSONString());

                return new ChatMessage(idGroup, msgType, chatText, userEmail, userName, userProfileUrl, chatTime);

            } catch (ParseException e) {
                e.printStackTrace();

                return null;
            }


        }

        //서버 스레드를 동작시킨다. : 클라이언트가 보내온 메세지를 받고, 접속한 클라이언트들에게 메세지를 전달한다.
        public void run() {

            try {
                // 최초 접속 : 사용자의 메세지를 출력
                String jsonMsg = input.readUTF();
                System.out.println("최초 수신 메시지 : " + jsonMsg);

                //json 메세지를 파싱하여 채팅 정보 변수들에 세팅함.
                //채팅 정보 변수들에는 방 번호, 메세지 타입, (채팅일 경우) 채팅 내용, 유저 이메일, 유저 이름, 유저 사진 url, 채팅 시간이 있다.
                chatMessage = parseJson(jsonMsg);

                // @@@@@ 유저가 방에 접속했음
                // 방이 없다면 방을 생성하고, 방에 유저를 등록함

                idGroup = chatMessage.idGroup;

                //해당 그룹 id 를 가진 채팅방이 없다면
                if (RoomManager.getRoomById(idGroup) == null) {

                    //해당 방 id로 새 방을 생성하고, 유저를 참가시킨다.
                    ChatRoom chatRoom = RoomManager.createRoomById(idGroup);

                    //방에 포함시킬 유저 객체를 생성
                    ChatUser chatUser = new ChatUser(chatMessage.userEmail, chatMessage.userName, this.socket);

                    //방에 유저를 참가시킨다.
                    assert chatRoom != null;
                    chatRoom.enterUser(chatUser);


                    //방 정보 확인을 위해 터미널 상에 출력하게 한다.
                    //방 정보에는 방 id, 유저의 이메일, 소켓, 이름이 있다.
                    chatRoom.printRoomInfo();

                }
                // 이미 해당 채팅방이 존재한다면
                else {

                    //해당 방을 룸매니저로 가져온다.
                    ChatRoom chatRoom = RoomManager.getRoomById(idGroup);

                    //방에 포함시킬 유저 객체를 생성
                    ChatUser chatUser = new ChatUser(chatMessage.userEmail, chatMessage.userName, this.socket);

                    //유저를 방에 참가시킨다.
                    if (chatRoom != null) {
                        chatRoom.enterUser(chatUser);
                    }

                    //방 정보 확인을 위해 터미널 상에 출력하게 한다.
                    //방 정보에는 방 id, 유저의 이메일, 소켓, 이름이 있다.
                    chatRoom.printRoomInfo();
                }


//                clients.put(userEmail, output);


//                sendMsg(chatMessage.userEmail + "   접속");


                // 방 접속 완료 후 클라이언트와 메세지 송수신하도록 무한루프. -> 유저가 방에서 나가면 io exception 으로 들어감
                while (input != null) {
                    //클라이언트가 보낸 메세지를 읽어들인다.
                    String json2 = input.readUTF();
                    System.out.println("서버에서 보낼 메시지 = " + json2);

                    //json 메세지를 파싱하여 채팅 정보 변수들에 세팅함.
                    //채팅 정보 변수들에는 방 번호, 메세지 타입, (채팅일 경우) 채팅 내용, 유저 이메일, 유저 이름, 유저 사진 url, 채팅 시간이 있다.
                    ChatMessage chatMessage2 = parseJson(json2);

                    //방 번호를 통해 방 객체를 가져온다.
                    ChatRoom chatRoom = RoomManager.getRoomById(chatMessage2.idGroup);

                    //방에 있는 유저들에게 메세지를 보낸다.
                    sendRoomMsg(chatRoom, json2);
//                    sendMsg(json2);


                }
            }

            //사용자가 방을 나가게 될 경우, 아래의 io exception 에 걸리게 된다.
            catch (IOException e) {

                System.out.println("소켓에 쓰기 실패! 방 인원 에서 해당 사용자 제거하세요");

                //해당 방을 룸매니저로 가져온다.
                ChatRoom chatRoom = RoomManager.getRoomById(idGroup);

                //방에서 퇴장시킬 유저 객체를 가져옴
                ChatUser chatUser = chatRoom.getUserMap().get(chatMessage.userEmail);

                //유저를 방에서 퇴장 처리함
                chatUser.exitRoom(chatRoom);
//                //확인을 위해 룸 정보를 출력
                chatRoom.printRoomInfo();


//                e.printStackTrace();
            }
        }

        // 메세지수신후 클라이언트에게 Return 할 sendMsg 메소드

        //방나누기 할 때 고쳐야겠네!
        private void sendMsg(String msg) {


            // clients의 Key값을 받아서 String 배열로선언
            Iterator<String> iterator = clients.keySet().iterator();

            // Return 할 key값이 없을때까지
            while (iterator.hasNext()) {
                try {

                    OutputStream dos = clients.get(iterator.next());
                    // System.out.println(msg);

                    //아웃풋 스트림을 통해 메시지를 보낸다 -> 채팅 내용이 서버에서 클라이언트로 전달된다.
                    DataOutputStream output = new DataOutputStream(dos);
                    output.writeUTF(msg);

                } catch (IOException e) {
                    System.out.println(e);
                }
            }


        }


        //같은 채팅방 유저들에게 메세지를 보내는 메소드
        private void sendRoomMsg(ChatRoom room, String msg) {


            HashMap<String, ChatUser> userMap = room.getUserMap();


            // 유저 목록을 순환하며, 필요한 요소를 제거하기 위해 iterator 사용
            Iterator<String> iterator = userMap.keySet().iterator();

            // Return 할 key값이 없을때까지
            while (iterator.hasNext()) {
                try {

                    ChatUser chatUser = userMap.get(iterator.next() );

                    //소켓에서 아웃풋스트림을 가져와 세팅한다.
                    DataOutputStream output = new DataOutputStream(chatUser.getSocket().getOutputStream());

                    //아웃풋 스트림에 메세지를 보낸다.
                    output.writeUTF(msg);

                }

                //사용자가 방을 나간 경우 해당 exception 에 들어온다.
                catch (IOException e) {

                    System.out.println(e);
                }
            }




//            for (int i = 0; i < userList.size(); i++) {
//
//
//                try {
//                    ChatUser chatUser = userList.get(i);
//
//                    //소켓에서 아웃풋스트림을 가져와 세팅한다.
//                    DataOutputStream output = new DataOutputStream(chatUser.getSocket().getOutputStream());
//
//                    //아웃풋 스트림에 메세지를 보낸다.
//                    output.writeUTF(msg);
//
//                }
//                //이미 닫힌 소켓에 메세지를 보내려 했다. 해당 유저를 방에서 제거한다.
//                catch (IOException e) {
//
//                    System.out.println("소켓에 쓰기 실패");
//
//
//                    e.printStackTrace();
//                }
//
//
//            }
        }


    }


}
