package com.tedngok.classmanagementsystem;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class NetworkErrorActivity extends AppCompatActivity {

    private WebSocketIO webSocketIO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_error);


        setResponse();
    }

    private void setResponse(){
        webSocketIO._socket.off("connect");
        webSocketIO._socket.on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                NetworkErrorActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {

//        super.onBackPressed();
    }
}
