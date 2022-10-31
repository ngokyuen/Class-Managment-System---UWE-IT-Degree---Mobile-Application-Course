package com.tedngok.classmanagementsystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

import static com.tedngok.classmanagementsystem.R.id.editQuery;
import static com.tedngok.classmanagementsystem.R.id.new_view;

public class SearchClassFragment extends Fragment {

    public static int reqSearchClassesResult = 1;
    public static int resSearchClasses = 1;

    private static final String ARG_PARAM1 = "param1", ARG_PARAM2 = "param2";
    private WebSocketIO webSocketIO;
    private String mParam1, mParam2;
    private FloatingActionButton addClassButton;
    private View view;
    private ProgressBar progressBar;
    private boolean load_callback = false;
    private AsyncTask<JSONObject, Void, Boolean> mTask = null;
    private Common _common;
    private LinearLayout new_view;
    private Button btnSubmit,btnReset;
    private CheckBox chkStatusAll, chkStatusOpen, chkStatusCancel, chkStatusBegin, chkStatusCompleted,
            chkTypeAll, chkTypeIndividual, chkTypeSmall;
    private TextView txtStartDateFrom, txtStartDateTo, txtStartTimeFrom, txtStartTimeTo;
    private TableRow conditionView2, conditionView3;
    private Spinner spinner_min_level_type, spinner_min_level_type2, spinner_min_level_type3;
    private EditText editQuery, editPriceFrom, editPriceTo, editMinLevelFrom, editMinLevelTo, editMinLevel2From, editMinLevel2To, editMinLevel3From, editMinLevel3To;

    private OnFragmentInteractionListener mListener;

    public SearchClassFragment() {
    }

    public static SearchClassFragment newInstance(String param1, String param2) {
        SearchClassFragment fragment = new SearchClassFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_search_class, container, false);
        _common = new Common(getContext());

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        addClassButton = (FloatingActionButton) view.findViewById(R.id.addClassActionButton);
        btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
        btnReset = (Button) view.findViewById(R.id.btnReset);
        new_view = (LinearLayout) view.findViewById(R.id.new_view);
        chkStatusAll = (CheckBox) view.findViewById(R.id.chkStatusAll);
        chkStatusBegin = (CheckBox) view.findViewById(R.id.chkStatusBegin);
        chkStatusCancel = (CheckBox) view.findViewById(R.id.chkStatusCancel);
        chkStatusCompleted = (CheckBox) view.findViewById(R.id.chkStatusCompleted);
        chkStatusOpen = (CheckBox) view.findViewById(R.id.chkStatusOpen);
        chkTypeAll = (CheckBox) view.findViewById(R.id.chkTypeAll);
        chkTypeIndividual = (CheckBox) view.findViewById(R.id.chkTypeIndividual);
        chkTypeSmall = (CheckBox) view.findViewById(R.id.chkTypeSmall);
        txtStartDateFrom = (TextView) view.findViewById(R.id.txtStartDateFrom);
        txtStartDateTo = (TextView) view.findViewById(R.id.txtStartDateTo);
        txtStartTimeFrom = (TextView) view.findViewById(R.id.txtStartTimeFrom);
        txtStartTimeTo = (TextView) view.findViewById(R.id.txtStartTimeTo);
        spinner_min_level_type = (Spinner) view.findViewById(R.id.spinner_min_level_type);
        spinner_min_level_type2 = (Spinner) view.findViewById(R.id.spinner_min_level_type2);
        spinner_min_level_type3 = (Spinner) view.findViewById(R.id.spinner_min_level_type3);
        conditionView2 = (TableRow) view.findViewById(R.id.conditionView2);
        conditionView3 = (TableRow) view.findViewById(R.id.conditionView3);
        editQuery = (EditText) view.findViewById(R.id.editQuery);
        editPriceFrom = (EditText) view.findViewById(R.id.editPriceFrom);
        editPriceTo = (EditText) view.findViewById(R.id.editPriceTo);
        editMinLevelFrom = (EditText) view.findViewById(R.id.editMinLevelFrom);
        editMinLevel2From = (EditText) view.findViewById(R.id.editMinLevel2From);
        editMinLevel3From = (EditText) view.findViewById(R.id.editMinLevel3From);
        editMinLevelTo = (EditText) view.findViewById(R.id.editMinLevelTo);
        editMinLevel2To = (EditText) view.findViewById(R.id.editMinLevel2To);
        editMinLevel3To = (EditText) view.findViewById(R.id.editMinLevel3To);

        setAction();
        setResponse();
        clickCheckStatusAll();
        clickCheckTypeAll();

        setEnable();

        return view;
    }

    private void setEnable() {


        spinner_min_level_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevelFrom.setText("");
                    editMinLevelFrom.setEnabled(false);
                    editMinLevelFrom.setError(null);
                    editMinLevelTo.setText("");
                    editMinLevelTo.setEnabled(false);
                    editMinLevelTo.setError(null);
                } else {

                    if (spinner_min_level_type.getSelectedItemPosition() == spinner_min_level_type2.getSelectedItemPosition() || spinner_min_level_type.getSelectedItemPosition() == spinner_min_level_type3.getSelectedItemPosition()){
                        spinner_min_level_type.setSelection(0);
                        _common.showMessage(new_view,getString(R.string.msg_selection_duplicate));
                    } else {
                        conditionView2.setVisibility(View.VISIBLE);
                        editMinLevelFrom.setEnabled(true);
                        editMinLevelTo.setEnabled(true);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_min_level_type2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevel2From.setText("");
                    editMinLevel2From.setEnabled(false);
                    editMinLevel2From.setError(null);
                    editMinLevel2To.setText("");
                    editMinLevel2To.setEnabled(false);
                    editMinLevel2To.setError(null);
                } else {

                    if (spinner_min_level_type2.getSelectedItemPosition() == spinner_min_level_type.getSelectedItemPosition() || spinner_min_level_type2.getSelectedItemPosition() == spinner_min_level_type3.getSelectedItemPosition()){
                        spinner_min_level_type2.setSelection(0);
                        _common.showMessage(new_view,getString(R.string.msg_selection_duplicate));
                    } else {
                        conditionView3.setVisibility(View.VISIBLE);
                        editMinLevel2From.setEnabled(true);
                        editMinLevel2To.setEnabled(true);
                    }

//                    editMinLevel2From.setEnabled(true);
//                    editMinLevel2To.setEnabled(true);
//                    conditionView3.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner_min_level_type3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    editMinLevel3From.setText("");
                    editMinLevel3From.setEnabled(false);
                    editMinLevel3From.setError(null);
                    editMinLevel3To.setText("");
                    editMinLevel3To.setEnabled(false);
                    editMinLevel3To.setError(null);
                } else {

                    if (spinner_min_level_type3.getSelectedItemPosition() == spinner_min_level_type.getSelectedItemPosition() || spinner_min_level_type3.getSelectedItemPosition() == spinner_min_level_type2.getSelectedItemPosition()){
                        spinner_min_level_type3.setSelection(0);
                        _common.showMessage(new_view,getString(R.string.msg_selection_duplicate));
                    } else {
                        editMinLevel3From.setEnabled(true);
                        editMinLevel3To.setEnabled(true);
                    }
//                    editMinLevel3From.setEnabled(true);
//                    editMinLevel3To.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setResponse() {

        webSocketIO._socket.off("searchClasses");
        webSocketIO._socket.on("searchClasses", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {

                                if (json.has(getString(R.string.jsonClasses))) {
                                    JSONArray jsonClasses = json.getJSONArray(getString(R.string.jsonClasses));
                                    if (jsonClasses.length() > 0) {
                                        showClasses(json.getJSONArray(getString(R.string.jsonClasses)));
                                    } else {
                                        showEmptyClassesError();
                                    }
                                } else {
                                    showTryAgainError();
                                }
                            } else {
                                showTryAgainError();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showTryAgainError();
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });
    }

    private void showClasses(JSONArray jsonArray) {
        SearchClassesResultActivity.data = jsonArray;
        Intent i = new Intent(getActivity(), SearchClassesResultActivity.class);
        //i.putExtra(getString(R.string.jsonClasses), jsonArray.toString());
        i.putExtra(getString(R.string.jsonQuery), editQuery.getText().toString());

//        startActivity(i);
        startActivityForResult(i, reqSearchClassesResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == reqSearchClassesResult){
            if (resultCode == resSearchClasses){
                String query = data.getExtras().getString(getString(R.string.jsonQuery));
                editQuery.setText(query);
                btnSubmit.performClick();
            }
        }
    }

    private void setAction() {

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editQuery.setText("");
                editPriceTo.setText("");
                editPriceFrom.setText("");
                txtStartDateFrom.setText("");
                txtStartDateTo.setText("");
                txtStartTimeFrom.setText("");
                txtStartTimeTo.setText("");
                spinner_min_level_type.setSelection(0);
                spinner_min_level_type2.setSelection(0);
                spinner_min_level_type3.setSelection(0);

            }
        });

        conditionView2.setVisibility(View.GONE);
        conditionView3.setVisibility(View.GONE);

        spinner_min_level_type.setAdapter(_common.getMinLevelTypeAdpater());
        spinner_min_level_type2.setAdapter(_common.getMinLevelTypeAdpater());
        spinner_min_level_type3.setAdapter(_common.getMinLevelTypeAdpater());
        setEnable();

        txtStartTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                final int mHour = c.get(Calendar.HOUR_OF_DAY);
                final int mMin = c.get(Calendar.MINUTE);

                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        me.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                    }
                }, mHour, mMin, true).show();
            }
        });

        txtStartTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                final int mHour = c.get(Calendar.HOUR_OF_DAY);
                final int mMin = c.get(Calendar.MINUTE);
//                final int mDay = c.get(Calendar.DAY_OF_MONTH);

                new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        me.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                    }
                }, mHour, mMin, true).show();
            }
        });

        txtStartDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                final int mYear = c.get(Calendar.YEAR);
                final int mMonth = c.get(Calendar.MONTH);
                final int mDay = c.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        me.setText(String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(dayOfMonth));
                    }
                }, mYear, mMonth, mDay).show();
            }
        });

        txtStartDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView me = (TextView) v;
                final Calendar c = Calendar.getInstance();
                final int mYear = c.get(Calendar.YEAR);
                final int mMonth = c.get(Calendar.MONTH);
                final int mDay = c.get(Calendar.DAY_OF_MONTH);

                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        me.setText(String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(dayOfMonth));
                    }
                }, mYear, mMonth, mDay).show();
            }
        });

        chkStatusCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
            }
        });

        chkStatusOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
            }
        });

        chkStatusCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
            }
        });

        chkStatusBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
            }
        });

        chkTypeIndividual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkType();
            }
        });

        chkTypeSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkType();
            }
        });

        chkStatusAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickStatusAll();
            }
        });
        chkTypeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickTypeAll();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editQuery = (EditText) view.findViewById(R.id.editQuery);
                editPriceFrom = (EditText) view.findViewById(R.id.editPriceFrom);
                editPriceTo = (EditText) view.findViewById(R.id.editPriceTo);
                editMinLevelFrom = (EditText) view.findViewById(R.id.editMinLevelFrom);
                editMinLevel2From = (EditText) view.findViewById(R.id.editMinLevel2From);
                editMinLevel3From = (EditText) view.findViewById(R.id.editMinLevel3From);
                editMinLevelTo = (EditText) view.findViewById(R.id.editMinLevelTo);
                editMinLevel2To = (EditText) view.findViewById(R.id.editMinLevel2To);
                editMinLevel3To = (EditText) view.findViewById(R.id.editMinLevel3To);

                boolean verification = true;

                if (!_common.isPriceFromTo(editPriceFrom.getText().toString(),editPriceTo.getText().toString())){
                    verification = false;
                    editPriceTo.setError(getString(R.string.msg_error_price_from_to));
                } else {
                    editPriceTo.setError(null);
                }

                if (!_common.isTimeFromTo(txtStartTimeFrom.getText().toString(),txtStartTimeTo.getText().toString())){
                    verification = false;
                    txtStartTimeTo.setError(getString(R.string.msg_error_time_from_to));
                } else {
                    txtStartTimeTo.setError(null);
                }

                if (!_common.isDateTimeFromTo(txtStartDateFrom.getText().toString(),txtStartDateTo.getText().toString())){
                    verification = false;
                    txtStartDateTo.setError(getString(R.string.msg_error_time_from_to));
                } else {
                    txtStartDateTo.setError(null);
                }

                if (spinner_min_level_type.getSelectedItemPosition() > 0 && editMinLevelFrom.getText().toString().equals("") && editMinLevelTo.getText().toString().equals("")) {
                    verification = false;
                    editMinLevelTo.setError(getString(R.string.error_empty));
                } else if (!_common.isQualificationValueFromTo(editMinLevelFrom.getText().toString(),  editMinLevelTo.getText().toString())) {
                    editMinLevelTo.setError(getString(R.string.msg_error_wrong_range));
                } else {
                    editMinLevelTo.setError(null);
                }

                if (spinner_min_level_type2.getSelectedItemPosition() > 0 && editMinLevel2From.getText().toString().equals("") && editMinLevel2To.getText().toString().equals("")) {
                    verification = false;
                    editMinLevel2To.setError(getString(R.string.error_empty));
                } else if (!_common.isQualificationValueFromTo(editMinLevel2From.getText().toString(),  editMinLevel2To.getText().toString())) {
                    editMinLevel2To.setError(getString(R.string.msg_error_wrong_range));
                } else {
                    editMinLevel2To.setError(null);
                }

                if (spinner_min_level_type3.getSelectedItemPosition() > 0 && editMinLevel3From.getText().toString().equals("") && editMinLevel3To.getText().toString().equals("")) {
                    verification = false;
                    editMinLevel3To.setError(getString(R.string.error_empty));
                } else if (!_common.isQualificationValueFromTo(editMinLevel3From.getText().toString(),  editMinLevel3To.getText().toString())) {
                    editMinLevel3To.setError(getString(R.string.msg_error_wrong_range));
                } else {
                    editMinLevel3To.setError(null);
                }

                if (verification) {
                    try {

                        JSONObject json = new JSONObject();
                        json.put(getString(R.string.jsonToken), _common.getToken().toString());

                        json.put(getString(R.string.jsonQuery), editQuery.getText().toString());

                        json.put(getString(R.string.jsonPriceFrom), editPriceFrom.getText().toString());
                        json.put(getString(R.string.jsonPriceTo), editPriceTo.getText().toString());
                        json.put(getString(R.string.jsonMinLevelFrom), editMinLevelFrom.getText().toString());
                        json.put(getString(R.string.jsonMinLevelTo), editMinLevelTo.getText().toString());
                        json.put(getString(R.string.jsonMinLevelType), spinner_min_level_type.getSelectedItem().toString());
                        json.put(getString(R.string.jsonMinLevel2From), editMinLevel2From.getText().toString());
                        json.put(getString(R.string.jsonMinLevel2To), editMinLevel2To.getText().toString());
                        json.put(getString(R.string.jsonMinLevelType2), spinner_min_level_type2.getSelectedItem().toString());
                        json.put(getString(R.string.jsonMinLevel3From), editMinLevel3From.getText().toString());
                        json.put(getString(R.string.jsonMinLevel3To), editMinLevel3To.getText().toString());
                        json.put(getString(R.string.jsonMinLevelType3), spinner_min_level_type3.getSelectedItem().toString());

                        json.put(getString(R.string.jsonDateFrom), txtStartDateFrom.getText().toString());
                        json.put(getString(R.string.jsonDateTo), txtStartDateTo.getText().toString());

                        if (!txtStartTimeFrom.getText().toString().equals("")){
                            json.put(getString(R.string.jsonTimeFrom), _common.dbTimeStringToSecond(txtStartTimeFrom.getText().toString()));
                        }
                        if (!txtStartTimeTo.getText().toString().equals("")){
                            json.put(getString(R.string.jsonTimeTo), _common.dbTimeStringToSecond(txtStartTimeTo.getText().toString()));
                        }

                        //setting Status
                        String status = "";
                        if (chkStatusBegin.isChecked())
                            status += getString(R.string.jsonStatusBeginning) + " ";
                        if (chkStatusCancel.isChecked())
                            status += getString(R.string.jsonStatusCancel) + " ";
                        if (chkStatusCompleted.isChecked())
                            status += getString(R.string.jsonStatusCompleted) + " ";
                        if (chkStatusOpen.isChecked())
                            status += getString(R.string.jsonStatusOpen) + " ";

                        if (!status.equals(""))
                            json.put(getString(R.string.jsonStatus), status);

                        //Setting Type
                        String type = "";
                        if (chkTypeIndividual.isChecked())
                            type += getString(R.string.jsonTypeIndividual) + " ";
                        if (chkTypeSmall.isChecked())
                            type += getString(R.string.jsonTypeSmall) + " ";
                        if (!type.equals(""))
                            json.put(getString(R.string.jsonType), type);

                        mTask = new SearchClassTask().execute(json);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        addClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddClassActivity.class);
                startActivity(i);
            }
        });
    }

    private void clickStatusAll() {
        if (chkStatusAll.isChecked()) {
            clickCheckStatusAll();
        } else {
            clickUncheckStatusAll();
        }
    }

    private void clickTypeAll() {
        if (chkTypeAll.isChecked()) {
            clickCheckTypeAll();
        } else {
            clickUncheckTypeAll();
        }
    }

    private void checkStatus() {
        boolean verify = true;
        verify &= chkStatusBegin.isChecked();
        verify &= chkStatusCompleted.isChecked();
        verify &= chkStatusOpen.isChecked();
        verify &= chkStatusCancel.isChecked();

        if (verify)
            chkStatusAll.setChecked(true);
        else
            chkStatusAll.setChecked(false);
    }

    private void checkType() {
        boolean verify = true;
        verify &= chkTypeSmall.isChecked();
        verify &= chkTypeIndividual.isChecked();

        if (verify)
            chkTypeAll.setChecked(true);
        else
            chkTypeAll.setChecked(false);
    }

    private void clickCheckStatusAll() {
        chkStatusAll.setChecked(true);
        chkStatusOpen.setChecked(true);
        chkStatusCompleted.setChecked(true);
        chkStatusCancel.setChecked(true);
        chkStatusBegin.setChecked(true);
    }

    private void clickUncheckStatusAll() {
        chkStatusAll.setChecked(false);
        chkStatusOpen.setChecked(false);
        chkStatusCompleted.setChecked(false);
        chkStatusCancel.setChecked(false);
        chkStatusBegin.setChecked(false);
    }

    private void clickCheckTypeAll() {
        chkTypeAll.setChecked(true);
        chkTypeIndividual.setChecked(true);
        chkTypeSmall.setChecked(true);
    }

    private void clickUncheckTypeAll() {
        chkTypeAll.setChecked(false);
        chkTypeIndividual.setChecked(false);
        chkTypeSmall.setChecked(false);
    }

    private void showLoadingProgress() {
        getActivity().runOnUiThread(new Runnable() {
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

    public void showEmptyClassesError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(new_view, R.string.error_empty_result, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void showTryAgainError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(new_view, R.string.error_try_again_later, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public class SearchClassTask extends AsyncTask<JSONObject, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONObject... params) {

            load_callback = false;
            showLoadingProgress();
            webSocketIO._socket.emit("searchClasses", (JSONObject) params[0]);

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
                showTryAgainError();
                //Toast.makeText(getApplicationContext(), getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show();
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

}
