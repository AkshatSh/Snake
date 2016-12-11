package com.a461.ellen.a461snake;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
    // public static final int WAITING = 6;

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
    private String scoreText = "YOU";
    private Button pauseButton;

    private int score = 0;
    private int otherScore = 0;

    private NetworksObject networked;
    boolean connectionEstablished = false;
    boolean screenCreated = false;

    // for detecting swipes
    private float x1, x2;
    private float y1, y2;
    static final int MIN_DISTANCE = 150;

    // position of snake & apple
    private ArrayList<Point> snakePos = new ArrayList<Point>();
    private Point applePos;

    // position of other snakes
    private ArrayList<Point> otherSnakePos;

    // random generator for apple
    private static final Random r = new Random();

    // handler for animation
    private RefreshHandler redrawHandler = new RefreshHandler();
    private Handler mainHandler;
    private long lastMove;

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
        mainHandler = new Handler(context.getMainLooper());
        initializeView();
    }

    private void initializeView() {
        //System.out.println("9. board size " + numRows + " x " + numColumns);
        setFocusable(true);

        resetTiles(6);
        loadTile(APPLE, ContextCompat.getDrawable(this.getContext(), R.drawable.apple));
        loadTile(SNAKE_HEAD, ContextCompat.getDrawable(this.getContext(), R.drawable.snakehead));
        loadTile(OTHER_SNAKE_HEAD, ContextCompat.getDrawable(this.getContext(), R.drawable.othersnakehead));
        loadTile(SNAKE_BODY, ContextCompat.getDrawable(this.getContext(), R.drawable.snakebody));
        loadTile(LEAF, ContextCompat.getDrawable(this.getContext(), R.drawable.leaf));
    }

    private void initializeGame() {
        //System.out.println("8. board size " + numRows + " x " + numColumns);
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
            scoreText = "YOU: ";
            if (otherSnakePos != null) {
                otherSnakePos.clear();
            }

            oscoreBoard.setText("THEM: " + otherScore);
            oscoreBoard.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.INVISIBLE);
        } else {
            scoreText = "SCORE: ";
            oscoreBoard.setVisibility(View.INVISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
        }
        scoreBoard.setText(scoreText + score);
    }

    private void genNewApple() {
        //System.out.println("6. board size " + numRows + " x " + numColumns);
        Point appleLoc = null;
        boolean added = false;
        while (!added) {
            int x = 1 + r.nextInt(numColumns - 2);
            int y = 1 + r.nextInt(numRows - 2);

            appleLoc = new Point(x, y);
            added = !(snakePos.contains(appleLoc));
            if (networked != null && otherSnakePos != null) {
                added &= !(otherSnakePos.contains(appleLoc));
            }
        }

        applePos = appleLoc;
    }

    public void setViews(CustomTextView statusView, CustomTextView scoreView,
                         CustomTextView oscoreView, Button pause, Button playagain) {
        System.out.println("5. board size " + numRows + " x " + numColumns);
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
                return directionUp();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("TOUCH EVENT DETECTED");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                System.out.println("action down");
                x1 = event.getX();
                y1 = event.getY();
                System.out.println("x1: " + x1 + "\ty1: " + y1);
                return true;
            case MotionEvent.ACTION_UP:
                System.out.println("action up");
                x2 = event.getX();
                y2 = event.getY();
                System.out.println("x2: " + x2 + "\ty2: " + y2);
                float deltaX = x2 - x1;
                float deltaY = y2 - y1;

                // left-right swipes
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    System.out.println("deltax");
                    if (x2 > x1) {
                        // left -> right
                        nextDirection = (currentDirection == LEFT) ? currentDirection : RIGHT;
                        System.out.println("pressed right");
                        return true;
                    } else {
                        // right -> left
                        nextDirection = (currentDirection == RIGHT) ? currentDirection : LEFT;
                        System.out.println("pressed left");
                        return true;
                    }
                } else if (Math.abs(deltaY) > MIN_DISTANCE) {
                    System.out.println("deltay");
                    if (y2 > y1) {
                        // down -> up
                        nextDirection = (currentDirection == UP) ? currentDirection : DOWN;
                        System.out.println("pressed down");
                        return true;
                    } else {
                        // up -> down
                        return directionUp();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean directionUp() {
        if (currentMode == READY || currentMode == LOST) {
            //System.out.println("1. board size " + numRows + " x " + numColumns);
            initializeGame();
            //System.out.println("2. board size " + numRows + " x " + numColumns);
            setMode(RUNNING);
            //System.out.println("3. board size " + numRows + " x " + numColumns);
            update();
            //System.out.println("4. board size " + numRows + " x " + numColumns);
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
    }

    public void setMode(int newMode) {
        //System.out.println("10. board size " + numRows + " x " + numColumns);
        int oldMode = currentMode;
        currentMode = newMode;

        if (currentMode == REQUESTING) {
            System.out.println("rows: " + numRows + " cols: " + numColumns);
            // initializeGame();
            statusText.setText("Searching for players...");
            pauseButton.setVisibility(View.INVISIBLE);
            networked = new NetworksObject(2, mainHandler);
            networked.setListener(this);
            return;
        }

        if (currentMode == RUNNING && oldMode != RUNNING) {
            System.out.println("running game...");
            statusText.setVisibility(View.INVISIBLE);
            playAgain.setVisibility(View.INVISIBLE);
            scoreBoard.setVisibility(View.VISIBLE);
            if (networked != null) {
                oscoreBoard.setVisibility(View.VISIBLE);
            }
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
                System.out.println("won");
                s = "YOU WON!\nYour Score: " + score + "\nTheir Score: " + otherScore;
                playAgain.setVisibility(View.VISIBLE);
                break;
            case LOST:
                System.out.println("lost");
                s = "GAME OVER\n Your Score: " + score + "\nTheir Score: " + otherScore;
                playAgain.setVisibility(View.VISIBLE);
                break;
        }
        statusText.setText(s);
        statusText.setVisibility(View.VISIBLE);
    }

    public void update() {
        //System.out.println("11. board size " + numRows + " x " + numColumns);
        if (currentMode == RUNNING) {
            //System.out.println("12. board size " + numRows + " x " + numColumns);
            long now = System.currentTimeMillis();

            if (now - lastMove > moveDelay) {
                clearTiles();
                updateSnake();
                updateOtherSnake();
                updateWalls();
                setTile(APPLE, applePos.x, applePos.y);
                if (networked != null) {
                    System.out.println("current mode: " + currentMode);
                    if (currentMode == LOST) {
                        networked.sendLostMessage();
                        return;
                    }
                    networked.sendMoves(snakePos, applePos, score, "");
                }
                lastMove = now;
            }
            redrawHandler.sleep(moveDelay);
        }
    }

    private void updateSnake() {
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
        System.out.println("new head " + newHead.toString());

        // check for collision with itself
        if (snakePos.contains(newHead)) {
            setMode(LOST);
            System.out.println("collision with itself");
            return;
        }

        if (otherSnakePos != null) {
            if (otherSnakePos.contains(newHead)) {
                setMode(LOST);
                System.out.println("collision with other snake");
                return;
            }
        }

        System.out.println("new points:\n");
        for (Point p: snakePos) {
            System.out.println(p.toString());
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
            if (networked != null) {
                scoreBoard.setText("YOU: " + score);
            } else {
                scoreBoard.setText("SCORE: " + score);
            }

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

    private void updateOtherSnake() {
        if (otherSnakePos == null) {
            return;
        }
        System.out.println("updating other snake");
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

    public void connectionEstablished() {
        System.out.println("connection establisehd");
        connectionEstablished = true;
        if (screenCreated) {
            screenCreated();
        }
    }

    @Override
    public void screenCreated() {
        screenCreated = true;
        if (connectionEstablished && currentMode == REQUESTING && otherSnakePos != null) {
            initializeGame();
            System.out.println("prepared to write rows cols " + numRows + ", " + numColumns);
            networked.sendInitialGame(numRows, numColumns, snakePos);
        }
    }

    public void moveReceived(ArrayList<Point> s, ArrayList<Point> os, Point apple, int oscore, String state) {
        if (otherSnakePos != null && otherSnakePos.size() > 0) {
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

        // if other snake collided with itself, the wall, or our snake
        // this detection should be done on the other client

        //if (state.equals("connected")) {

        otherSnakePos = os;
        if (otherScore != oscore) {
            otherScore = oscore;
            oscoreBoard.setText("THEM: " + otherScore);
        }

        applePos = apple;
        //}

        if (state.equals("initialgame")) {
            System.out.println("initial game received");
            snakePos = s;
            setMode(RUNNING);
            update();
        } else {
            updateOtherSnake();
        }
    }

    public void endGame() {
        System.out.println("won state");
        setMode(WON);
    }
}
