package com.a461.ellen.a461snake;

public interface NetworkObservable {
    public void add(NetworkListener listener);
    public void remove(NetworkListener listener);
}
