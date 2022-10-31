package com.tedngok.classmanagementsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class EditStudentActivity extends AppCompatActivity {

    private WebSocketIO _webSocketIO;

    private EditText editStudentID, editHKID, editFirstName, editLastName, editMobileNo, editHomeNo, editAddress, editEmail;
    private ImageView imgUser;
    private RadioGroup radioGender;
    private RadioButton radioMale, radioFemale;

    private ProgressBar loadProgress;
    private LinearLayout new_student_view;
    private Button btnSubmit, btnDelete;

    private Common _common;
    private String student_id;

    private Spinner spinnerMinLevelType, spinnerMinLevelType2, spinnerMinLevelType3, spinnerMinLevelType4, spinnerMinLevelType5;
    private EditText editMinLevel, editMinLevel2, editMinLevel3, editMinLevel4, editMinLevel5;
    private LinearLayout conditionView2, conditionView3, conditionView5, conditionView4;

    private boolean load_callback = false;
    private AsyncTask<?, Void, Boolean> mTask = null;
    private AsyncTask<?, ?, ?> mTask2 = null;

    private AlertDialog photoDialog;
    private LinearLayout takePhotoCaptureView, takePhotoFromFileView;

    private final int  reqTakePhotoFromFile = 2, reqDeleteStudent = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        _common = new Common(this);

        Bundle bundle = getIntent().getExtras();
        student_id = bundle.getString(getString(R.string.jsonStudentID));
//        Toast.makeText(this, bundle.getString(getString(R.string.jsonStudentID)), Toast.LENGTH_SHORT).show();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new_student_view = (LinearLayout) findViewById(R.id.new_student_view);
        editStudentID = (EditText) findViewById(R.id.editStudentID);
        editStudentID.setEnabled(false);
        editHKID = (EditText) findViewById(R.id.editHKID);
        editFirstName = (EditText) findViewById(R.id.editFirstName);
        editLastName = (EditText) findViewById(R.id.editLastName);
        editMobileNo = (EditText) findViewById(R.id.editMobileNo);
        editHomeNo = (EditText) findViewById(R.id.editHomeNo);
        radioGender = (RadioGroup) findViewById(R.id.radioGenderGroup);
        radioMale = (RadioButton) findViewById(R.id.radioMale);
        radioFemale = (RadioButton) findViewById(R.id.radioFemale);
        editAddress = (EditText) findViewById(R.id.editAddress);
        editEmail = (EditText) findViewById(R.id.editEmail);
        new_student_view = (LinearLayout) findViewById(R.id.new_student_view);
        loadProgress = (ProgressBar) findViewById(R.id.loadProgress);
        imgUser = (ImageView) findViewById(R.id.imgUser);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setVisibility(View.VISIBLE);

        conditionView2 = (LinearLayout) findViewById(R.id.conditionView2);
        conditionView3 = (LinearLayout) findViewById(R.id.conditionView3);
        conditionView4 = (LinearLayout) findViewById(R.id.conditionView4);
        conditionView5 = (LinearLayout) findViewById(R.id.conditionView5);
        spinnerMinLevelType = (Spinner) findViewById(R.id.spinner_min_level_type);
        spinnerMinLevelType2 = (Spinner) findViewById(R.id.spinner_min_level_type2);
        spinnerMinLevelType3 = (Spinner) findViewById(R.id.spinner_min_level_type3);
        spinnerMinLevelType4 = (Spinner) findViewById(R.id.spinner_min_level_type4);
        spinnerMinLevelType5 = (Spinner) findViewById(R.id.spinner_min_level_type5);
        conditionView2.setVisibility(View.GONE);
        conditionView3.setVisibility(View.GONE);
        conditionView4.setVisibility(View.GONE);
        conditionView5.setVisibility(View.GONE);
        editMinLevel = (EditText) findViewById(R.id.editMinLevel);
        editMinLevel2 = (EditText) findViewById(R.id.editMinLevel2);
        editMinLevel3 = (EditText) findViewById(R.id.editMinLevel3);
        editMinLevel4 = (EditText) findViewById(R.id.editMinLevel4);
        editMinLevel5 = (EditText) findViewById(R.id.editMinLevel5);

        setRepsonse();
        setAction();
        mTask = new GetStudentTask().execute(student_id);
    }

    private void setRepsonse() {
        _webSocketIO._socket.off("deleteStudent");
        _webSocketIO._socket.on("deleteStudent", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {

                                _common.showMessage(new_student_view,getString(R.string.msg_delete_student_success));
                                finish();
                            } else {
                                _common.showErrorMessage(new_student_view,null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(new_student_view,null);
                        }
                    }
                });
            }
        });

        _webSocketIO._socket.off("uploadStudentProfileImage");
        _webSocketIO._socket.on("uploadStudentProfileImage", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {

                                _common.showMessage(new_student_view, R.string.msg_update_image_success);
                            } else {
                                _common.showErrorMessage(new_student_view,null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(new_student_view,null);
                        }
                    }
                });
            }
        });

        _webSocketIO._socket.off("editStudent");
        _webSocketIO._socket.on("editStudent", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                _common.showMessage(new_student_view,R.string.msg_update_success);
                            } else {
                                _common.showErrorMessage(new_student_view,null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(new_student_view,null);
                        }
                        load_callback = true;
                        _common.showLoadingProgress(new_student_view, loadProgress, load_callback);
                    }
                });
            }
        });

        _webSocketIO._socket.off("getStudentByStudentId");
        _webSocketIO._socket.on("getStudentByStudentId", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject response = (JSONObject) args[0];
                        JSONObject user = null;
                        try {
                            user = (JSONObject) response.getJSONObject("student");
                            if (user.has(getString(R.string.jsonStudentID))) {
                                editStudentID.setText(user.getString(getString(R.string.jsonStudentID)));
                            }
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
                                imgUser.setImageBitmap(_common.base64ToBitmap(user.getString(getString(R.string.jsonImage))));
                            }

                            if(user.has(getString(R.string.jsonQualifications))){
                                JSONArray qualifications = user.getJSONArray(getString(R.string.jsonQualifications));
//                                Toast.makeText(EditStudentActivity.this, qualifications.toString(), Toast.LENGTH_LONG).show();

                                for(int i =0; i < qualifications.length(); i++){
                                    JSONObject qualification = qualifications.getJSONObject(i);
                                    String type = qualification.getString(getString(R.string.jsonQualificationType));
                                    int level = qualification.getInt(getString(R.string.jsonQualificationLevel));

                                    switch (i){
                                        case 0:
                                            spinnerMinLevelType.setSelection(_common.getMinLevelType(type));
                                            editMinLevel.setText(String.valueOf(level));
                                            break;
                                        case 1:
                                            spinnerMinLevelType2.setSelection(_common.getMinLevelType(type));
                                            editMinLevel2.setText(String.valueOf(level));
                                            break;
                                        case 2:
                                            spinnerMinLevelType3.setSelection(_common.getMinLevelType(type));
                                            editMinLevel3.setText(String.valueOf(level));
                                            break;
                                        case 3:
                                            spinnerMinLevelType4.setSelection(_common.getMinLevelType(type));
                                            editMinLevel4.setText(String.valueOf(level));
                                            break;
                                        case 4:
                                            spinnerMinLevelType5.setSelection(_common.getMinLevelType(type));
                                            editMinLevel5.setText(String.valueOf(level));
                                            break;
                                    }

                                }

                            }
                            load_callback = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            load_callback = false;
                            _common.showErrorMessage(new_student_view,null);
                            finish();
                        }
                        _common.showLoadingProgress(new_student_view, loadProgress, load_callback);
                    }
                });
            }

        });
    }

    private void setAction() {

        spinnerMinLevelType.setAdapter(_common.getMinLevelTypeAdpater());
        spinnerMinLevelType2.setAdapter(_common.getMinLevelTypeAdpater());
        spinnerMinLevelType3.setAdapter(_common.getMinLevelTypeAdpater());
        spinnerMinLevelType4.setAdapter(_common.getMinLevelTypeAdpater());
        spinnerMinLevelType5.setAdapter(_common.getMinLevelTypeAdpater());

        spinnerMinLevelType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevel.setEnabled(false);
                } else {


                    if (spinnerMinLevelType.getSelectedItemPosition() == spinnerMinLevelType2.getSelectedItemPosition() ||
                            spinnerMinLevelType.getSelectedItemPosition() == spinnerMinLevelType3.getSelectedItemPosition() ||
                            spinnerMinLevelType.getSelectedItemPosition() == spinnerMinLevelType4.getSelectedItemPosition() ||
                            spinnerMinLevelType.getSelectedItemPosition() == spinnerMinLevelType5.getSelectedItemPosition()
                            ){
                        spinnerMinLevelType.setSelection(0);
                        _common.showMessage(new_student_view,"Field 1:" + getString(R.string.msg_selection_duplicate));
                    } else {
                        conditionView2.setVisibility(View.VISIBLE);
                        editMinLevel.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMinLevelType2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevel2.setEnabled(false);
                } else {

                    if (spinnerMinLevelType2.getSelectedItemPosition() == spinnerMinLevelType.getSelectedItemPosition() ||
                            spinnerMinLevelType2.getSelectedItemPosition() == spinnerMinLevelType3.getSelectedItemPosition() ||
                            spinnerMinLevelType2.getSelectedItemPosition() == spinnerMinLevelType4.getSelectedItemPosition() ||
                            spinnerMinLevelType2.getSelectedItemPosition() == spinnerMinLevelType5.getSelectedItemPosition()
                            ){
                        spinnerMinLevelType2.setSelection(0);
                        _common.showMessage(new_student_view,"Field 2:" + getString(R.string.msg_selection_duplicate));
                    } else {
                        conditionView3.setVisibility(View.VISIBLE);
                        editMinLevel2.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMinLevelType3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevel3.setEnabled(false);
                } else {

                    if (spinnerMinLevelType3.getSelectedItemPosition() == spinnerMinLevelType.getSelectedItemPosition() ||
                            spinnerMinLevelType3.getSelectedItemPosition() == spinnerMinLevelType2.getSelectedItemPosition() ||
                            spinnerMinLevelType3.getSelectedItemPosition() == spinnerMinLevelType4.getSelectedItemPosition() ||
                            spinnerMinLevelType3.getSelectedItemPosition() == spinnerMinLevelType5.getSelectedItemPosition()
                            ){
                        spinnerMinLevelType3.setSelection(0);
                        _common.showMessage(new_student_view,"Field 3:" + getString(R.string.msg_selection_duplicate));
                    } else {
                        conditionView4.setVisibility(View.VISIBLE);
                        editMinLevel3.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMinLevelType4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {

                } else {
                    if (spinnerMinLevelType4.getSelectedItemPosition() == spinnerMinLevelType.getSelectedItemPosition() ||
                            spinnerMinLevelType4.getSelectedItemPosition() == spinnerMinLevelType2.getSelectedItemPosition() ||
                            spinnerMinLevelType4.getSelectedItemPosition() == spinnerMinLevelType3.getSelectedItemPosition() ||
                            spinnerMinLevelType4.getSelectedItemPosition() == spinnerMinLevelType5.getSelectedItemPosition()
                            ) {
                        spinnerMinLevelType4.setSelection(0);
                        _common.showMessage(new_student_view,"Field 4:" +  getString(R.string.msg_selection_duplicate));
                    } else {
                        conditionView5.setVisibility(View.VISIBLE);
                        editMinLevel4.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerMinLevelType5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevel5.setEnabled(false);
                } else {
                    if (spinnerMinLevelType5.getSelectedItemPosition() == spinnerMinLevelType.getSelectedItemPosition() ||
                            spinnerMinLevelType5.getSelectedItemPosition() == spinnerMinLevelType2.getSelectedItemPosition() ||
                            spinnerMinLevelType5.getSelectedItemPosition() == spinnerMinLevelType3.getSelectedItemPosition() ||
                            spinnerMinLevelType5.getSelectedItemPosition() == spinnerMinLevelType4.getSelectedItemPosition()
                            ) {
                        spinnerMinLevelType5.setSelection(0);
                        _common.showMessage(new_student_view,"Field 5:" +  getString(R.string.msg_selection_duplicate));
                    } else {
                        editMinLevel5.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(EditStudentActivity.this)
                        .setTitle(getString(R.string.title_delete_student))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    JSONObject json = new JSONObject();
                                    json.put(getString(R.string.jsonStudentID), student_id);
                                    json.put(getString(R.string.jsonToken), _common.getToken());
                                    new DeleteStudentTask().execute(json);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout image_capture_method = (LinearLayout) getLayoutInflater().inflate(R.layout.capture_method, null);
                takePhotoCaptureView = (LinearLayout) image_capture_method.findViewById(R.id.takePhotoCaptureView);
//                takePhotoFromFileView = (LinearLayout) capture_method.findViewById(R.id.takePhotoFromFileView);

                photoDialog = new AlertDialog.Builder(EditStudentActivity.this).setView(image_capture_method).show();
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

                boolean verification = true;

                if (editStudentID.getText().toString().equals("")) {
                    editStudentID.setError(getString(R.string.error_empty));
                    verification &= false;
                }
                if (editHKID.getText().toString().equals("")) {
                    editHKID.setError(getString(R.string.error_empty));
                    verification &= false;
                }
                if (editFirstName.getText().toString().equals("")) {
                    editFirstName.setError(getString(R.string.error_empty));
                    verification &= false;
                }
                if (editLastName.getText().toString().equals("")) {
                    editLastName.setError(getString(R.string.error_empty));
                    verification &= false;
                }

                String email = editEmail.getText().toString();
                String mobile = editMobileNo.getText().toString();
                String home = editHomeNo.getText().toString();

                if (email.equals("") && mobile.equals("") && home.equals("")) {
                    if (email.equals("")) {
                        editEmail.setError(getString(R.string.error_empty));
                        verification &= false;
                    } else if (mobile.equals("")) {
                        editMobileNo.setError(getString(R.string.error_empty));
                        verification &= false;
                    } else if (home.equals("")) {
                        editHomeNo.setError(getString(R.string.error_empty));
                        verification &= false;
                    }
                }

                if (!_common.isEmailValid(editEmail.getText().toString())){
                    editEmail.setError(getString(R.string.error_invalid_email));
                    verification &=false;
                }

                if (spinnerMinLevelType.getSelectedItemPosition() != 0 && editMinLevel.getText().toString().equals("")) {
                    editMinLevel.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (! editMinLevel.getText().toString().equals("") && !_common.isQualificationValueValid( editMinLevel.getText().toString())) {
                    verification &= false;
                    editMinLevel.setError(getString(R.string.msg_error_min_level_value));
                } else {
                    editMinLevel.setError(null);
                }

                if (spinnerMinLevelType2.getSelectedItemPosition() != 0 && editMinLevel2.getText().toString().equals("")) {
                    editMinLevel2.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (! editMinLevel2.getText().toString().equals("") && !_common.isQualificationValueValid( editMinLevel2.getText().toString())) {
                    verification &= false;
                    editMinLevel2.setError(getString(R.string.msg_error_min_level_value));
                } else {
                    editMinLevel2.setError(null);
                }

                if (spinnerMinLevelType3.getSelectedItemPosition() != 0 && editMinLevel3.getText().toString().equals("")) {
                    editMinLevel3.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (! editMinLevel3.getText().toString().equals("") && !_common.isQualificationValueValid( editMinLevel3.getText().toString())) {
                    verification &= false;
                    editMinLevel3.setError(getString(R.string.msg_error_min_level_value));
                } else {
                    editMinLevel3.setError(null);
                }

                if (spinnerMinLevelType4.getSelectedItemPosition() != 0 && editMinLevel4.getText().toString().equals("")) {
                    editMinLevel4.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (! editMinLevel4.getText().toString().equals("") && !_common.isQualificationValueValid( editMinLevel4.getText().toString())) {
                    verification &= false;
                    editMinLevel4.setError(getString(R.string.msg_error_min_level_value));
                } else {
                    editMinLevel4.setError(null);
                }

                if (spinnerMinLevelType5.getSelectedItemPosition() != 0 && editMinLevel5.getText().toString().equals("")) {
                    editMinLevel5.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (! editMinLevel5.getText().toString().equals("") && !_common.isQualificationValueValid( editMinLevel5.getText().toString())) {
                    verification &= false;
                    editMinLevel5.setError(getString(R.string.msg_error_min_level_value));
                } else {
                    editMinLevel5.setError(null);
                }

                if (verification) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(getString(R.string.jsonToken), _common.getToken());
                        Bundle bundle = getIntent().getExtras();
                        String student_id = bundle.getString(getString(R.string.jsonStudentID));
                        json.put(getString(R.string.jsonStudentID), student_id);

                        json.put(getString(R.string.jsonHKID), editHKID.getText());
                        //json.put(getString(R.string.jsonStudentID), editStudentID.getText());
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


                        JSONArray qualifications = new JSONArray();
                        if (spinnerMinLevelType.getSelectedItemPosition() > 0 && !editMinLevel.getText().toString().equals("")){
                            JSONObject q = new JSONObject();
                            q.put(getString(R.string.jsonQualificationLevel), editMinLevel.getText().toString());
                            q.put(getString(R.string.jsonQualificationType), spinnerMinLevelType.getSelectedItem().toString());
                            qualifications.put(q);

                        }
                        if (spinnerMinLevelType2.getSelectedItemPosition() > 0 && !editMinLevel2.getText().toString().equals("")){
                            JSONObject q = new JSONObject();
                            q.put(getString(R.string.jsonQualificationLevel), editMinLevel2.getText().toString());
                            q.put(getString(R.string.jsonQualificationType), spinnerMinLevelType2.getSelectedItem().toString());
                            qualifications.put(q);
                        }
                        if (spinnerMinLevelType3.getSelectedItemPosition() > 0 && !editMinLevel3.getText().toString().equals("")){
                            JSONObject q = new JSONObject();
                            q.put(getString(R.string.jsonQualificationLevel), editMinLevel3.getText().toString());
                            q.put(getString(R.string.jsonQualificationType), spinnerMinLevelType3.getSelectedItem().toString());
                            qualifications.put(q);
                        }
                        if (spinnerMinLevelType4.getSelectedItemPosition() > 0 && !editMinLevel4.getText().toString().equals("")){
                            JSONObject q = new JSONObject();
                            q.put(getString(R.string.jsonQualificationLevel), editMinLevel4.getText().toString());
                            q.put(getString(R.string.jsonQualificationType), spinnerMinLevelType4.getSelectedItem().toString());
                            qualifications.put(q);
                        }
                        if (spinnerMinLevelType5.getSelectedItemPosition() > 0 && !editMinLevel5.getText().toString().equals("")){
                            JSONObject q = new JSONObject();
                            q.put(getString(R.string.jsonQualificationLevel), editMinLevel5.getText().toString());
                            q.put(getString(R.string.jsonQualificationType), spinnerMinLevelType5.getSelectedItem().toString());
                            qualifications.put(q);
                        }
                        json.put(getString(R.string.jsonQualifications), qualifications);

                        mTask = new editStudentTask().execute(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public class editStudentTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {

            load_callback = false;
            _common.showLoadingProgress(new_student_view, loadProgress, load_callback);
            _webSocketIO._socket.emit("editStudent", (JSONObject) params[0]);

            long starttime = System.currentTimeMillis();
            while (!load_callback && System.currentTimeMillis() - starttime < 10000) {
            }

            if (load_callback)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                _common.showErrorMessage(new_student_view,null);
                //Toast.makeText(getApplicationContext(), getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show();
            }
            load_callback = true;
            mTask = null;
            _common.showLoadingProgress(new_student_view, loadProgress, load_callback);
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            _common.showLoadingProgress(new_student_view, loadProgress, load_callback);
        }
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
                    json.put(getString(R.string.jsonStudentID), student_id);

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

    public class DeleteStudentTask extends AsyncTask<JSONObject, Void, Boolean>{

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            JSONObject json = (JSONObject) params[0];
            _webSocketIO._socket.emit("deleteStudent", json);
            return null;
        }
    }

    public class GetStudentTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            load_callback = false;
            _common.showLoadingProgress(new_student_view, loadProgress, load_callback);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(getString(R.string.jsonStudentID), params[0]);
                jsonObject.put(getString(R.string.jsonToken), _common.getToken());
                _webSocketIO._socket.emit("getStudentByStudentId", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            long starttime = System.currentTimeMillis();
            while (!load_callback && System.currentTimeMillis() - starttime < 10000) {
            }

            if (load_callback)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
//                Toast.makeText(EditStudentActivity.this, R.string.error_try_again_later, Toast.LENGTH_SHORT).show();
                finish();
                //showTryAgainError();
            }
            load_callback = true;
            mTask = null;
            _common.showLoadingProgress(new_student_view, loadProgress, load_callback);
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            _common.showLoadingProgress(new_student_view, loadProgress, load_callback);
        }
    }

    public class UploadPhotoTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            JSONObject json = (JSONObject) params[0];
            _webSocketIO._socket.emit("uploadStudentProfileImage", json);
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

//    public void showUpdateSuccess(final String msg) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (msg != null) {
//                    Snackbar.make(new_student_view, msg, Snackbar.LENGTH_SHORT).show();
//                } else {
//                    Snackbar.make(new_student_view, R.string.msg_update_success, Snackbar.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }
//
//    private void showTryAgainError() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Snackbar.make(loadProgress, R.string.error_try_again_later, Snackbar.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void showLoadingProgress() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (!load_callback) {
//                    loadProgress.setVisibility(View.VISIBLE);
//                    new_student_view.setVisibility(View.INVISIBLE);
//                } else {
//                    loadProgress.setVisibility(View.INVISIBLE);
//                    new_student_view.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//    }

//    public void showEmptyResultError() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Snackbar.make(new_student_view, R.string.error_empty_result, Snackbar.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void showLoadSuccess() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                Snackbar.make(progressBar,R.string.msg_update_success, Snackbar.LENGTH_SHORT).show();
//            }
//        });
//    }

    @Override
    public boolean onSupportNavigateUp() {
        return _common.onSupportNavigateUp();
    }
}
