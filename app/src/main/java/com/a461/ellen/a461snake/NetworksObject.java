package com.a461.ellen.a461snake;

import android.graphics.Point;

import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class NetworksObject {
    private int numPlayers;
    private Selector selector;
    private SocketChannel client;
    private ReadThread rt = null;
    private WriteThread wt = null;

    public NetworksObject(int n) {
        numPlayers = n;
        SocketChannel client = openConnection();
//        try {
//            new Thread(new NetworkThread(client)).start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    // open connection with server
    private SocketChannel openConnection() {
        // int result = 0;
        try {
            client = SocketChannel.open();
            InetSocketAddress server = new InetSocketAddress("localhost", 1111);
            client.connect(server);
            client.configureBlocking(false);
            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            return client;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // for setting up initial game--request server send positions of both
    // snakes and apple given the size of the board
    public void sendInitialGame(int rows, int cols) {
        String data = new String("r:" + rows + "\n" + "c:" + cols + "\r\n");
        System.out.println("message: \n" + data);

    }

    public void sendMoves(ArrayList<Point> snakePos, Point applePos, int score) {
        System.out.println("packaging data");
        JSONObject data = packageData(snakePos, applePos, score);
        System.out.println("sending data");
    }

    private JSONObject packageData(ArrayList<Point> snakePos, Point applePos, int score) {

    }

    private void decipherData(String message) {

    }

    // thread to handle all reading from server
    public class ReadThread extends Thread {
        public SocketChannel channel = null;

        public ReadThread(SocketChannel client) {
            channel = client;
        }

        public void run() {

        }
    }

    // thread to handle all writing to server
    public class WriteThread extends Thread {
        public SocketChannel channel = null;
        String data = null;

        public WriteThread(String s, SocketChannel client) {
            data = s;
            channel = client;
        }

        public void run() {
            try {
                ByteBuffer databb = ByteBuffer.wrap(data.getBytes());
                int temp = client.write(databb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
//
//class NetworkThread implements Runnable {
//    private SocketChannel server;
//    Selector selector;
//
//    public NetworkThread(SocketChannel channel) throws Exception {
//        server = channel;
//    }
//
//    public void run() {
//        try {
//            selector = Selector.open();
//            server.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
