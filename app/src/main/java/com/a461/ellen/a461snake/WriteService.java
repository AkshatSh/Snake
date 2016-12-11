package com.a461.ellen.a461snake;

import android.app.IntentService;
import android.content.Intent;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteService extends IntentService {
    SocketChannel client;

    WriteService(SocketChannel c) {
        super("");
        client = c;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String data = workIntent.getDataString();
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
