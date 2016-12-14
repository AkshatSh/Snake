package com.a461.ellen.a461snake;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class GridView extends View {
    // leave space on top for score card and pause button
    private static final int TOP_OFFSET = 120;

    // # of tiles in grid & size of each tile
    protected static int tileSize = 48;
    protected static int numColumns;
    protected static int numRows;

    private final Paint p = new Paint();
    private static int xpadding;
    private static int ypadding;

    // array of drawable icons
    private Bitmap[] bitmapImg;
    // what to draw at each cell
    private int[][] grid;

    public GridView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
    }

    public void resetTiles(int count) {
        bitmapImg = new Bitmap[count];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // leave some space for buttons and scoreboard on top
        h = h - TOP_OFFSET;
        //numColumns = (int) Math.floor(w / tileSize);
        //numRows = (int) Math.floor(h / tileSize);
        numColumns = 22;
        numRows = 31;

        xpadding = ((w - (tileSize * numColumns)) / 2);
        ypadding = ((h - (tileSize * numRows)) / 2);

        grid = new int[numColumns][numRows];
        clearTiles();

        System.out.println("screen size changed " + oldw + "x" + oldh + " : " + w + "x" + h);
        if (oldw == 0 && oldh == 0 && w > 0 && h > 0) {
            screenCreated();
        }
    }


    public void screenCreated() {

    }

    public void clearTiles() {
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                setTile(0, i, j);
            }
        }
    }

    public void setTile(int tile, int x, int y) {
        if (grid == null) {
            grid = new int[numColumns][numRows];
        }
        grid[x][y] = tile;
    }

    // map key with drawable icon
    public void loadTile(int key, Drawable tile) {
        // System.out.println("\nTILE SIZE: " + tileSize);
        Bitmap b = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        tile.setBounds(0, 0, tileSize, tileSize);
        tile.draw(c);

        bitmapImg[key] = b;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                int tile = grid[i][j];
                if (tile > 0) {
                    canvas.drawBitmap(bitmapImg[tile],
                            xpadding + (i * tileSize),
                            TOP_OFFSET + ypadding + (j * tileSize),
                            p);
                }
            }
        }
    }
}
