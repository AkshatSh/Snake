package com.a461.ellen.a461snake;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class StartScreen extends AppCompatActivity {
    private int numPlayers;
    private CustomTextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.start_screen);

        System.out.println("invoked start screen");
        numPlayers = 1;
        Button down = (Button) findViewById(R.id.downbutton);
        Button up = (Button) findViewById(R.id.upbutton);
        final Button start = (Button) findViewById(R.id.startbutton);
        tv = (CustomTextView) findViewById(R.id.players);

        down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (numPlayers > 1) {
                    numPlayers--;
                    tv.setText("" + numPlayers);
                }
            }
        });

        up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (numPlayers < 2) {
                    numPlayers++;
                    tv.setText("" + numPlayers);
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent startGame = new Intent(getApplicationContext(), Game.class);
                startGame.putExtra("numPlayers", numPlayers);
                startActivity(startGame);
            }
        });
    }
}
