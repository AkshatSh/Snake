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

    // # of tiles in grid & size of each tile
    protected static int tileSize;
    protected static int numColumns;
    protected static int numRows;

    private final Paint p = new Paint();
    private static int xpadding;
    private static int ypadding;

    // array of drawable icons
    private Bitmap[] bitmapImg;
    // what to draw at each cell
    private int[][] grid;

    public GridView(Context context, AttributeSet attributes) {
        this(context, attributes, 0);
    }

    public GridView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
    }

    public void resetTiles(int count) {
        bitmapImg = new Bitmap[count];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        numColumns = (int) Math.floor(w / tileSize);
        numRows = (int) Math.floor(h / tileSize);

        xpadding = ((w - (tileSize * numColumns)) / 2);
        ypadding = ((h - (tileSize * numRows)) / 2);

        grid = new int[numColumns][numRows];
        clearTiles();
    }

    public void clearTiles() {
        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                setTile(0, i, j);
            }
        }
    }

    public void setTile(int tile, int x, int y) {
        grid[x][y] = tile;
    }

    // map key with drawable icon
    public void loadTile(int key, Drawable tile) {
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
                            ypadding + (j * tileSize),
                            p);
                }
            }
        }
    }
}
