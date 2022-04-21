package com.nhnacademy;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class MyServer {
    @Parameter(names={"-l"})
    int port;

    private final ConcurrentHashMap<String, DataOutputStream>
        clientOutMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        MyServer server = new MyServer();
        JCommander.newBuilder()
            .addObject(server)
            .build()
            .parse(args);
        server.run();
    }

    private void run() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(" Start server " + serverSocket.getLocalSocketAddress());
            try {
                Socket socket;
                socket = serverSocket.accept();
                Thread sender = new Sender(socket);
                sender.start();
                ClientSession client = new ClientSession(socket);
                client.start();
            } catch (IOException e) {
                System.out.println("클라이언트 접속에 실패하였습니다!");
                Thread.currentThread().interrupt();
            }
        }
    }

    public void sendToAll(String message) {
        System.out.println(message);
        for (DataOutputStream out : clientOutMap.values()) {
            try {
                out.writeUTF(message);
            } catch (IOException e) {
                System.out.println("에러 : " + e);
                System.out.println("클라이언트 송출 스트림에 실패하였습니다!(네트워크 끊김");
            }
        }
    }

    class ClientSession extends Thread {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;

        ClientSession(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            connect();
        }

        private void connect() {
            try {
                while (isConnect()) {
                    sendToAll(in.readUTF());
                }
            } catch (IOException cause) {
                System.out.println("연결이 끊어졌습니다.");
            } finally {
                System.out.println("연결 종료");
            }
        }

        private boolean isConnect() {
            return this.in != null;
        }
    }

    public static class Sender extends Thread {
        private DataOutputStream out;
        public Sender(Socket socket) throws IOException {
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                sendMessage();
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        private boolean isSendable() {
            return this.out != null;
        }

        private void sendMessage() throws IOException {
            try (Scanner sc = new Scanner(System.in)) {
                while (isSendable()) {
                    this.out.writeUTF(sc.nextLine());
                }
            }
        }
    }
}
