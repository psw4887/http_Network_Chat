package com.nhnacademy;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class MyClient {
    @Parameter(names={"ip"})
    static
    String ip;

    @Parameter(names={"port"})
    static
    int port;

    public static void main(String[] args) {
        MyClient client = new MyClient();
        JCommander.newBuilder()
            .addObject(client)
            .build()
            .parse(args);
        client.connect(ip, port);
    }

    private void connect(String serverHost, int port) {
        try {
            Socket socket = new Socket(serverHost, port);
            System.out.println("Connect to Server");
            Thread sender = new Sender(socket);
            Thread receiver = new Receiver(socket);
            sender.start();
            receiver.start();
        } catch (IOException e) {
            System.out.println(e);
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

    private static class Receiver extends Thread {
        private final DataInputStream in;

        private Receiver(Socket socket) throws IOException {
            this.in = new DataInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (isReceivable()) {
                receiveMessage();
            }
        }

        private boolean isReceivable() {
            return this.in != null;
        }

        private void receiveMessage() {
            try {
                System.out.println(in.readUTF());
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
