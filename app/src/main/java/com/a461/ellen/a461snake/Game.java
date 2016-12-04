package com.a461.ellen.a461snake;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import static com.a461.ellen.a461snake.R.id.button;

public class Game extends AppCompatActivity {
    private SnakeView sv;
    private Button playAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        sv = (SnakeView) findViewById(R.id.snake_game);
        CustomTextView status = (CustomTextView) findViewById(R.id.text);
        CustomTextView score = (CustomTextView) findViewById(R.id.score);
        CustomTextView oscore = (CustomTextView) findViewById(R.id.oscore);
        Button pauseButton = (Button) findViewById(R.id.button);

        playAgain = (Button) findViewById(R.id.playAgain);
        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sv.setViews(status, score, oscore, pauseButton, playAgain);

        Intent i = getIntent();
        int numPlayers = i.getIntExtra("numPlayers", 1);

        // if numPlayers > 1, send request to NetworksObject
        System.out.println("players: " + numPlayers);
        if (numPlayers > 1) {
            sv.setMode(SnakeView.REQUESTING);
        } else {
            oscore.setVisibility(View.INVISIBLE);
            sv.setMode(SnakeView.READY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playAgain.isShown()) {
            return;
        }
        sv.setMode(SnakeView.PAUSE);
    }
}
