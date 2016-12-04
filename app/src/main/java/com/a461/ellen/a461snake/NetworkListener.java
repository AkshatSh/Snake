package com.a461.ellen.a461snake;

import android.graphics.Point;

import java.util.ArrayList;

public interface NetworkListener {
    public void moveReceived(ArrayList<Point> s, Point apple, int otherscore);
}
