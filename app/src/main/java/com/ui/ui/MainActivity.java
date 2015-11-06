package com.ui.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ui.uimodule.button.RaisedButton;


public class MainActivity extends AppCompatActivity {

    RaisedButton raisedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        raisedButton = (RaisedButton) findViewById(R.id.raisedButton);

//        (findViewById(R.id.enable)).setOnClickListener(new View.OnClickListener() {
//            boolean enable = true;
//            @Override
//            public void onClick(View v) {
//                enable = !enable;
//                raisedButton.setEnabled(enable);
//            }
//        });
//
//        (findViewById(R.id.animationEnable)).setOnClickListener(new View.OnClickListener() {
//            boolean enable = true;
//            @Override
//            public void onClick(View v) {
//                enable = !enable;
//                raisedButton.setEnabledWithAnimation(enable);
//            }
//        });

    }
}
