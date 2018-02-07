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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;


//채팅 서버의 코드다. 인텔리제이 환경에서 디버그했고,
//리눅스 서버에서 javac로 컴파일 후 구동한다.

/*
컴파일 방법

1. 터미널에서 javac 로 컴파일

>> javac -d . -cp "json-simple-1.1.1.jar" ChatServer.java

명령어 해석
javac : 자바 컴파일
-d : 패키징 컴파일 예약어(패키지를 쓸 것이다)
. : 현재 디렉토리에서 컴파일

-cp "XXX" : "XXX"로 클래스 패스 지정
ChatServer.java : 이 자바 파일을 컴파일 함

정리하면 -> ChatServer.java 자바 파일을, "json-simple-1.1.1.jar" 클래스 패스와 함께
현재 폴더에서 패키징 컴파일을 해라.


2. java 실행 -> json-simple 라이브러리 jar와 함께 실행

>> java -cp ".:json-simple-1.1.1.jar" com.company.ChatServer

해석 : 자바 실행, 클래스 패스를 현재 폴더(.) 와, json-simple-1.1.1.jar 로 명시.
"" 내부의 각 클래스 패스는 : 글자로 구분한다. "" 내부 내용은 띄어쓰기가 금지됨

정리하면 -> 현재 폴더와 json-simple-1.1.1.jar 를 클래스 패스로 하여, com.company.ChatServer 를 실행하라



 */



public class ChatServer {


    //서버에 접속한 클라이언트의 해쉬맵 : 유저 이메일 - 아웃풋 스트림으로 구성됨
    HashMap<String, DataOutputStream> clients;

    //채팅방의 해쉬맵. 방 id값 - 방 객체로 이루어짐
//    HashMap<Integer, ChatRoom> rooms;

    //유저 정보 리스트 생성
    ArrayList<UserInfo> listUser;


    //채팅방 id
    int idGroup;



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

        listUser = new ArrayList<UserInfo>();
//        rooms = new HashMap<Integer, ChatRoom>();


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
                socket = serverSocket.accept();

                //클라이언트의 ip 값을 가져온다.
                InetAddress ip = socket.getInetAddress();

                //ip 값을 출력한다.
                System.out.println("ip 주소 +"+ip + "로 사용자가 연결되었습니다.");

                //해당 클라이언트를 위한 스레드를 구동
                new ServerChatThread(socket).start();
            }
        } catch (IOException e) {
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

    class UserInfo {

        //그룹 id
        int idGroup;

        //유저 이메일
        String emailUser;

        //데이터 아웃스트림림
        DataOutputStream dataOutputStream;

        public UserInfo(int idGroup, String emailUser, DataOutputStream dataOutputStream) {
            this.idGroup = idGroup;
            this.emailUser = emailUser;
            this.dataOutputStream = dataOutputStream;
        }

    }


    class ServerChatThread extends Thread {

        Socket socket = null;

        String emailUser = null;


        DataInputStream input;
        DataOutputStream output;

        //멀티스레드의 생성자
        public ServerChatThread(Socket socket) {
            this.socket = socket;


            try {
                // 객체를 주고받을 Stream생성자를 선언한다.
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
            }
            catch (IOException e) {
            }
        }


        //서버 스레드를 동작시킨다. : 클라이언트가 보내온 메세지를 받고, 접속한 클라이언트들에게 메세지를 전달한다.
        public void run() {

            try {
                // 최초 접속 : 사용자의 이메일을 받아와 출력하고, 접속한 클라이언트들에게 해당 이메일을 전송
                emailUser = input.readUTF();
                System.out.println("사용자 이메일 : " + emailUser);

                clients.put(emailUser, output);
                System.out.println(emailUser+"유저의 OutputStream : " + clients.get(emailUser) );

                sendMsg(emailUser + "   접속");



                // 그후에 채팅메세지수신시
                while (input != null) {
                    try {
                        //클라이언트가 보낸 메세지를 읽어들인다.
                        String tempMsg = input.readUTF();



                        System.out.println("서버에서 보낼 메시지 = "+tempMsg);
                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonObject = (JSONObject) jsonParser.parse(tempMsg);


                        idGroup = Integer.parseInt( jsonObject.get("idGroup").toString() );


//                        //userInfo 리스트에 유저 정보가 없다면 추가하기
//                        if( isUserFirst()  ){
//                            UserInfo userInfo = new UserInfo(idGroup, emailUser, output);
//
//                            listUser.add(userInfo);
//
//                            System.out.println("유저가 추가되었습니다.");
//                        }


                        //서버에 접속한 클라이언트들에게 메세지를 보낸다 : 방나누기 할 경우 달라짐@@@
                        sendMsg(tempMsg);
//
//                        //방이 존재할 경우 그대로 유저를 추가하면 됨
//                        if(rooms.containsKey(idGroup) )
//                        {
//                            System.out.println("기존 방에 추가");
//
//
//                            //방 해쉬맵에서 방을 가져온다
//                            ChatRoom chatRoom = rooms.get(idGroup);
//
//                            //방에 유저 추가
//                            chatRoom.addUser(emailUser, output);
//
//                            chatRoom.printInfo();
//                        }
//                        //방이 존재하지 않음 -> 새 방을 생성 후 유저 추가
//                        else{
//
//
//                            System.out.println("새 방 생성");
//                            //새 방 생성
//                            ChatRoom chatRoom = new ChatRoom(idGroup);
//
//                            //방 해쉬맵에 방 추가
//                            rooms.put(idGroup, chatRoom);
//
//                            //방에 유저 추가
//                            chatRoom.addUser(emailUser, output);
//
//                            chatRoom.printInfo();
//                        }
//


//                        String chatText = jsonObject.get("chatText").toString();
//                        String chatWriterName = jsonObject.get("chatWriterName").toString();
//
//                        String chatTime = jsonObject.get("chatTime").toString();
//                        String chatWriterProfile =  jsonObject.get("chatWriterProfile").toString();

                        String chatWriterEmail = jsonObject.get("chatWriterEmail").toString();





//                        System.out.println("변환한 객체 = "+idGroup+chatText+chatWriterEmail+chatWriterName+chatText+chatTime+chatWriterProfile);





                    } catch (IOException e) {
                        sendMsg("No massege");
                        break;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        private boolean isUserFirst(){

            for(int i = 0 ; i < listUser.size(); i++){

                UserInfo userInfo = listUser.get(i);

                //방 id 같고 유저 동일 -> 추가 X
                if(  userInfo.idGroup == idGroup && userInfo.emailUser.equals(emailUser)){

                    return false;
                }


            }

            return true;


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

//            for(int i = 0; i < listUser.size(); i++){
//
//
//                //리스트에서 건진 아이템의 id와 그룹 id가 동일 -> 같은 방의 유저임. 메시지 보냄
//                if(listUser.get(i).idGroup == idGroup){
//
//
//                    try {
//                        DataOutputStream dataOutputStream = listUser.get(i).dataOutputStream;
//                        dataOutputStream.writeUTF(msg);
//                    }
//
//                    //쓰기가 안되는 상황 발생 -> 소켓이 종료된 것임.
//                    catch (IOException e)
//                    {
//                        e.printStackTrace();
//                    }
//
//
//                }
//
//
//
//
//
//            }






        }
    }
}
