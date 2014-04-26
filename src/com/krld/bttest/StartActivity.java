package com.krld.bttest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Andrey on 4/18/2014.
 */
public class StartActivity extends Activity{
    private Button toFindDevicesButton;
    private Button toBTServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        initButtons();
    }

    private void initButtons() {
        toFindDevicesButton = (Button) findViewById(R.id.toFindBTDevicesButton);
        toFindDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(StartActivity.this, FindBTDevices.class);
                StartActivity.this.startActivity(myIntent);
            }
        });
        toBTServerButton = (Button) findViewById(R.id.toBTServerButton);
        toBTServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(StartActivity.this, ServerBTActivity.class);
                StartActivity.this.startActivity(myIntent);
            }
        });
    }

    private void showToast(String message) {
        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
