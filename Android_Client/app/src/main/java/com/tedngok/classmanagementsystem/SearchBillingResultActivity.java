package com.tedngok.classmanagementsystem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class SearchBillingResultActivity extends AppCompatActivity {

    private Context _context;
    private Common _common;
    private WebSocketIO _webSocketIO;
    private String _class_id, _student_id;
    private Toolbar toolbar;
    private AppBarLayout app_bar;
    private ProgressBar progressBar;
    private AsyncTask<?, ?, ?> mTask;
    private AlertDialog editBillingDialog;
    private FloatingActionButton refreseh;
    private RecyclerView list;
    private RecyclerView.Adapter list_adapter;
    private RecyclerView.LayoutManager list_layout_manger;
    private Bitmap _billingImage;
    private boolean load_callback = false;
    private JSONArray _billings;
    private JSONArray _smsList;
    private Button btn, btn2, btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        _context = this;
        _common = new Common(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        refreseh = (FloatingActionButton) findViewById(R.id.refresh);
        refreseh.setVisibility(View.VISIBLE);
        app_bar = (AppBarLayout) findViewById(R.id.app_bar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        list = (RecyclerView) findViewById(R.id.list);
        list_layout_manger = new LinearLayoutManager(this);
        list.setLayoutManager(list_layout_manger);

        btn = (Button) findViewById(R.id.btn);
        btn.setText(getString(R.string.select_all));
        btn.setVisibility(View.VISIBLE);
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setText(getString(R.string.cancel_all));
        btn2.setVisibility(View.VISIBLE);
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setText(getString(R.string.sms));
        btn3.setVisibility(View.VISIBLE);

        setAction();
        setRepsonse();
    }

    private void setAction() {
        verifySelection();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0; i<list.getChildCount(); i++){
                    CardView list_billing = (CardView) list.getChildAt(i).findViewById(R.id.list_billing);
                    CheckBox chkSelect = (CheckBox) list_billing.findViewById(R.id.chkSelect);
                    chkSelect.setChecked(true);
                }
                verifySelection();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0; i<list.getChildCount(); i++){
                    CardView list_billing = (CardView) list.getChildAt(i).findViewById(R.id.list_billing);
                    CheckBox chkSelect = (CheckBox) list_billing.findViewById(R.id.chkSelect);
                    chkSelect.setChecked(false);
                }
                verifySelection();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _smsList = new JSONArray();

                for(int i=0; i<list.getChildCount(); i++){
                    CardView list_billing = (CardView) list.getChildAt(i).findViewById(R.id.list_billing);
                    CheckBox chkSelect = (CheckBox) list_billing.findViewById(R.id.chkSelect);

                    if (chkSelect.isChecked()) {
                        try {
                            _smsList.put(_billings.getJSONObject(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Intent i = new Intent(SearchBillingResultActivity.this, SMSActivity.class);
                i.putExtra("sms", _smsList.toString());
                startActivityForResult(i, _common.reqPageWithoutEmpty);
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(getString(R.string.jsonClassID))) {
            _class_id = bundle.getString(getString(R.string.jsonClassID));
        }

        if (bundle.containsKey(getString(R.string.jsonStudentID))) {
            _student_id = bundle.getString(getString(R.string.jsonStudentID));
        }

        new GetBillings(_class_id, _student_id).execute();

        refreseh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetBillings(_class_id, _student_id).execute();
            }
        });
    }


    private void verifySelection(){

        btn3.setVisibility(View.GONE);
        for(int i=0; i<list.getChildCount(); i++){
            CardView list_billing = (CardView) list.getChildAt(i).findViewById(R.id.list_billing);
            CheckBox chkSelect = (CheckBox) list_billing.findViewById(R.id.chkSelect);

            if (chkSelect.isChecked()) {
                btn3.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setRepsonse() {
        _webSocketIO._socket.off("sendPendingEmail");
        _webSocketIO._socket.on("sendPendingEmail", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject json = (JSONObject) args[0];
                if (json.has(getString(R.string.jsonStudent)) && json.has(getString(R.string.jsonClass))) {
                    try {
                        JSONObject jsonStudent = json.getJSONObject(getString(R.string.jsonStudent));
                        JSONObject jsonClass = json.getJSONObject(getString(R.string.jsonClass));
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setData(Uri.parse("mailto:"));
                        i.setType("text/plain");

                        String email = jsonStudent.getString(getString(R.string.jsonEmail));
                        String className = jsonClass.getString(getString(R.string.jsonClassName));
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        i.putExtra(Intent.EXTRA_SUBJECT, "You have been set pending for Class: " + className);
                        i.putExtra(Intent.EXTRA_TEXT, "Hello, You have been set pending for Class: " + className + ", Thanks!");
                        startActivityForResult(Intent.createChooser(i,"Send Email To"), 123);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        _webSocketIO._socket.off("sendConfirmedEmail");
        _webSocketIO._socket.on("sendConfirmedEmail", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject json = (JSONObject) args[0];
                if (json.has(getString(R.string.jsonStudent)) && json.has(getString(R.string.jsonClass))) {
                    try {
                        JSONObject jsonStudent = json.getJSONObject(getString(R.string.jsonStudent));
                        JSONObject jsonClass = json.getJSONObject(getString(R.string.jsonClass));
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setData(Uri.parse("mailto:"));
                        i.setType("text/plain");

                        String email = jsonStudent.getString(getString(R.string.jsonEmail));
                        String className = jsonClass.getString(getString(R.string.jsonClassName));
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        i.putExtra(Intent.EXTRA_SUBJECT, "You have been confirmed to Class: " + className);
                        i.putExtra(Intent.EXTRA_TEXT, "Hello, You have confirmed to Class: " + className + ", Thanks!");
                        startActivityForResult(Intent.createChooser(i,"Send Email To"), 123);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        _webSocketIO._socket.off("sendCancelEmail");
        _webSocketIO._socket.on("sendCancelEmail", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject json = (JSONObject) args[0];
                if (json.has(getString(R.string.jsonStudent)) && json.has(getString(R.string.jsonClass))) {
                    try {
                        JSONObject jsonStudent = json.getJSONObject(getString(R.string.jsonStudent));
                        JSONObject jsonClass = json.getJSONObject(getString(R.string.jsonClass));
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setData(Uri.parse("mailto:"));
                        i.setType("text/plain");

                        String email = jsonStudent.getString(getString(R.string.jsonEmail));
                        String className = jsonClass.getString(getString(R.string.jsonClassName));
                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        i.putExtra(Intent.EXTRA_SUBJECT, "You have been cancelled to Class: " + className);
                        i.putExtra(Intent.EXTRA_TEXT, "Hello, You have cancelled to Class: " + className + ", Thanks!");
                        startActivityForResult(Intent.createChooser(i,"Send Email To"), 123);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        _webSocketIO._socket.off("reloadBillings");
        _webSocketIO._socket.on("reloadBillings", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreseh.performClick();
                    }
                });

            }
        });

        _webSocketIO._socket.off("updateBilling");
        _webSocketIO._socket.on("updateBilling", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject json = (JSONObject) args[0];
                    if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                        _common.showMessage(list, getString(R.string.msg_update_image_success));
                    } else {
                        _common.showErrorMessage(list, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    _common.showErrorMessage(list, null);
                }

                load_callback = true;
                _common.showLoadingProgress(list, progressBar, load_callback);
            }
        });

        _webSocketIO._socket.off("getBilling");
        _webSocketIO._socket.on("getBilling", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {

                                ConstraintLayout dialog_view = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_edit_billing, null);

                                if (json.has(getString(R.string.jsonBilling))) {
                                    final JSONObject billing = json.getJSONObject(getString(R.string.jsonBilling));
                                    ImageView imgBilling = (ImageView) dialog_view.findViewById(R.id.imgBilling);
                                    final Spinner spinnerStatus = (Spinner) dialog_view.findViewById(R.id.spinnerStatus);
                                    spinnerStatus.setAdapter(_common.getBillingStatusAdapter());
                                    spinnerStatus.setSelection(_common.getBillingStatus(billing.getString(getString(R.string.jsonStatus))));

                                    if (billing.has(getString(R.string.jsonImage))) {
                                        String base64Str = billing.getString(getString(R.string.jsonImage));
                                        String billingStatus = billing.getString(getString(R.string.jsonStatus));
                                        imgBilling.setImageBitmap(_common.base64ToBitmap(base64Str));
                                        spinnerStatus.setSelection(_common.getBillingStatus(billingStatus));
                                    }

                                    imgBilling.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            if (i.resolveActivity(getPackageManager()) != null) {
                                                startActivityForResult(i, _common.reqTakePhotoCapture);
                                            }
                                        }
                                    });

                                    editBillingDialog = new AlertDialog.Builder(_context)
                                            .setTitle(R.string.title_edit_billing)
                                            .setView(dialog_view)
                                            .setPositiveButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        JSONObject editObject = new JSONObject();

                                                        if (_billingImage != null) {
                                                            editObject.put(getString(R.string.jsonImage), _common.bitmapToBase64(_billingImage));
                                                        }
                                                        editObject.put(getString(R.string.jsonID), billing.getString(getString(R.string.jsonID)));
                                                        editObject.put(getString(R.string.jsonStatus), _common.billingStatusToValue(spinnerStatus.getSelectedItem().toString()));
                                                        new GetBillings(editObject).execute();

                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            })
                                            .show();
                                } else {
                                    _common.showErrorMessage(list, null);
                                }

                            } else {
                                _common.showErrorMessage(list, json);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(list, null);
                        }
                        load_callback = true;
                        _common.showLoadingProgress(list, progressBar, load_callback);
                    }
                });
            }
        });

        _webSocketIO._socket.off("getBillings");
        _webSocketIO._socket.on("getBillings", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                _billings = json.getJSONArray(getString(R.string.jsonBillings));
                                list.setAdapter(new ListAdapter(_context, json.getJSONArray(getString(R.string.jsonBillings))));
                            } else {
                                if (json.has(getString(R.string.jsonErrorCode))) {
                                    String error_code = json.getString(getString(R.string.jsonErrorCode));
                                    if (error_code.equals(getString(R.string.error_code_level_not_enough))) {
                                        _common.showErrorMessage(list, json);
                                    }
                                } else {
                                    _common.showErrorMessage(list, json);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(list, null);
                        }
                        load_callback = true;
                        _common.showLoadingProgress(list, progressBar, load_callback);
                    }
                });
            }
        });
    }

    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private JSONArray jsonArray;
        private Context context;
        private Common common;

        public ListAdapter(Context context, JSONArray jsonArray) {
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
            return new ListViewHolder(getLayoutInflater().from(context).inflate(R.layout.list_billing, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject billing = (JSONObject) jsonArray.getJSONObject(position);
                ((ListAdapter.ListViewHolder) holder).bindData(billing);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class ListViewHolder extends RecyclerView.ViewHolder {
            private TextView txtClassID, txtStudentID, txtCreatedAtTime, txtCreatedAtDate, txtClassName, txtStudentName, txtPrice, txtStatus;
            private ImageView imgBilling;
            private LinearLayout classNameView, studentNameView;
            private CardView list_billing;
            private CheckBox chkSelect;

            public ListViewHolder(View itemView) {
                super(itemView);
                imgBilling = (ImageView) itemView.findViewById(R.id.imgBilling);

                txtClassID = (TextView) itemView.findViewById(R.id.txtClassID);
                txtStudentID = (TextView) itemView.findViewById(R.id.txtStudentID);
                txtCreatedAtTime = (TextView) itemView.findViewById(R.id.txtCreatedAtTime);
                txtCreatedAtDate = (TextView) itemView.findViewById(R.id.txtCreatedAtDate);
                txtClassName = (TextView) itemView.findViewById(R.id.txtClassName);
                txtStudentName = (TextView) itemView.findViewById(R.id.txtStudentName);
                txtPrice = (TextView) itemView.findViewById(R.id.txtPrice);
                txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);

                list_billing = (CardView) itemView.findViewById(R.id.list_billing);

                classNameView = (LinearLayout) itemView.findViewById(R.id.classNameView);
                studentNameView = (LinearLayout) itemView.findViewById(R.id.studentNameView);

                chkSelect = (CheckBox) itemView.findViewById(R.id.chkSelect);
            }

            public void bindData(final JSONObject billing) {
                try {

                    JSONObject classObj = billing.getJSONObject(getString(R.string.jsonClass));
                    JSONObject studentObj = billing.getJSONObject(getString(R.string.jsonStudent));

                    if (billing.has(getString(R.string.jsonClass))) {
                        String class_id = classObj.getString(getString(R.string.jsonClassID));
                        txtClassID.setText(getString(R.string.class_id) + ":" + class_id);
                    } else {
                        txtClassID.setVisibility(View.GONE);
                    }

                    if (billing.has(getString(R.string.jsonStudent))) {
                        String student_id = studentObj.getString(getString(R.string.jsonStudentID));
                        txtStudentID.setText(getString(R.string.student_id) + ": " + student_id);
                    } else {
                        txtStudentID.setVisibility(View.GONE);
                    }

                    if (billing.has(getString(R.string.jsonCreatedAt))) {
                        String createdAt = billing.getString(getString(R.string.jsonCreatedAt));
                        txtCreatedAtDate.setText(_common.dbDateTimeToDateTimeString(createdAt));
                        txtCreatedAtTime.setText(_common.dbDateTimeToTimeString(createdAt));
                    }

                    if (classObj.has(getString(R.string.jsonClassName))) {
                        String className = classObj.getString(getString(R.string.jsonClassName));
                        txtClassName.setText(className);
                    } else {
                        classNameView.setVisibility(View.GONE);
                    }

                    if (studentObj.has(getString(R.string.jsonFirstName)) && studentObj.has(getString(R.string.jsonLastName))) {
                        String student_name = studentObj.getString(getString(R.string.jsonLastName)) + " " + studentObj.getString(getString(R.string.jsonFirstName));
                        txtStudentName.setText(student_name);
                    } else {
                        studentNameView.setVisibility(View.GONE);
                    }

                    if (classObj.has(getString(R.string.jsonPrice))) {
                        String price = "$" + String.valueOf(classObj.getDouble(getString(R.string.jsonPrice)));
                        txtPrice.setText(price);
                    } else {
                        txtPrice.setVisibility(View.GONE);
                    }

                    if (billing.has(getString(R.string.jsonImage))) {
                        String base64Str = billing.getString(getString(R.string.jsonImage));
                        imgBilling.setImageBitmap(_common.base64ToBitmap(base64Str));
                        imgBilling.setBackgroundColor(Color.TRANSPARENT);
                    }

                    if (billing.has(getString(R.string.jsonStatus))) {
                        String status = (String) billing.get(getString(R.string.jsonStatus));
                        txtStatus.setText(_common.billingValueToStatus(status));
                    } else {
                        txtStatus.setVisibility(View.GONE);
                    }

                    list_billing.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                new GetBillings(billing.getString(getString(R.string.jsonID))).execute();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    chkSelect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            verifySelection();
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class GetBillings extends AsyncTask<JSONObject, Void, Boolean> {

        private String class_id;
        private String student_id;
        private String billing_real_id;
        private JSONObject update_billing_json;

        public GetBillings(JSONObject update_billing_json) {
            this.update_billing_json = update_billing_json;
        }

        public GetBillings(String billing_real_id) {
            this.billing_real_id = billing_real_id;
        }

        public GetBillings(String class_id, String student_id) {
            this.class_id = class_id;
            this.student_id = student_id;
        }


        @Override
        protected Boolean doInBackground(JSONObject... params) {
            load_callback = false;
            _common.showLoadingProgress(list, progressBar, load_callback);

            try {
                JSONObject jsonObject = new JSONObject();
                if (update_billing_json != null) {
                    update_billing_json.put(getString(R.string.jsonToken), _common.getToken());
                    _webSocketIO._socket.emit("updateBilling", update_billing_json);
                } else if (billing_real_id != null) {
                    jsonObject = new JSONObject();
                    jsonObject.put(getString(R.string.jsonID), billing_real_id);
                    jsonObject.put(getString(R.string.jsonToken), _common.getToken());
                    _webSocketIO._socket.emit("getBilling", jsonObject);
                } else {
                    jsonObject.put(getString(R.string.jsonStudentID), student_id);
                    jsonObject.put(getString(R.string.jsonClassID), class_id);
                    jsonObject.put(getString(R.string.jsonToken), _common.getToken());
                    _webSocketIO._socket.emit("getBillings", jsonObject);
                }

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
                _common.showErrorMessage(list, null);
            }
            load_callback = true;
            mTask = null;
            _common.showLoadingProgress(list, progressBar, load_callback);
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            _common.showLoadingProgress(list, progressBar, load_callback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == _common.reqTakePhotoCapture) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                ImageView imgBilling = (ImageView) editBillingDialog.findViewById(R.id.imgBilling);
                imgBilling.setImageBitmap(bitmap);
                imgBilling.getLayoutParams().height = 600;
                _billingImage = bitmap;
            }
        } else if (requestCode == _common.reqPageWithoutEmpty){
            if (resultCode == Activity.RESULT_CANCELED){
                if (data != null && data.hasExtra(getString(R.string.jsonErrorMessage))){
                    _common.showMessage(list,data.getStringExtra(getString(R.string.jsonErrorMessage)));
                }
            } else if (resultCode == Activity.RESULT_OK){
                _common.showMessage(list,R.string.msg_send_sms_succcess);
            }
        } else if (requestCode == 123){
            //for after send email
            //refresh
            refreseh.performClick();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return _common.onSupportNavigateUp();
    }
}
