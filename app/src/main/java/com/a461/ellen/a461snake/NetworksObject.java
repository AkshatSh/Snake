package com.a461.ellen.a461snake;

import android.graphics.Point;

import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class NetworksObject implements NetworkObservable {

    // implement NetworkObservable interface
    private List<NetworkListener> listeners = new ArrayList<NetworkListener>();

    public void add(NetworkListener listener) {
        listeners.add(listener);
    }

    public void remove(NetworkListener listener) {
        listeners.remove(listener);
    }

    private int numPlayers;
    private Selector selector;
    private SocketChannel client;
    private ReadThread rt = null;
    private ArrayList<Point> otherSnake = null;
    private int otherScore;
    private Point applePos = null;

    public NetworksObject(int n) {
        numPlayers = n;
        openConnection();
//        try {
//            new Thread(new NetworkThread(client)).start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    // open connection with server
    private void openConnection() {
        // int result = 0;
        try {
            client = SocketChannel.open();
            InetSocketAddress server = new InetSocketAddress("localhost", 1111);
            client.connect(server);
            client.configureBlocking(false);
            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            receive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receive() {
        rt = new ReadThread("read thread", client);
        rt.start();
    }

    // for setting up initial game--request server send positions of both
    // snakes and apple given the size of the board
    public void sendInitialGame(int rows, int cols) {
        String data = new String("r:" + rows + "\n" + "c:" + cols + "\r\n");
        System.out.println("message: \n" + data);
        try {
            ByteBuffer databb = ByteBuffer.wrap(data.getBytes());
            int written = client.write(databb);
            System.out.println("wrote " + written);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // delimit points by a space
    // delimit fields by a \n
    public void sendMoves(ArrayList<Point> snakePos, Point applePos, int score, String message) {
        System.out.println("packaging data");
        String data = "p:";
        for (Point p: snakePos) {
            data += "[" + p.x + "," + p.y + "] ";
        }
        data += "\na:" + "[" + applePos.x + "," + applePos.y + "]";
        data += "\ns:" + score + "\nm:" + message + "\r\n";
        System.out.println("data: \n" + data);
        try {
            ByteBuffer databb = ByteBuffer.wrap(data.getBytes());
            int written = client.write(databb);
            System.out.println("wrote " + written);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decipherData(String message) {

    }

    // thread to handle all reading from server
    public class ReadThread extends Thread {
        public SocketChannel channel = null;

        public ReadThread(String s, SocketChannel client) {
            super(s);
            channel = client;
        }

        public void run() {
            System.out.println("reading");
            int numRead = 0;
            ByteBuffer bb = ByteBuffer.allocate(2048);
            try {
                while (true) {
                    while((numRead = client.read(bb)) != -1) {
                        if (numRead == 0) {
                            continue;
                        }
                        bb.flip();
                        Charset cs = Charset.forName("utf-8");
                        CharsetDecoder decoder = cs.newDecoder();
                        CharBuffer charBuffer = decoder.decode(bb);
                        String result = charBuffer.toString();
                        bb.clear();
                    }
                }
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
