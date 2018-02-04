package com.company;

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

public class ChatServer {


    //서버에 접속한 클라이언트의 해쉬맵
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
        clients = new HashMap<String, DataOutputStream>();

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
            System.out.println("접속대기중");

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

    class ServerChatThread extends Thread {

        Socket socket = null;

        String emailUser = null;
        String msg = null;

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
                sendMsg(emailUser + "   접속");

                // 그후에 채팅메세지수신시
                while (input != null) {
                    try {
                        //클라이언트가 보낸 메세지를 읽어들인다.
                        String tempMsg = input.readUTF();

                        //서버에 접속한 클라이언트들에게 메세지를 보낸다 : 방나누기 할 경우 달라짐@@@
                        sendMsg(tempMsg);

                        System.out.println(tempMsg);

                    } catch (IOException e) {
                        sendMsg("No massege");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
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
                    DataOutputStream output = new DataOutputStream(dos);
                    output.writeUTF(msg);

                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }
}
