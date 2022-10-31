package com.tedngok.classmanagementsystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Calendar;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class AddClassActivity extends AppCompatActivity {

    private WebSocketIO webSocketIO;

    private TextView txtMap, txtStartDateValue, txtEndDateValue, txtStartTimeValue, txtEndTimeValue, txtStartTime, txtEndTime;
    private EditText editAddress, editClassID, editName, editPrice, editMaxStudents, editNoOfLession;
    private EditText editMinLevel, editMinLevel2, editMinLevel3;
    private Spinner spinnerMinLevelType, spinnerMinLevelType2, spinnerMinLevelType3;
    private ImageView imgMap;
    private LinearLayout statusView, otherView, conditionView2, conditionView3;
    private TextInputLayout maxStudentsView;
    private RadioGroup radioType;
    private RadioButton radioIndividual, radioSmall;
    private ProgressBar progressBar;
    private LinearLayout new_view;
    private Button btnSubmit, btnAddTeacher;
    private CheckBox chkWeekly;

    private boolean load_callback = false;
    private AsyncTask<JSONObject, Void, Boolean> mTask = null;
    private Common _common;

    private Double latitude = 0.0, longitude = 0.0;

    public static final int reqAddAddress = 1, resAddAddressSuccess = 1, resAddAddressFail = 2;
    private AlertDialog alertDialog;

    private JSONArray _teachers;
    private RecyclerView teachersView;
    private RecyclerView.Adapter _list_adapter;
    private RecyclerView.LayoutManager list_layout_manger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        _common = new Common(this);

        list_layout_manger = new GridLayoutManager(this,1);
        teachersView = (RecyclerView) findViewById(R.id.teachersView);
        teachersView.setLayoutManager(list_layout_manger);

        txtMap = (TextView) findViewById(R.id.txtMap);
        txtStartDateValue = (TextView) findViewById(R.id.txtStartDateValue);
        txtEndDateValue = (TextView) findViewById(R.id.txtEndDateValue);
        txtStartTimeValue = (TextView) findViewById(R.id.txtStartTimeValue);
        txtEndTimeValue = (TextView) findViewById(R.id.txtEndTimeValue);
        txtStartTime = (TextView) findViewById(R.id.txtStartTime);
        txtEndTime = (TextView) findViewById(R.id.txtEndTime);
        editNoOfLession = (EditText) findViewById(R.id.editNoOfLession);
        editClassID = (EditText) findViewById(R.id.editClassID);
        editName = (EditText) findViewById(R.id.editName);
        imgMap = (ImageView) findViewById(R.id.imgMap);
        editAddress = (EditText) findViewById(R.id.editAddress);
        editPrice = (EditText) findViewById(R.id.editPrice);
        editMinLevel = (EditText) findViewById(R.id.editMinLevel);
        editMinLevel2 = (EditText) findViewById(R.id.editMinLevel2);
        editMinLevel3 = (EditText) findViewById(R.id.editMinLevel3);
        spinnerMinLevelType = (Spinner) findViewById(R.id.spinner_min_level_type);
        spinnerMinLevelType2 = (Spinner) findViewById(R.id.spinner_min_level_type2);
        spinnerMinLevelType3 = (Spinner) findViewById(R.id.spinner_min_level_type3);
        editMaxStudents = (EditText) findViewById(R.id.editMaxStudents);
        maxStudentsView = (TextInputLayout) findViewById(R.id.max_students_view);
        statusView = (LinearLayout) findViewById(R.id.status_view);
        otherView = (LinearLayout) findViewById(R.id.other_view);
        conditionView2 = (LinearLayout) findViewById(R.id.conditionView2);
        conditionView3 = (LinearLayout) findViewById(R.id.conditionView3);
        radioType = (RadioGroup) findViewById(R.id.radioType);
        radioIndividual = (RadioButton) findViewById(R.id.radioIndividual);
        radioSmall = (RadioButton) findViewById(R.id.radioSmall);
        chkWeekly = (CheckBox) findViewById(R.id.chkWeekly);
        new_view = (LinearLayout) findViewById(R.id.new_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnAddTeacher = (Button) findViewById(R.id.btnAddTeacher);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        setButtonAction();
        setAction();
        setResponse();
    }

    private void setAction() {
        _teachers = new JSONArray();
        _list_adapter = new ListAdapter(this);
        teachersView.setAdapter(_list_adapter);


        editMinLevel.setEnabled(false);
        editMinLevel2.setEnabled(false);
        editMinLevel3.setEnabled(false);
        statusView.setVisibility(View.GONE);
        maxStudentsView.setVisibility(View.GONE);
        otherView.setVisibility(View.GONE);
        chkWeekly.setVisibility(View.GONE);
        conditionView2.setVisibility(View.GONE);
        conditionView3.setVisibility(View.GONE);
        txtStartTimeValue.setVisibility(View.GONE);
        txtEndTimeValue.setVisibility(View.GONE);
        txtStartTime.setVisibility(View.GONE);
        txtEndTime.setVisibility(View.GONE);
        spinnerMinLevelType.setAdapter(_common.getMinLevelTypeAdpater());
        spinnerMinLevelType2.setAdapter(_common.getMinLevelTypeAdpater());
        spinnerMinLevelType3.setAdapter(_common.getMinLevelTypeAdpater());
        spinnerMinLevelType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevel.setEnabled(false);
                } else {

                    if (spinnerMinLevelType.getSelectedItemPosition() == spinnerMinLevelType2.getSelectedItemPosition() || spinnerMinLevelType.getSelectedItemPosition() == spinnerMinLevelType3.getSelectedItemPosition()) {
                        spinnerMinLevelType.setSelection(0);
                        _common.showMessage(new_view, getString(R.string.msg_selection_duplicate));
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

                    if (spinnerMinLevelType2.getSelectedItemPosition() == spinnerMinLevelType.getSelectedItemPosition() || spinnerMinLevelType2.getSelectedItemPosition() == spinnerMinLevelType3.getSelectedItemPosition()) {
                        spinnerMinLevelType2.setSelection(0);
                        _common.showMessage(new_view, getString(R.string.msg_selection_duplicate));
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

                    if (spinnerMinLevelType3.getSelectedItemPosition() == spinnerMinLevelType.getSelectedItemPosition() || spinnerMinLevelType3.getSelectedItemPosition() == spinnerMinLevelType2.getSelectedItemPosition()) {
                        spinnerMinLevelType3.setSelection(0);
                        _common.showMessage(new_view, getString(R.string.msg_selection_duplicate));
                    } else {
                        editMinLevel3.setEnabled(true);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        radioType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == radioIndividual.getId()) {
                    maxStudentsView.setVisibility(View.GONE);
                } else if (checkedId == radioSmall.getId()) {
                    maxStudentsView.setVisibility(View.VISIBLE);
                }
            }
        });

        txtEndDateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                final int mYear = c.get(Calendar.YEAR);
                final int mMonth = c.get(Calendar.MONTH);
                final int mDay = c.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(AddClassActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        me.setText(String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(dayOfMonth));
                        enable();
                    }
                }, mYear, mMonth, mDay).show();
            }
        });

        txtStartDateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                final int mYear = c.get(Calendar.YEAR);
                final int mMonth = c.get(Calendar.MONTH);
                final int mDay = c.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(AddClassActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        me.setText(String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(dayOfMonth));
                        enable();
                    }
                }, mYear, mMonth, mDay).show();
            }
        });

        txtStartTimeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                new TimePickerDialog(AddClassActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        me.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                        enable();
                    }
                }, 0, 0, true).show();
            }
        });

        txtEndTimeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                new TimePickerDialog(AddClassActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        me.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                        enable();
                    }
                }, 0, 0, true).show();
            }
        });

        imgMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AddClassActivity.this, MapsActivity.class);
                startActivityForResult(i, reqAddAddress);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == reqAddAddress) {
            if (resultCode == resAddAddressSuccess) {
                latitude = data.getDoubleExtra(getString(R.string.jsonLat), 0.0);
                longitude = data.getDoubleExtra(getString(R.string.jsonLng), 0.0);
                String address = data.getStringExtra(getString(R.string.jsonAddress));
                if (editAddress.getText().toString().equals("")) {
                    editAddress.setText(address);
                }
            }
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }

    private void enable() {
        if (!txtStartDateValue.getText().toString().equals("")) {
            txtStartTimeValue.setVisibility(View.VISIBLE);
            txtStartTime.setVisibility(View.VISIBLE);
        }
        if (!txtEndDateValue.getText().toString().equals("")) {
            txtEndTimeValue.setVisibility(View.VISIBLE);
            txtEndTime.setVisibility(View.VISIBLE);
        }

        if (!txtEndDateValue.getText().toString().equals("") && !txtStartDateValue.getText().toString().equals("") &&
                !txtStartTimeValue.getText().toString().equals("") && !txtEndTimeValue.getText().toString().equals("")) {
            otherView.setVisibility(View.VISIBLE);
            chkWeekly.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonAction() {
        btnAddTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = getLayoutInflater().inflate(R.layout.dialog_search_teacher, null);
                webSocketIO._socket.off("getUserByName");
                webSocketIO._socket.on("getUserByName", new Emitter.Listener() {
                    @Override
                    public void call(final Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                GridLayoutManager list_layout_manger2 = new GridLayoutManager(AddClassActivity.this,3);
                                RecyclerView search_teachers_view = (RecyclerView) view.findViewById(R.id.search_teachers_view);
                                search_teachers_view.setLayoutManager(list_layout_manger2);
                                try {
                                    JSONObject json = (JSONObject) args[0];
                                    search_teachers_view.setAdapter(new ListAdapter2(AddClassActivity.this,json.getJSONArray(getString(R.string.jsonUsers))));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                Button btnSearch = (Button) view.findViewById(R.id.btnSearch);
                final EditText editTeacherName = (EditText) view.findViewById(R.id.editTeacherName);
                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject json = new JSONObject();
                            json.put(getString(R.string.jsonToken), _common.getToken());
                            json.put(getString(R.string.jsonQuery), editTeacherName.getText().toString());
                            webSocketIO._socket.emit("getUserByName", json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                alertDialog = new AlertDialog.Builder(AddClassActivity.this)
                        .setTitle(getString(R.string.alert_title))
                        .setView(view)
                        .show();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean verification = true;


                if (editClassID.getText().toString().equals("")) {
                    editClassID.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    editClassID.setError(null);
                }

                if (editName.getText().toString().equals("")) {
                    editName.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    editName.setError(null);
                }

                if (editAddress.getText().toString().equals("")) {
                    editAddress.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    editAddress.setError(null);
                }

                if (latitude == 0.0 && longitude == 0.0) {
                    txtMap.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    txtMap.setError(null);
                }

                if (txtStartDateValue.getText().toString().equals("")) {
                    txtStartDateValue.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    txtStartDateValue.setError(null);
                }

                if (txtEndDateValue.getText().toString().equals("")) {
                    txtEndDateValue.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    txtEndDateValue.setError(null);
                }

                if (txtEndTimeValue.getText().toString().equals("")) {
                    txtEndTimeValue.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    txtEndTimeValue.setError(null);
                }

                if (txtStartTimeValue.getText().toString().equals("")) {
                    txtStartTimeValue.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    txtStartTimeValue.setError(null);
                }

                if (editNoOfLession.getText().toString().equals("")) {
                    editNoOfLession.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (Integer.parseInt(editNoOfLession.getText().toString()) > 99 || Integer.parseInt(editNoOfLession.getText().toString()) == 0) {
                    editNoOfLession.setError(getString(R.string.error_more_99));
                    verification &= false;
                } else {
                    editNoOfLession.setError(null);
                }

                if (radioSmall.isChecked() && editMaxStudents.getText().toString().equals("")) {
                    editMaxStudents.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    editMaxStudents.setError(null);
                }

                if (spinnerMinLevelType.getSelectedItemPosition() != 0 && editMinLevel.getText().toString().equals("")) {
                    editMinLevel.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (!_common.isQualificationValueValid(editMinLevel.getText().toString())) {
                    editMinLevel.setError(getString(R.string.msg_error_min_level_value));
                    verification &= false;
                } else {
                    editMinLevel.setError(null);
                }

                if (spinnerMinLevelType2.getSelectedItemPosition() != 0 && editMinLevel2.getText().toString().equals("")) {
                    editMinLevel2.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (!_common.isQualificationValueValid(editMinLevel2.getText().toString())) {
                    editMinLevel2.setError(getString(R.string.msg_error_min_level_value));
                    verification &= false;
                } else {
                    editMinLevel2.setError(null);
                }

                if (spinnerMinLevelType3.getSelectedItemPosition() != 0 && editMinLevel3.getText().toString().equals("")) {
                    editMinLevel3.setError(getString(R.string.error_empty));
                    verification &= false;
                } else if (!_common.isQualificationValueValid(editMinLevel3.getText().toString())) {
                    editMinLevel3.setError(getString(R.string.msg_error_min_level_value));
                    verification &= false;
                } else {
                    editMinLevel3.setError(null);
                }

                if (editPrice.getText().toString().equals("")) {
                    editPrice.setError(getString(R.string.error_empty));
                    verification &= false;
                } else {
                    editPrice.setError(null);
                }


                if (!_common.isDateTimeFromTo(txtStartDateValue.getText().toString(),txtEndDateValue.getText().toString())){
                    verification  &= false;
                    txtEndDateValue.setError(getString(R.string.msg_error_time_from_to));
                } else {
                    txtEndDateValue.setError(null);
                }


                if (!_common.isTimeFromTo(txtStartTimeValue.getText().toString(),txtEndTimeValue.getText().toString())){
                    verification = false;
                    txtEndTimeValue.setError(getString(R.string.msg_error_time_from_to));
                } else {
                    txtEndTimeValue.setError(null);
                }

                if (verification) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put(getString(R.string.jsonToken), _common.getToken());

                        json.put(getString(R.string.jsonClassID), editClassID.getText());
                        json.put(getString(R.string.jsonClassName), editName.getText());
                        json.put(getString(R.string.jsonLat), latitude);
                        json.put(getString(R.string.jsonLng), longitude);
                        json.put(getString(R.string.jsonAddress), editAddress.getText());
                        json.put(getString(R.string.jsonPrice), editPrice.getText());
                        json.put(getString(R.string.jsonStartDate), txtStartDateValue.getText());
                        json.put(getString(R.string.jsonEndDate), txtEndDateValue.getText());
//                        String[] startTime = txtStartTimeValue.getText().toString().split(":");
//                        int startHour = Integer.valueOf(startTime[0]);
//                        int startMins = Integer.valueOf(startTime[1]);
//                        json.put(getString(R.string.jsonStartTime), startHour * 3600 + startMins * 60);

                        json.put(getString(R.string.jsonStartTime), _common.dbTimeStringToSecond( txtStartTimeValue.getText().toString()));

//                        String[] endTime = txtEndTimeValue.getText().toString().split(":");
//                        int endHour = Integer.valueOf(endTime[0]);
//                        int endMins = Integer.valueOf(endTime[1]);
//                        json.put(getString(R.string.jsonEndTime), endHour * 3600 + endMins * 60);

                        json.put(getString(R.string.jsonEndTime), _common.dbTimeStringToSecond( txtEndTimeValue.getText().toString()));


                        json.put(getString(R.string.jsonNoOfLession), editNoOfLession.getText());
                        if (spinnerMinLevelType.getSelectedItemPosition() != 0 && !editMinLevel.getText().toString().equals("")) {
                            json.put(getString(R.string.jsonMinLevelType), spinnerMinLevelType.getSelectedItem().toString());
                            json.put(getString(R.string.jsonMinLevel), Integer.valueOf(editMinLevel.getText().toString()));
                        }
                        if (spinnerMinLevelType2.getSelectedItemPosition() != 0 && !editMinLevel2.getText().toString().equals("")) {
                            json.put(getString(R.string.jsonMinLevelType2), spinnerMinLevelType2.getSelectedItem().toString());
                            json.put(getString(R.string.jsonMinLevel2), Integer.valueOf(editMinLevel2.getText().toString()));
                        }
                        if (spinnerMinLevelType3.getSelectedItemPosition() != 0 && !editMinLevel3.getText().toString().equals("")) {
                            json.put(getString(R.string.jsonMinLevelType3), spinnerMinLevelType3.getSelectedItem().toString());
                            json.put(getString(R.string.jsonMinLevel3), Integer.valueOf(editMinLevel3.getText().toString()));
                        }

                        if (radioIndividual.isChecked()) {
                            json.put(getString(R.string.jsonType), getString(R.string.jsonTypeIndividual));
                        } else if (radioSmall.isChecked()) {
                            json.put(getString(R.string.jsonType), getString(R.string.jsonTypeSmall));
                            json.put(getString(R.string.jsonMaxStudents), editMaxStudents.getText());
                        }

                        if (chkWeekly.isChecked()){
                            json.put(getString(R.string.jsonWeekly), true);
                        } else {
                            json.put(getString(R.string.jsonWeekly), false);
                        }

                        json.put(getString(R.string.jsonTeachers), _teachers);

                        mTask = new AddClassTask().execute(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setResponse() {


        webSocketIO._socket.off("addClassWeekly");
        webSocketIO._socket.on("addClassWeekly", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                _common.showMessage(new_view, R.string.msg_add_success);
//                                if (json.has(getString(R.string.jsonClass))){
//                                    JSONObject class_ = json.getJSONObject(getString(R.string.jsonClass));
//                                    String id_ = class_.getString(getString(R.string.jsonID));
//                                    String startDate_ = (String) class_.get(getString(R.string.jsonStartDate));
//                                    int startTime_ = class_.getInt(getString(R.string.jsonStartTime));
//                                    String endDate_ = (String) class_.get(getString(R.string.jsonEndDate));
//                                    int endTime_ = class_.getInt(getString(R.string.jsonEndTime));
//                                    int no_of_lession_ = class_.getInt(getString(R.string.no_of_lession));
//
//                                    for (int i=0; i< no_of_lession_;i++){
//                                        JSONObject resJSON = new JSONObject();
//                                        resJSON.put(getString(R.string.jsonID), id_);
//                                        resJSON.put(getString(R.string.jsonStartDate), startDate_);
//                                        resJSON.put(getString(R.string.jsonStartTime), startTime_);
//                                        resJSON.put(getString(R.string.jsonEndDate), endDate_);
//                                        resJSON.put(getString(R.string.jsonEndTime), endTime_);
//                                        webSocketIO._socket.emit("addClassSchedule", resJSON);
//                                    }
//
//                                }
                            } else {
                                _common.showErrorMessage(new_view, json);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(new_view, null);
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });

        webSocketIO._socket.off("addClass");
        webSocketIO._socket.on("addClass", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                _common.showMessage(new_view, R.string.msg_add_success);
                            } else {
                                _common.showErrorMessage(new_view, json);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(new_view, null);
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return _common.onSupportNavigateUp();
    }

    private void showLoadingProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!load_callback) {
                    progressBar.setVisibility(View.VISIBLE);
                    new_view.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    new_view.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public class AddClassTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {

            load_callback = false;
            showLoadingProgress();
            webSocketIO._socket.emit("addClass", (JSONObject) params[0]);

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
                _common.showErrorMessage(new_view, null);
            }
            load_callback = true;
            mTask = null;
            showLoadingProgress();
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            showLoadingProgress();
        }
    }

    private class ListAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private Common common;
        private JSONArray jsonArray;

        // public ListAdapter2(Context context) {
        public ListAdapter2(Context context, JSONArray jsonArray) {
            this.context = context;
            this.jsonArray = jsonArray;
            this.common = new Common(context);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListAdapter2.ListViewHolder(getLayoutInflater().from(context).inflate(R.layout.list_teacher_tab2, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject teacher = (JSONObject) jsonArray.getJSONObject(position);
                ((ListAdapter2.ListViewHolder) holder).bindData(teacher);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (jsonArray == null) {
                return 0;
            } else {
                return jsonArray.length();
            }
        }

        public class ListViewHolder extends RecyclerView.ViewHolder {
            private TextView txtTeacherName;
            private LinearLayout list;
            private String teacherId;

            public ListViewHolder(View itemView) {
                super(itemView);
                list = (LinearLayout) itemView;
                txtTeacherName = (TextView) itemView.findViewById(R.id.txtTeacherName);
            }

            public void bindData(final JSONObject _teacher) {
                try {
                    //JSONObject _teacher = _classSchedule.getJSONObject(getString(R.string.jsonUser));

                    if (_teacher.has(getString(R.string.jsonID))) {
                        teacherId = _teacher.getString(getString(R.string.jsonID));
                    }

                    if (_teacher.has(getString(R.string.jsonFirstName)) && _teacher.has(getString(R.string.jsonLastName))) {
                        String firstName = _teacher.getString(getString(R.string.jsonFirstName));
                        String lastName = _teacher.getString(getString(R.string.jsonLastName));
                        txtTeacherName.setText(lastName + " " + firstName);
                    }

                    list.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for (int i =0;i<_teachers.length(); i++){
                                            JSONObject temp_teacher = _teachers.getJSONObject(i);
                                            String _id = temp_teacher.getString(getString(R.string.jsonID));
                                            if (_id.equals(_teacher.getString(getString(R.string.jsonID)))){
                                                return;
                                            }

                                        }
                                        _teachers.put(_teacher);
                                        _list_adapter.notifyDataSetChanged();

                                        alertDialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }


    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private Common common;

        public ListAdapter(Context context) {
            //public ListAdapter(Context context, JSONArray jsonArray) {
            this.context = context;
            //this.jsonArray = jsonArray;
            this.common = new Common(context);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListAdapter.ListViewHolder(getLayoutInflater().from(context).inflate(R.layout.list_teacher_tab, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject teacher = (JSONObject) _teachers.getJSONObject(position);
                ((ListAdapter.ListViewHolder) holder).bindData(teacher);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (_teachers == null) {
                return 0;
            } else {
                return _teachers.length();
            }
        }

        public class ListViewHolder extends RecyclerView.ViewHolder {
            private TextView txtTeacherName;
            private CardView list;
            private String teacherId;
            private ImageView imgDelete;

            public ListViewHolder(View itemView) {
                super(itemView);
                txtTeacherName = (TextView) itemView.findViewById(R.id.txtTeacherName);
                imgDelete = (ImageView) itemView.findViewById(R.id.imgDelete);
            }

            public void bindData(final JSONObject _teacher) {
                try {
                    //JSONObject _teacher = _classSchedule.getJSONObject(getString(R.string.jsonUser));

                    if (_teacher.has(getString(R.string.jsonID))) {
                        teacherId = _teacher.getString(getString(R.string.jsonID));
                    }

                    if (_teacher.has(getString(R.string.jsonFirstName)) && _teacher.has(getString(R.string.jsonLastName))) {
                        String firstName = _teacher.getString(getString(R.string.jsonFirstName));
                        String lastName = _teacher.getString(getString(R.string.jsonLastName));
                        txtTeacherName.setText(lastName + " " + firstName);
                    }

                    imgDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog alertDialog = new AlertDialog.Builder(AddClassActivity.this)
                                    .setTitle(getString(R.string.alert_title))
                                    .setMessage(getString(R.string.msg_confirm_delete))
                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            for (int i =0; i < _teachers.length();i++){
                                                try {
                                                    JSONObject teacher = _teachers.getJSONObject(i);
                                                    String _id = teacher.getString(getString(R.string.jsonID));
                                                    if (teacherId.equals(_id)){
                                                        _teachers.remove(i);
                                                        _list_adapter.notifyDataSetChanged();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    })
                                    .show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }
}
