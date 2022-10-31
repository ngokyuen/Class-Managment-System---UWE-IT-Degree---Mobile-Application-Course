package com.tedngok.classmanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class ProfileActivity extends AppCompatActivity {

    private WebSocketIO webSocketIO;
    private EditText editFirstName, editLastName, editMobileNo,
            editHomeNo, editAddress, editEmail, editPassword, editRePassword, editHKID;
    private RadioGroup radioGender;
    private RadioButton radioMale, radioFemale;

    private ImageView imgUser;

    private ProgressBar loadProgress;
    private NestedScrollView profile_view;
    private Button btnSubmit;

    private boolean load_profile_callback = false;
    private AsyncTask<JSONObject, Void, Boolean> mTask = null;
    private AsyncTask<?, ?, ?> mTask2 = null;
    private Common _common;

    private AlertDialog photoDialog;
    private LinearLayout takePhotoCaptureView, takePhotoFromFileView;

    private final int reqTakePhotoFromFile = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        _common = new Common(this);

        editFirstName = (EditText) findViewById(R.id.editFirstName);
        editLastName = (EditText) findViewById(R.id.editLastName);

        editHKID = (EditText) findViewById(R.id.editHKID);

        editMobileNo = (EditText) findViewById(R.id.editMobileNo);
        editHomeNo = (EditText) findViewById(R.id.editHomeNo);

        radioGender = (RadioGroup) findViewById(R.id.radioGenderGroup);
        radioMale = (RadioButton) findViewById(R.id.radioMale);
        radioFemale = (RadioButton) findViewById(R.id.radioFemale);

        editAddress = (EditText) findViewById(R.id.editAddress);
        editEmail = (EditText) findViewById(R.id.editEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editRePassword = (EditText) findViewById(R.id.editRePassword);

        profile_view = (NestedScrollView) findViewById(R.id.profile_view);
        loadProgress = (ProgressBar) findViewById(R.id.loadProgress);

        imgUser = (ImageView) findViewById(R.id.imgUser);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        setAction();
        setResponse();
        mTask = new GetProfileTask().execute(new JSONObject());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == _common.reqTakePhotoCapture) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                String img_base64 = _common.bitmapToBase64(bitmap);

                imgUser.setImageBitmap(bitmap);

                try {
                    JSONObject json = new JSONObject();
                    json.put(getString(R.string.jsonToken), _common.getToken());
                    json.put(getString(R.string.jsonImage), img_base64);

                    mTask2 = new UploadPhotoTask().execute(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
//        else if (requestCode == reqTakePhotoFromFile) {
//            if (resultCode == RESULT_OK) {
//                Uri selectedImageUri = data.getData();
//                if (selectedImageUri != null) {
//
//                }
//            }
//        }
    }

    private void setAction() {
        final Context me = this;

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout capture_method = (LinearLayout) getLayoutInflater().inflate(R.layout.capture_method, null);
                takePhotoCaptureView = (LinearLayout) capture_method.findViewById(R.id.takePhotoCaptureView);
//                takePhotoFromFileView = (LinearLayout) capture_method.findViewById(R.id.takePhotoFromFileView);

                photoDialog = new AlertDialog.Builder(me).setView(capture_method).show();
                takePhotoCaptureView.setOnClickListener(_common.takePhotoOnClickListener(photoDialog));

//                takePhotoFromFileView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent i = new Intent();
//                        i.setType("image/*");
//                        i.setAction(Intent.ACTION_GET_CONTENT);
//                        startActivityForResult(Intent.createChooser(i, "Select Photo"), reqTakePhotoFromFile);
//                        photoDialog.dismiss();
//                    }
//                });

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean verify = true;

                JSONObject verifyResult = _common.verifyPassword(editPassword.getText().toString(), editRePassword.getText().toString());
                try {
                    if (!verifyResult.getBoolean(getString(R.string.result))) {
                        verify = false;
                        editPassword.setError(verifyResult.getString(getString(R.string.jsonErrorMsg)));
                    } else if (!editPassword.getText().toString().equals("") && !_common.isPasswordValid(editPassword.getText().toString())){
                        verify = false;
                        editPassword.setError(getString(R.string.error_invalid_password));
                    } else {
                        editPassword.setError(null);
                    }

                    if (!_common.isEmailValid(editEmail.getText().toString())){
                        verify = false;
                        editEmail.setError(getString(R.string.error_invalid_email));
                    } else {
                        editEmail.setError(null);
                    }


                    if (verify){
                        JSONObject json = new JSONObject();
                        SharedPreferences sp = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE);
                        try {
                            json.put(getString(R.string.jsonToken), sp.getString(getString(R.string.token), ""));
                            json.put(getString(R.string.jsonHKID), editHKID.getText());
                            json.put(getString(R.string.jsonEmail), editEmail.getText());
                            json.put(getString(R.string.jsonAddress), editAddress.getText());
                            json.put(getString(R.string.jsonFirstName), editFirstName.getText());
                            json.put(getString(R.string.jsonLastName), editLastName.getText());
                            json.put(getString(R.string.jsonMobileNo), editMobileNo.getText());
                            json.put(getString(R.string.jsonHomeNo), editHomeNo.getText());
                            if (radioMale.isChecked()) {
                                json.put(getString(R.string.jsonGender), getString(R.string.jsonMale));
                            } else if (radioFemale.isChecked()) {
                                json.put(getString(R.string.jsonGender), getString(R.string.jsonFemale));
                            }

                            if (!editPassword.getText().toString().equals("")) {
                                json.put(getString(R.string.jsonPassword), editPassword.getText().toString());
                            }

                            mTask = new UpdateProfileTask().execute(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void setResponse() {
        webSocketIO._socket.off("uploadProfileImage");
        webSocketIO._socket.on("uploadProfileImage", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {

                                showUpdateSuccess(getString(R.string.msg_update_image_success));
                            } else {
                                showTryAgainError();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showTryAgainError();
                        }
                    }
                });
            }
        });

        webSocketIO._socket.off("updateProfile");
        webSocketIO._socket.on("updateProfile", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                showUpdateSuccess(null);
                            } else {
                                showTryAgainError();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showTryAgainError();
                        }
                        load_profile_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });

        webSocketIO._socket.off("getProfile");
        webSocketIO._socket.on("getProfile", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject response = (JSONObject) args[0];
                        JSONObject user = null;

                        try {
                            user = (JSONObject) response.getJSONObject("user");
                            if (user.has(getString(R.string.jsonAddress))) {
                                editAddress.setText(user.getString(getString(R.string.jsonAddress)));
                            }
                            if (user.has(getString(R.string.jsonEmail))) {
                                editEmail.setText(user.getString(getString(R.string.jsonEmail)));
                            }
                            if (user.has(getString(R.string.jsonLastName))) {
                                editLastName.setText(user.getString(getString(R.string.jsonLastName)));
                            }
                            if (user.has(getString(R.string.jsonFirstName))) {
                                editFirstName.setText(user.getString(getString(R.string.jsonFirstName)));
                            }
                            if (user.has(getString(R.string.jsonMobileNo))) {
                                editMobileNo.setText(user.getString(getString(R.string.jsonMobileNo)));
                            }
                            if (user.has(getString(R.string.jsonHomeNo))) {
                                editHomeNo.setText(user.getString(getString(R.string.jsonHomeNo)));
                            }
                            if (user.has(getString(R.string.jsonHKID))) {
                                editHKID.setText(user.getString(getString(R.string.jsonHKID)));
                            }
                            if (user.has(getString(R.string.jsonGender))) {
                                final String gender = user.getString(getString(R.string.jsonGender));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (gender.equals(getString(R.string.jsonMale))) {
                                            radioMale.setChecked(true);
                                        } else if (gender.equals(getString(R.string.jsonFemale))) {
                                            radioFemale.setChecked(true);
                                        }
                                    }
                                });
                            }
                            if (user.has(getString(R.string.jsonImage))) {
                                final String image_base64 = user.getString(getString(R.string.jsonImage));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imgUser.setImageBitmap(_common.base64ToBitmap(image_base64));
                                    }
                                });
                            }

                            load_profile_callback = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            load_profile_callback = false;

                            showTryAgainError();
                            finish();

                        }
                        showLoadingProgress();
                    }
                });

            }

        });
    }

    private void showLoadingProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!load_profile_callback) {
                    loadProgress.setVisibility(View.VISIBLE);
                    profile_view.setVisibility(View.INVISIBLE);
                } else {
                    loadProgress.setVisibility(View.INVISIBLE);
                    profile_view.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void showUpdateSuccess(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (msg != null) {
                    Snackbar.make(profile_view, msg, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(profile_view, R.string.msg_update_success, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showTryAgainError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(profile_view, R.string.error_try_again_later, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public class UploadPhotoTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            JSONObject json = (JSONObject) params[0];
            webSocketIO._socket.emit("uploadProfileImage", json);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public class UpdateProfileTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {

            load_profile_callback = false;
            showLoadingProgress();
            webSocketIO._socket.emit("updateProfile", (JSONObject) params[0]);

            long starttime = System.currentTimeMillis();
            while (!load_profile_callback && System.currentTimeMillis() - starttime < 10000) {
            }

            if (load_profile_callback)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                showTryAgainError();
            }
            load_profile_callback = true;
            mTask = null;
            showLoadingProgress();
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            showLoadingProgress();
        }
    }

    public class GetProfileTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {

            load_profile_callback = false;
            showLoadingProgress();

            JSONObject json = new JSONObject();
            SharedPreferences sp = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE);
            String token = sp.getString(getString(R.string.token), "");
            try {
                json.put(getString(R.string.token), token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocketIO._socket.emit("getProfile", json);

            long starttime = System.currentTimeMillis();
            while (!load_profile_callback && System.currentTimeMillis() - starttime < 10000) {
            }

            if (load_profile_callback)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                showTryAgainError();
                finish();
            }
            mTask = null;
            showLoadingProgress();
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            showLoadingProgress();
        }
    }
}
