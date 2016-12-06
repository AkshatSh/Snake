package com.a461.ellen.a461snake;

import java.nio.channels.SocketChannel;

public interface ThreadCallback {
    public void callback(String result);
    public void connectionEstablished(SocketChannel c);
}
