package com.a461.ellen.a461snake;

import android.graphics.Point;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
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

public class NetworksObject implements NetworkObservable, ThreadCallback {

    // implement NetworkObservable
    private NetworkListener listener = null;

    public void setListener(NetworkListener nl) {
        listener = nl;
    }

    public void removeListener(NetworkListener nl) {
        listener = null;
    }

    // implement ThreadCallback
    public void callback(String result) {
        decipherData(result);
    }

    public void connectionEstablished(SocketChannel c) {
        client  = c;
        wt.client = client;
        rt.channel = client;
        rt.start();
        if (client == null) {
            System.out.println("client is null");
        }
        listener.connectionEstablished();
    }

    private int numPlayers;
    private Selector selector;
    private SocketChannel client = null;
    private ReadThread rt = null;
    private WriteThread wt = null;
    private ConnectionThread ct = null;
    //private String url = "Sample-env.3wcenitkcy.us-east-1.elasticbeanstalk.com";
    private String url = "108.179.153.156";

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
        // connect on new thread
        ct = new ConnectionThread(client, url, this);
        ct.start();
        wt = new WriteThread("", client);
        rt = new ReadThread("read thread", client, this);
    }

    // for setting up initial game--request server send positions of both
    // snakes and apple given the size of the board
    public void sendInitialGame(int rows, int cols, ArrayList<Point> snakePos) {
        String data = "p:";
        for (Point p: snakePos) {
            data += "[" + p.x + "," + p.y + "] ";
        }
        data += "\nr:" + rows + "\n" + "c:" + cols + "\r\n";
        System.out.println("message: \n" + data);

        // write to server on separate thread
        wt.data = data;
        wt.start();
    }

    // delimit points by a space
    // delimit fields by a \n
    public void sendMoves(ArrayList<Point> snakePos, Point applePos, int score, String state) {
        String data = packageData(snakePos, applePos, score, state);
        wt.data = data;
        wt.start();
    }

    private String packageData(ArrayList<Point> snakePos, Point applePos, int score, String state) {
        System.out.println("packaging data");
        String data = "p:";
        for (Point p: snakePos) {
            data += "[" + p.x + "," + p.y + "] ";
        }
        data += "\na:" + "[" + applePos.x + "," + applePos.y + "]";
        data += "\ns:" + score + "\nm:" + state + "\r\n";
        System.out.println("data: \n" + data);
        return data;
    }

    private void decipherData(String message) {
        // split string into fields
        ArrayList<Point> otherSnake = null;
        int otherScore = 0;
        Point applePos = null;
        String state = null;

        // populate positions
        int posStart = message.indexOf(':');
        int posEnd = message.indexOf('\n');
        int pointStart = message.indexOf('[', posStart);
        while (pointStart != -1) {
            int x = message.charAt(pointStart + 1);
            int y = message.charAt(pointStart + 3);
            Point p = new Point(Character.getNumericValue(x), Character.getNumericValue(y));
            otherSnake.add(p);
            pointStart = message.indexOf('[', pointStart + 1);
        }

        // find apple pos
        int appleStart = message.indexOf(':', posStart + 1);
        int x = message.charAt(appleStart + 2);
        int y = message.charAt(appleStart + 4);
        applePos = new Point(Character.getNumericValue(x), Character.getNumericValue(y));

        // find score
        int scoreStart = message.indexOf(':', appleStart + 1);
        int s = message.charAt(scoreStart + 1);
        otherScore = Character.getNumericValue(s);

        // find status message
        int statusStart = message.indexOf(':', appleStart + 1);
        int statusEnd = message.indexOf("\r\n");
        state = message.substring(statusStart + 1, statusEnd);

        listener.moveReceived(otherSnake, applePos, otherScore, state);
    }
}


class ConnectionThread extends Thread {
    public SocketChannel client;
    String url;
    Selector selector;
    ThreadCallback c;

    public ConnectionThread(SocketChannel channel, String u, ThreadCallback callback) {
        System.out.println("creating new connection thread");
        url = u;
        client = channel;
        c = callback;
    }

    public void run() {
        try {
            System.out.println("running connection");
            int port = sendHttpGet(url);
            client = SocketChannel.open();
            System.out.println("opened connection");
            InetAddress host = InetAddress.getByName(url);
            System.out.println("host: " + host.getHostAddress());
            InetSocketAddress server = new InetSocketAddress(host.getHostAddress(), port);
            client.connect(server);
            client.configureBlocking(false);
            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            System.out.println("finished connection");
            c.connectionEstablished(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int sendHttpGet(String url) throws Exception {
        //String address = "http://" + url + "/api/games/create";
        String address = "http://" + url + ":8080/api/games/create";
        //URL urlObject = new URL("http", url, -1, "/");
        URL urlObject = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Snake");

        int responseCode = connection.getResponseCode();
        System.out.println("Sent POST request to " + url);
        System.out.println("response code: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        inputLine = in.readLine();
        System.out.println("response: " + inputLine);
        in.close();

        int port = Integer.parseInt(inputLine);
        return port;
    }
}

class WriteThread extends Thread {
    public String data;
    public SocketChannel client;

    public WriteThread(String s, SocketChannel c) {
        data = s;
        client = c;
    }

    public void run() {
        try {
            if (client == null) {
                System.out.println("client is null");
                return;
            }
            if (data == null) {
                System.out.println("data is null");
                return;
            }
            if (data.equals("")) {
                System.out.println("data is empty");
                return;
            }
            ByteBuffer databb = ByteBuffer.wrap(data.getBytes());
            int written = client.write(databb);
            System.out.println("wrote " + written);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// thread to handle all reading from server
class ReadThread extends Thread {
    public SocketChannel channel = null;
    ThreadCallback c;

    public ReadThread(String s, SocketChannel client, ThreadCallback callback) {
        super(s);
        channel = client;
        c = callback;
    }

    public void run() {
        if (channel == null) {
            System.out.println("client is null");
            return;
        }
        System.out.println("reading");
        int numRead = 0;
        ByteBuffer bb = ByteBuffer.allocate(2048);
        try {
            while (true) {
                while((numRead = channel.read(bb)) != -1) {
                    if (numRead == 0) {
                        continue;
                    }
                    bb.flip();
                    Charset cs = Charset.forName("utf-8");
                    CharsetDecoder decoder = cs.newDecoder();
                    CharBuffer charBuffer = decoder.decode(bb);
                    String result = charBuffer.toString();
                    bb.clear();
                    System.out.println("read " + result);
                    c.callback(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
