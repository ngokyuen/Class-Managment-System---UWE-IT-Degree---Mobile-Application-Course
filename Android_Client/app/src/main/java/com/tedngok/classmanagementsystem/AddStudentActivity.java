package com.tedngok.classmanagementsystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class AddStudentActivity extends AppCompatActivity {

    WebSocketIO webSocketIO;

    private EditText editHKID, editStudentID,editFirstName, editLastName,editMobileNo,editHomeNo,editAddress, editEmail;
    private RadioGroup radioGender;
    private RadioButton radioMale,radioFemale;

    private ProgressBar loadProgress;
    private LinearLayout new_student_view;
    private Button btnSubmit;

    private boolean load_student_callback = false;
    private AsyncTask<JSONObject, Void, Boolean> mTask = null;
    private Common _common;

    private ImageView imgUser;

    private AlertDialog photoDialog;
    private LinearLayout takePhotoCaptureView, takePhotoFromFileView;

    private Spinner spinnerMinLevelType, spinnerMinLevelType2, spinnerMinLevelType3, spinnerMinLevelType4, spinnerMinLevelType5;
    private EditText editMinLevel, editMinLevel2, editMinLevel3, editMinLevel4, editMinLevel5;
    private LinearLayout conditionView2, conditionView3, conditionView5, conditionView4;

    private final int reqTakePhotoCapture = 1, reqTakePhotoFromFile = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        _common = new Common(this);

        editStudentID = (EditText) findViewById(R.id.editStudentID);
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

        setAction();
        setResponse();
//        mTask = new ProfileActivity.GetProfileTask().execute(new JSONObject());
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
                        _common.showMessage(new_student_view,getString(R.string.msg_selection_duplicate));
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
                        _common.showMessage(new_student_view,getString(R.string.msg_selection_duplicate));
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
                        _common.showMessage(new_student_view,getString(R.string.msg_selection_duplicate));
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
                        _common.showMessage(new_student_view, getString(R.string.msg_selection_duplicate));
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
                        _common.showMessage(new_student_view, getString(R.string.msg_selection_duplicate));
                    } else {
                        editMinLevel5.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout image_capture_method = (LinearLayout) getLayoutInflater().inflate(R.layout.capture_method, null);
                takePhotoCaptureView = (LinearLayout) image_capture_method.findViewById(R.id.takePhotoCaptureView);
//                takePhotoFromFileView = (LinearLayout) image_capture_method.findViewById(R.id.takePhotoFromFileView);

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

                photoDialog = new AlertDialog.Builder(AddStudentActivity.this).setView(image_capture_method).show();
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

                        json.put(getString(R.string.jsonHKID), editHKID.getText());
                        json.put(getString(R.string.jsonStudentID), editStudentID.getText());
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
                            JSONObject qualification = new JSONObject();
                            qualification.put(getString(R.string.jsonQualificationLevel), editMinLevel.getText().toString());
                            qualification.put(getString(R.string.jsonQualificationType), spinnerMinLevelType.getSelectedItem().toString());
                            qualifications.put(qualification);

                        }
                        if (spinnerMinLevelType2.getSelectedItemPosition() > 0 && !editMinLevel2.getText().toString().equals("")){
                            JSONObject qualification = new JSONObject();
                            qualification.put(getString(R.string.jsonQualificationLevel), editMinLevel2.getText().toString());
                            qualification.put(getString(R.string.jsonQualificationType), spinnerMinLevelType2.getSelectedItem().toString());
                            qualifications.put(qualification);
                        }
                        if (spinnerMinLevelType3.getSelectedItemPosition() > 0 && !editMinLevel3.getText().toString().equals("")){
                            JSONObject qualification = new JSONObject();
                            qualification.put(getString(R.string.jsonQualificationLevel), editMinLevel3.getText().toString());
                            qualification.put(getString(R.string.jsonQualificationType), spinnerMinLevelType3.getSelectedItem().toString());
                            qualifications.put(qualification);
                        }
                        if (spinnerMinLevelType4.getSelectedItemPosition() > 0 && !editMinLevel4.getText().toString().equals("")){
                            JSONObject qualification = new JSONObject();
                            qualification.put(getString(R.string.jsonQualificationLevel), editMinLevel4.getText().toString());
                            qualification.put(getString(R.string.jsonQualificationType), spinnerMinLevelType4.getSelectedItem().toString());
                            qualifications.put(qualification);
                        }
                        if (spinnerMinLevelType5.getSelectedItemPosition() > 0 && !editMinLevel5.getText().toString().equals("")){
                            JSONObject qualification = new JSONObject();
                            qualification.put(getString(R.string.jsonQualificationLevel), editMinLevel5.getText().toString());
                            qualification.put(getString(R.string.jsonQualificationType), spinnerMinLevelType5.getSelectedItem().toString());
                            qualifications.put(qualification);
                        }
                        json.put(getString(R.string.jsonQualifications), qualifications);


                        Bitmap bitmap = ((BitmapDrawable) imgUser.getDrawable()).getBitmap();
                        json.put(getString(R.string.jsonImage), _common.bitmapToBase64(bitmap));
                        mTask = new AddStudentTask().execute(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void setResponse() {
        webSocketIO._socket.off("addStudent");
        webSocketIO._socket.on("addStudent", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                showAddStudentSuccess();
                            } else {
                                _common.showErrorMessage(new_student_view,json);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showTryAgainError();
                        }
                        load_student_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == reqTakePhotoCapture) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                String img_base64 = _common.bitmapToBase64(bitmap);
                photoDialog.dismiss();
                imgUser.setImageBitmap(bitmap);

            }
        } else if (requestCode == reqTakePhotoFromFile) {
            if (resultCode == RESULT_OK) {
                photoDialog.dismiss();
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {

                }
            }
        }
    }

    public void showAddStudentSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(new_student_view, R.string.msg_add_success, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void showTryAgainError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(new_student_view, R.string.error_try_again_later, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showLoadingProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!load_student_callback) {
                    loadProgress.setVisibility(View.VISIBLE);
                    new_student_view.setVisibility(View.INVISIBLE);
                } else {
                    loadProgress.setVisibility(View.INVISIBLE);
                    new_student_view.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public class AddStudentTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {

            load_student_callback = false;
            showLoadingProgress();
            webSocketIO._socket.emit("addStudent", (JSONObject) params[0]);

            long starttime = System.currentTimeMillis();
            while (!load_student_callback && System.currentTimeMillis() - starttime < 10000) {
            }

            if (load_student_callback)
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                showTryAgainError();
                //Toast.makeText(getApplicationContext(), getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show();
            }
            load_student_callback = true;
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
