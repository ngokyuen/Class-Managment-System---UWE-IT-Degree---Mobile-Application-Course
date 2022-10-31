package com.tedngok.classmanagementsystem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

import general.Common;
import general.WebSocketIO;

public class SMSActivity extends AppCompatActivity {

    private Context _context;
    private Common _common;
    private WebSocketIO _webSocketIO;
    private boolean load_callback = false;

    private ProgressBar progressBar;

    private Toolbar toolbar;
    private AppBarLayout app_bar;

    private RecyclerView list;
    private RecyclerView.Adapter list_adapter;
    private RecyclerView.LayoutManager list_layout_manger;

    private Button btnSendSMS;

    private JSONArray smsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        _context = this;
        _common = new Common(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnSendSMS = (Button) findViewById(R.id.btn);
        btnSendSMS.setText(getString(R.string.sms));
        btnSendSMS.setVisibility(View.VISIBLE);

        try {
            smsList = new JSONArray(getIntent().getStringExtra("sms"));
            if (smsList.length() == 0 || smsList == null){
                Intent i = new Intent();
                i.putExtra(getString(R.string.jsonErrorMessage), R.string.error_empty_data);
                setResult(Activity.RESULT_CANCELED, i);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        list = (RecyclerView) findViewById(R.id.list);
        list_layout_manger = new LinearLayoutManager(this);
        list_adapter = new ListAdapter(this, smsList);
        list.setLayoutManager(list_layout_manger);
        list.setAdapter(list_adapter);
        setAction();
//        setRepsonse();
    }


    private void setAction() {
        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(_context)
                        .setTitle(R.string.title_sms)
                        .setMessage(R.string.msg_confirm_send_sms)
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SmsManager smsManager = SmsManager.getDefault();

                                JSONArray temp_smsList = smsList;
                                JSONArray upload_contentList = new JSONArray();
                                JSONArray billingsJSON = new JSONArray();
                                int length = temp_smsList.length();
                                for (int i = 0; i < length; i++) {
                                    CardView child = (CardView) list.getChildAt(i);
                                    TextView txtMobile = (TextView) child.findViewById(R.id.txtMobile);
                                    EditText editSMSContent = (EditText) child.findViewById(R.id.editSMSContent);
                                    String smsContent = editSMSContent.getText().toString();
                                    upload_contentList.put(smsContent);
                                    smsManager.sendTextMessage(txtMobile.getText().toString(), null, smsContent, null, null);
                                    try {
                                        String _id = temp_smsList.getJSONObject(0).getString(getString(R.string.jsonID));
                                        billingsJSON.put(_id);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    temp_smsList.remove(0);
                                    list_adapter.notifyItemRemoved(i);
                                }

                                try {
                                    JSONObject uploadSMSLogJSON = new JSONObject();
                                    uploadSMSLogJSON.put(getString(R.string.jsonToken), _common.getToken());
                                    uploadSMSLogJSON.put(getString(R.string.jsonContents),upload_contentList);
                                    uploadSMSLogJSON.put(getString(R.string.jsonBillings),billingsJSON);

                                    _webSocketIO._socket.emit("uploadSMSLog", uploadSMSLogJSON);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Intent i = new Intent();
                                setResult(Activity.RESULT_OK, i);
                                finish();
                            }
                        })
                        .show();
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
            return new ListAdapter.ListViewHolder(getLayoutInflater().from(context).inflate(R.layout.list_sms, parent, false));
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
            private TextView txtMobile, txtClassName, txtStudentName;
            private EditText editSMSContent;
            private CardView list;

            public ListViewHolder(View itemView) {
                super(itemView);

                txtMobile = (TextView) itemView.findViewById(R.id.txtMobile);
                txtClassName = (TextView) itemView.findViewById(R.id.txtClassName);
                txtStudentName = (TextView) itemView.findViewById(R.id.txtStudentName);
                editSMSContent = (EditText) itemView.findViewById(R.id.editSMSContent);

                list = (CardView) itemView.findViewById(R.id.list);

            }

            public void bindData(final JSONObject billing) {
                try {

                    JSONObject classObj = billing.getJSONObject(getString(R.string.jsonClass));
                    JSONObject studentObj = billing.getJSONObject(getString(R.string.jsonStudent));

                    String mobileNo = studentObj.getString(getString(R.string.jsonMobileNo));
                    txtMobile.setText(mobileNo);

                    String className = classObj.getString(getString(R.string.jsonClassName));
                    txtClassName.setText(className);

                    String student_name = studentObj.getString(getString(R.string.jsonLastName)) + " " + studentObj.getString(getString(R.string.jsonFirstName));
                    txtStudentName.setText(student_name);

                    Double price = classObj.getDouble(getString(R.string.jsonPrice));

                    String sms_content = "Hi, (" + student_name + ")! Sorry, we have not received " +
                            "($" + price.toString() + ") payment  for the  (" + className + ") class , Please pay us as soon as possible! Thanks!";
                    editSMSContent.setText(sms_content);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        return _common.onSupportNavigateUp();
    }
}
