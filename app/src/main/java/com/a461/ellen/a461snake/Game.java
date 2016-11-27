package com.a461.ellen.a461snake;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Game extends AppCompatActivity {
    private SnakeView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        sv = (SnakeView) findViewById(R.id.snake_game);
        sv.setTextView((TextView) findViewById(R.id.text));
        sv.setMode(SnakeView.READY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sv.setMode(SnakeView.PAUSE);
    }
}
