package com.tedngok.classmanagementsystem;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

import static java.lang.Thread.sleep;

public class StartActivity extends AppCompatActivity {

    private final int reqMultiPermission = 1000;
    private WebSocketIO webSocketIO;
    private boolean socketConnectStatus = false;
    private boolean clientAssignPermission = false;
    private TextView txtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        webSocketIO = new WebSocketIO(this.getApplicationContext());
        txtMessage = (TextView) findViewById(R.id.txtMessage);

        verifyPermission();

        setResponse();
    }

    private void setResponse() {
        webSocketIO._socket.off("connect");
        webSocketIO._socket.on("connect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                socketConnectStatus = true;
                start();
            }
        });

        webSocketIO._socket.off("disconnect");
        webSocketIO._socket.on("disconnect", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                txtMessage.setText("Connect to Server: Failed");
                socketConnectStatus = false;
            }
        });
    }

    private void verifyPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA + Manifest.permission.READ_CONTACTS +
                        Manifest.permission.ACCESS_COARSE_LOCATION + Manifest.permission.ACCESS_FINE_LOCATION +
                        Manifest.permission.SEND_SMS + Manifest.permission.READ_EXTERNAL_STORAGE + Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                requestPermission(true);

            } else {

                // No explanation needed, we can request the permission.

                requestPermission(false);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case reqMultiPermission: {

                if (grantResults.length > 0) {
                    boolean CAMERA_PERMISSION = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_CONTACTS_PERMISSION = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ACCESS_COARSE_LOCATION_PERMISSION = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean ACCESS_FINE_LOCATION_PERMISSION = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean SEND_SMS_PERMISSION = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean READ_EXTERNAL_STORAGE_PERMISSION = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean WRITE_EXTERNAL_STORAGE_PERMISSION = grantResults[6] == PackageManager.PERMISSION_GRANTED;

                    if (CAMERA_PERMISSION && READ_CONTACTS_PERMISSION && ACCESS_COARSE_LOCATION_PERMISSION && ACCESS_FINE_LOCATION_PERMISSION &&
                            SEND_SMS_PERMISSION && READ_EXTERNAL_STORAGE_PERMISSION && WRITE_EXTERNAL_STORAGE_PERMISSION) {

                        clientAssignPermission = true;
                        start();

                    } else {
                        requestPermission(true);
                    }
                } else {
                    requestPermission(true);
                }
                return;
            }

        }
    }

    private void requestPermission(boolean needViewOnCLick) {
        if (needViewOnCLick) {
            Snackbar.make(StartActivity.this.findViewById(android.R.id.content),
                    "Please Grant Permissions to run CMS",
                    Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(StartActivity.this,
                                    new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.READ_CONTACTS,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.SEND_SMS,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    },
                                    reqMultiPermission);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    reqMultiPermission);
        }

    }

    private void start() {

        if (clientAssignPermission && !socketConnectStatus) {
            txtMessage.setText("Try to Connect Server...");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(15000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtMessage.setText(getString(R.string.msg_socket_connect_fail));
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            t.start();
        } else if (socketConnectStatus && clientAssignPermission) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sleep(1000);
                        changeActivity();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    private void changeActivity() {
        final Intent main = new Intent(this, BasicActivity.class);
        final Intent login = new Intent(this, LoginActivity.class);
        SharedPreferences sp = this.getSharedPreferences(getString(R.string.login_preference_file), this.MODE_PRIVATE);
        if (sp.getString(getString(R.string.token), "") != "") {
            startActivity(main);
        } else {
            startActivity(login);
        }
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
