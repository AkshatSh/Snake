package com.a461.ellen.a461snake;

import android.graphics.Point;

import java.util.ArrayList;

public interface NetworkListener {
    public void moveReceived(ArrayList<Point> s, ArrayList<Point> os, Point apple, int otherscore, String message);
    public void connectionEstablished();
}
