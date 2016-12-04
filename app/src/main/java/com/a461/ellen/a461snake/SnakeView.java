package com.a461.ellen.a461snake;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class SnakeView extends GridView implements NetworkListener {

    private static final String TAG = "SnakeView";

    // modes, pause only available in single player mode
    private int currentMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOST = 3;
    public static final int REQUESTING = 4;
    public static final int WON = 5;

    // directions
    private int currentDirection = UP;
    private int nextDirection = UP;
    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int RIGHT = 3;
    private static final int LEFT = 4;
    private static final long moveDelay = 500;

    // treasure & snake drawables, reserve 0 for empty tile
    private static final int APPLE = 1;
    private static final int SNAKE_HEAD = 2;
    private static final int SNAKE_BODY = 3;
    private static final int LEAF = 4;
    private static final int OTHER_SNAKE_HEAD = 5;

    // status to inform user
    private CustomTextView statusText;
    private Button playAgain;
    private CustomTextView scoreBoard;
    private CustomTextView oscoreBoard;
    private Button pauseButton;

    private int score = 0;
    private int otherScore = 0;
    private NetworksObject networked;
    private long lastMove;

    // position of snake & apple
    private ArrayList<Point> snakePos = new ArrayList<Point>();
    private Point applePos;

    // position of other snakes
    private ArrayList<Point> otherSnakePos;

    // random generator for apple
    private static final Random r = new Random();

    // handler for animation
    private RefreshHandler redrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message m) {
            SnakeView.this.update();
            SnakeView.this.invalidate();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    public SnakeView(Context context) {
        this(context, null);
    }

    public SnakeView(Context context, AttributeSet attributes) {
        this(context, attributes, 0);
    }

    public SnakeView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        initializeView();
    }

    private void initializeView() {
        setFocusable(true);

        resetTiles(6);
        loadTile(APPLE, ContextCompat.getDrawable(this.getContext(), R.drawable.apple));
        loadTile(SNAKE_HEAD, ContextCompat.getDrawable(this.getContext(), R.drawable.snakehead));
        loadTile(OTHER_SNAKE_HEAD, ContextCompat.getDrawable(this.getContext(), R.drawable.snakehead));
        loadTile(SNAKE_BODY, ContextCompat.getDrawable(this.getContext(), R.drawable.snakebody));
        loadTile(LEAF, ContextCompat.getDrawable(this.getContext(), R.drawable.leaf));
    }

    private void initializeGame() {
        snakePos.clear();
        applePos = null;
        int startY = numRows - 2;

        // TODO: assign random starting position so other snake won't collide?
        snakePos.add(new Point(5, startY));
        snakePos.add(new Point(4, startY));
        snakePos.add(new Point(3, startY));
        snakePos.add(new Point(2, startY));
        snakePos.add(new Point(1, startY));

        nextDirection = UP;
        genNewApple();
        score = 0;
        if (networked != null) {
            otherSnakePos.clear();
            scoreBoard.setText("YOU: " + score);
            oscoreBoard.setText("THEM: " + otherScore);
            pauseButton.setVisibility(View.INVISIBLE);
        } else {
            scoreBoard.setText("SCORE: " + score);
            oscoreBoard.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void genNewApple() {
        Point appleLoc = null;
        boolean added = false;
        while (!added) {
            int x = 1 + r.nextInt(numColumns - 2);
            int y = 1 + r.nextInt(numRows - 2);

            appleLoc = new Point(x, y);
            added = !(snakePos.contains(appleLoc));
            if (networked != null) {
                added &= !(otherSnakePos.contains(appleLoc));
            }
        }

        applePos = appleLoc;
    }

    public void setViews(CustomTextView statusView, CustomTextView scoreView,
                         CustomTextView oscoreView, Button pause, Button playagain) {
        statusText = statusView;
        scoreBoard = scoreView;
        oscoreBoard = oscoreView;
        oscoreBoard.setVisibility(View.INVISIBLE);
        playAgain = playagain;
        playAgain.setVisibility(View.INVISIBLE);

        pauseButton = pause;
        pauseButton.setVisibility(View.INVISIBLE);
        pauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMode == PAUSE) {
                    v.setBackgroundResource(R.drawable.pause);
                    setMode(RUNNING);
                    update();
                } else if (currentMode == RUNNING){
                    v.setBackgroundResource(R.drawable.play);
                    setMode(PAUSE);
                    update();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent m) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (currentMode == READY || currentMode == LOST) {
                    initializeGame();
                    setMode(RUNNING);
                    update();
                    return true;
                }

                if (currentMode == PAUSE) {
                    setMode(RUNNING);
                    update();
                    return true;
                }

                nextDirection = (currentDirection == DOWN) ? currentDirection : UP;
                System.out.println("pressed up");
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                nextDirection = (currentDirection == UP) ? currentDirection : DOWN;
                System.out.println("pressed down");
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                nextDirection = (currentDirection == RIGHT) ? currentDirection : LEFT;
                System.out.println("pressed left");
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                nextDirection = (currentDirection == LEFT) ? currentDirection : RIGHT;
                System.out.println("pressed right");
                return true;
            default:
                return super.onKeyDown(keyCode, m);
        }
    }

    public void setMode(int newMode) {
        int oldMode = currentMode;
        currentMode = newMode;

        if (currentMode == REQUESTING) {
            System.out.println("rows: " + numRows + " cols: " + numColumns);
            // initializeGame();
            statusText.setText("Searching for players...");
            pauseButton.setVisibility(View.INVISIBLE);
            networked = new NetworksObject(2);
            networked.setListener(this);
            networked.sendInitialGame(numRows, numColumns, snakePos);
            return;
        }

        if (currentMode == RUNNING && oldMode != RUNNING) {
            System.out.println("running game...");
            statusText.setVisibility(View.INVISIBLE);
            playAgain.setVisibility(View.INVISIBLE);
            scoreBoard.setVisibility(View.VISIBLE);
            update();
            return;
        }

        CharSequence s = "";
        switch(currentMode) {
            case PAUSE:
                s = "PAUSED\nPress play or swipe up to resume";
                break;
            case READY:
                s = "Swipe up to begin";
                break;
            case WON:
                s = "YOU WON!\nScore: " + score;
                playAgain.setVisibility(View.VISIBLE);
                break;
            case LOST:
                s = "GAME OVER\nScore: " + score;
                playAgain.setVisibility(View.VISIBLE);
                break;
        }
        statusText.setText(s);
        statusText.setVisibility(View.VISIBLE);
    }

    public void update() {
        if (currentMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - lastMove > moveDelay) {
                clearTiles();
                updateSnakes();
                updateWalls();
                setTile(APPLE, applePos.x, applePos.y);
                if (networked != null) {
                    networked.sendMoves(snakePos, applePos, score, (currentMode == LOST ? "lost" : ""));
                }
                lastMove = now;
            }
            redrawHandler.sleep(moveDelay);
        }
    }

    private void updateSnakes() {
        boolean growSnake = false;

        // move snake -- remove from tail and add to head if not growing
        Point head = snakePos.get(0);
        Point newHead = new Point(1, 1);
        currentDirection = nextDirection;

        switch (currentDirection) {
            case RIGHT:
                newHead = new Point(head.x + 1, head.y);
                break;
            case LEFT:
                newHead = new Point(head.x - 1, head.y);
                break;
            case UP:
                newHead = new Point(head.x, head.y - 1);
                break;
            case DOWN:
                newHead = new Point(head.x, head.y + 1);
                break;
        }

        // check for collision with itself
        if (snakePos.contains(newHead)) {
            setMode(LOST);
            System.out.println("collision with itself");
            return;
        }

        if (networked != null) {
            if (otherSnakePos.contains(newHead)) {
                setMode(LOST);
                System.out.println("collision with other snake");
                return;
            }
        }

        // check for collision with walls
        if ((newHead.x < 1) || (newHead.y < 1) || (newHead.x > numColumns - 2) || (newHead.y > numRows - 2)) {
            setMode(LOST);
            System.out.println("collision detected");
            return;
        }

        // check if it ate apple
        if (newHead.equals(applePos)) {
            genNewApple();
            score++;
            scoreBoard.setText("SCORE: " + score);
            growSnake = true;
        }

        // move snake
        snakePos.add(0, newHead);
        if (!growSnake) {
            snakePos.remove(snakePos.size() - 1);
        }

        for (int i = 0; i < snakePos.size(); i++) {
            Point p = snakePos.get(i);
            if (i == 0) {
                setTile(SNAKE_HEAD, p.x, p.y);
            } else {
                setTile(SNAKE_BODY, p.x, p.y);
            }
        }
    }
    
    private void updateWalls() {
        for (int i = 0; i < numColumns; i++) {
            setTile(LEAF, i, 0);
            setTile(LEAF, i, numRows - 1);
        }
        for (int j = 1; j < numRows - 1; j++) {
            setTile(LEAF, 0, j);
            setTile(LEAF, numColumns - 1, j);
        }
    }

    public void moveReceived(ArrayList<Point> s, Point apple, int otherscore, String state) {
        if (otherSnakePos != null || otherSnakePos.size() > 0) {
            // remove current other snake
            for (int i = 0; i < otherSnakePos.size(); i++) {
                Point p = otherSnakePos.get(i);
                if (i == 0) {
                    setTile(0, p.x, p.y);
                } else {
                    setTile(0, p.x, p.y);
                }
            }
        }
        if (state.equals("connected")) {
            if (currentMode == REQUESTING) {
                setMode(RUNNING);
            }
            otherSnakePos = s;
            otherScore = otherscore;
            applePos = apple;
        }

        // if other snake collided with itself, the wall, or our snake
        // this detection should be done on the other client
        if (state.equals("lost")) {
            setMode(WON);
            return;
        }

        // update drawing of other snake
        for (int i = 0; i < otherSnakePos.size(); i++) {
            Point p = otherSnakePos.get(i);
            if (i == 0) {
                setTile(OTHER_SNAKE_HEAD, p.x, p.y);
            } else {
                setTile(SNAKE_BODY, p.x, p.y);
            }
        }
    }
}
