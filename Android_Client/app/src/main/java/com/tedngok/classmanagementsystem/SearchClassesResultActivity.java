package com.tedngok.classmanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class SearchClassesResultActivity extends AppCompatActivity {

    private String ACTION;
    private Common _common;
    private AsyncTask<?,?,?> mTask;

    private EditText editQuery;
    private TextView txtReset;
    private ProgressBar progressBar;
    private FloatingActionButton floatSearch;
    private ConstraintLayout list_view;
    private WebSocketIO _webSocketIO;
    private boolean load_callback = false;

    private Toolbar toolbar;
    private AppBarLayout app_bar;
    private String _student_id;

    private RecyclerView list;
    private RecyclerView.Adapter list_adapter;
    private RecyclerView.LayoutManager list_layout_manger;

    public final static String reqMap = "reqMap";
    public final static int reqJoin = 1;
    public static JSONArray data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _common = new Common(SearchClassesResultActivity.this);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(getString(R.string.jsonStudentID))){
            _student_id = bundle.getString(getString(R.string.jsonStudentID));
        }
        ACTION = getIntent().getAction();
        if (ACTION != null && ACTION == SearchStudentFragment.ActionRecommendClassesResult) {

            setContentView(R.layout.activity_list);
            list_view = (ConstraintLayout) findViewById(R.id.list_view);

        } else {
            setContentView(R.layout.activity_search_classes_result);
            editQuery = (EditText) findViewById(R.id.editQuery);
            txtReset = (TextView) findViewById(R.id.txtReset);
            floatSearch = (FloatingActionButton) findViewById(R.id.floatSearch);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app_bar = (AppBarLayout) findViewById(R.id.app_bar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        list = (RecyclerView) findViewById(R.id.list);
        list_layout_manger = new LinearLayoutManager(this);
        list.setLayoutManager(list_layout_manger);

        setAction();
        setRepsonse();
    }

    private void setRepsonse(){
        _webSocketIO._socket.off("reloadClassList");
        _webSocketIO._socket.on("reloadClassList", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (floatSearch != null)
                            floatSearch.performClick();
                    }
                });
            }
        });

        if (ACTION != null & ACTION == SearchStudentFragment.ActionRecommendClassesResult) {
            _webSocketIO._socket.off("joinClass2");
            _webSocketIO._socket.on("joinClass2", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject json = (JSONObject) args[0];
                                if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {

                                    _common.showMessage(list_view, getString(R.string.msg_join_success));

//                                    System.out.print(json.toString());
                                    if (json.has(getString(R.string.jsonStudent)) && json.has(getString(R.string.jsonClass))) {
                                        JSONObject jsonStudent = json.getJSONObject(getString(R.string.jsonStudent));
                                        JSONObject jsonClass = json.getJSONObject(getString(R.string.jsonClass));
                                        Intent i = new Intent(Intent.ACTION_SEND);
                                        i.setData(Uri.parse("mailto:"));
                                        i.setType("text/plain");

                                        String email = jsonStudent.getString(getString(R.string.jsonEmail));
                                        String className = jsonClass.getString(getString(R.string.jsonClassName));
                                        Double price = jsonClass.getDouble(getString(R.string.jsonPrice));
                                        i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                                        i.putExtra(Intent.EXTRA_SUBJECT, "You have assigned to Class: " + className);
                                        i.putExtra(Intent.EXTRA_TEXT, "Hello, You have assigned to Class: " + className + ". Please pay the tuition fee ($" + String.valueOf(price) + ")within 3 days, Thanks!");
                                        startActivity(Intent.createChooser(i,"Send Email To"));
                                   }


                                } else {
                                    if (json.has(getString(R.string.jsonErrorCode))) {
                                        String error_code = json.getString(getString(R.string.jsonErrorCode));
                                        if (error_code.equals(getString(R.string.error_code_level_not_enough))) {

                                            _common.showErrorMessage(list_view, json);
                                        }
                                    } else {
                                        _common.showErrorMessage(list_view, json);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                _common.showErrorMessage(list_view, null);
                            }
                            load_callback = true;
                            _common.showLoadingProgress(list_view, progressBar, load_callback);
                        }
                    });
                }
            });
        }
    }

    private void setAction() {
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                app_bar.setExpanded(true);
            }
        });

        if (ACTION != null & ACTION == SearchStudentFragment.ActionRecommendClassesResult) {
            Bundle bundle = getIntent().getExtras();

            //JSONArray classes = new JSONArray(bundle.getString(getString(R.string.jsonClasses)));
            JSONArray classes = data;

            if (classes.length() == 1) {
                getSupportActionBar().setTitle(getString(R.string.only) + " " + String.valueOf(classes.length()) + " " + getString(R.string.class_recommend));
            } else {
                getSupportActionBar().setTitle(String.valueOf(classes.length()) + " " + getString(R.string.classes_recommend));
            }

            list_adapter = new ListClassesAdapter(this, classes);
            list.setAdapter(list_adapter);

        } else {

            txtReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editQuery.setText("");
                }
            });

            floatSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent();
                    i.putExtra(getString(R.string.jsonQuery), editQuery.getText().toString());
                    SearchClassesResultActivity.this.setResult(SearchClassFragment.resSearchClasses, i);
                    SearchClassesResultActivity.this.finish();
                }
            });

            try {
                Bundle bundle = getIntent().getExtras();
                String query = bundle.getString(getString(R.string.jsonQuery), "");
                editQuery.setText(query);
                editQuery.setSelection(query.length());
//                JSONArray classes = new JSONArray(bundle.getString(getString(R.string.jsonClasses)));
                JSONArray classes = data;
                if (classes.length() == 1) {
                    getSupportActionBar().setTitle(getString(R.string.only) + " " + String.valueOf(classes.length()) + " " + getString(R.string.class_available));
                } else {
                    getSupportActionBar().setTitle(String.valueOf(classes.length()) + " " + getString(R.string.classes_available));
                }

                list_adapter = new ListClassesAdapter(this, classes);
                list.setAdapter(list_adapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private class ListClassesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private JSONArray jsonArray;
        private Context context;
        private Common common;

        public ListClassesAdapter(Context context, JSONArray jsonArray) {
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
            return new ListClassesViewHolder(getLayoutInflater().from(context).inflate(R.layout.list_class, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject _class = (JSONObject) jsonArray.getJSONObject(position);
                ((ListClassesViewHolder) holder).bindData(_class);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class ListClassesViewHolder extends RecyclerView.ViewHolder {
            private TextView txtClassName, txtNoOfLession, txtMaxStudents,
                    txtStartDateTime, txtEndDateTime,
                    txtStatus, txtPrice, txtClassId, txtAddress, txtTeachers;
            private ImageView imgType, imgMap;
            private LinearLayout startDateTimeView, endDateTimeView;
            private CardView list_class_view;
            private Button btnJoin, btnGallery, btnBilling, btnClassSchedule;
            private String classId;
            private final int resJoin = 1;

            public ListClassesViewHolder(View itemView) {
                super(itemView);
                imgType = (ImageView) itemView.findViewById(R.id.imgType);
                imgMap = (ImageView) itemView.findViewById(R.id.imgMap);

                txtClassId = (TextView) itemView.findViewById(R.id.txtClassId);
                txtClassName = (TextView) itemView.findViewById(R.id.txtClassName);
                txtNoOfLession = (TextView) itemView.findViewById(R.id.txtNoOfLession);
                txtMaxStudents = (TextView) itemView.findViewById(R.id.txtMaxStudents);
                txtTeachers = (TextView) itemView.findViewById(R.id.txtTeachers);

                startDateTimeView = (LinearLayout) itemView.findViewById(R.id.startDateTimeView);
                txtStartDateTime = (TextView) itemView.findViewById(R.id.txtStartDateTime);

                endDateTimeView = (LinearLayout) itemView.findViewById(R.id.endDateTimeView);
                txtEndDateTime = (TextView) itemView.findViewById(R.id.txtEndDateTime);

                txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
                txtPrice = (TextView) itemView.findViewById(R.id.txtPrice);
                txtAddress = (TextView) itemView.findViewById(R.id.txtAddress);

                list_class_view = (CardView) itemView.findViewById(R.id.list_class_view);

                btnBilling = (Button) itemView.findViewById(R.id.btnBilling);
                btnGallery = (Button) itemView.findViewById(R.id.btnGallery);
                btnJoin = (Button) itemView.findViewById(R.id.btnJoin);
                btnClassSchedule =(Button) itemView.findViewById(R.id.btnClassSchedule);
            }

            public void bindData(JSONObject _class) {
                try {
                    if (_class.has(getString(R.string.jsonLat)) && _class.has(getString(R.string.jsonLng))) {
                        final Double jsonLat = (Double) _class.get(getString(R.string.jsonLat));
                        final Double jsonLng = (Double) _class.get(getString(R.string.jsonLng));
                        final String jsonAddress = (String) _class.get(getString(R.string.jsonAddress));
                        imgMap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(SearchClassesResultActivity.this, MapsActivity.class);
                                i.putExtra(getString(R.string.jsonLat), jsonLat);
                                i.putExtra(getString(R.string.jsonLng), jsonLng);
                                i.putExtra(getString(R.string.jsonAddress), jsonAddress);
                                i.setAction(reqMap);
                                startActivity(i);
                            }
                        });
                    } else {
                        imgMap.setVisibility(View.GONE);
                    }

                    if (_class.has(getString(R.string.jsonType))) {
                        String jsonType = (String) _class.get(getString(R.string.jsonType));
                        if (jsonType.equals(getString(R.string.jsonTypeIndividual))) {
                            imgType.setImageResource(R.drawable.presentation1);
                            invisibleBtnJoin();
                        } else if (jsonType.equals(getString(R.string.jsonTypeSmall))) {
                            imgType.setImageResource(R.drawable.presentation);
                        }
                    } else {
                        imgType.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonClassID)) && _class.has(getString(R.string.jsonWeekly))) {
                        Boolean weekly = _class.getBoolean(getString(R.string.jsonWeekly));
                        classId = (String) _class.get(getString(R.string.jsonClassID));

                        if (!weekly) {
                            btnClassSchedule.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(SearchClassesResultActivity.this, EditClassScheduleActivity.class);
                                    i.putExtra(getString(R.string.jsonToken), common.getToken());
                                    i.putExtra(getString(R.string.jsonClassID), classId);
                                    startActivity(i);
                                }
                            });
                            btnClassSchedule.setVisibility(View.VISIBLE);
                        } else {
                            btnClassSchedule.setVisibility(View.GONE);
                        }


                    }


                    if (_class.has(getString(R.string.jsonClassID))) {
                        classId = (String) _class.get(getString(R.string.jsonClassID));
                        txtClassId.setText(classId);

                        list_class_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(SearchClassesResultActivity.this, EditClassActivity.class);
                                i.putExtra(getString(R.string.jsonClassID), classId);
                                startActivity(i);
                            }
                        });

                        btnBilling.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(SearchClassesResultActivity.this, SearchBillingResultActivity.class);
                                i.putExtra(getString(R.string.jsonToken), common.getToken());
                                i.putExtra(getString(R.string.jsonClassID), classId);
                                startActivity(i);
                            }
                        });

                    } else {
                        txtClassId.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonClassName))) {
                        String jsonClassName = (String) _class.get(getString(R.string.jsonClassName));
                        txtClassName.setText(jsonClassName);
                    } else {
                        txtClassName.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonStatus))) {
                        String status_str = _common.valueToStatus(_class.getString(getString(R.string.jsonStatus)));
                        txtStatus.setText(status_str);

                    } else {
                        txtStatus.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonMaxStudents)) && _class.has(getString(R.string.jsonStudents))) {
                        int jsonMaxStudents = (int) _class.get(getString(R.string.jsonMaxStudents));
                        JSONArray jsonStudents = (JSONArray) _class.get(getString(R.string.jsonStudents));
                        txtMaxStudents.setText(String.valueOf(jsonStudents.length()) + "/" + String.valueOf(jsonMaxStudents) + " " + getString(R.string.students));

                        if (jsonStudents.length() >= jsonMaxStudents) {
                            invisibleBtnJoin();
                        }
                    } else {
                        txtMaxStudents.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonStartDate)) && _class.has(getString(R.string.jsonStartTime))) {
                        txtStartDateTime.setText(common.dbDateTimeToDateTimeString(_class.get(getString(R.string.jsonStartDate))) + " " +
                                common.dbSecondToTimeString(_class.getInt(getString(R.string.jsonStartTime))));
                    } else {
                        startDateTimeView.setVisibility(View.GONE);
                    }

                    if (_class.has(getString(R.string.jsonEndDate)) && _class.has(getString(R.string.jsonEndTime))) {
                        txtEndDateTime.setText(common.dbDateTimeToDateTimeString(_class.get(getString(R.string.jsonEndDate))) + " " +
                                common.dbSecondToTimeString(_class.getInt(getString(R.string.jsonEndTime))));
                    } else {
                        endDateTimeView.setVisibility(View.GONE);
                    }

                    if (_class.has(getString(R.string.jsonNoOfLession))) {
                        int jsonNoOfLession = (Integer) _class.get(getString(R.string.jsonNoOfLession));
                        txtNoOfLession.setText(String.valueOf(jsonNoOfLession));
                    } else {
                        txtNoOfLession.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonPrice))) {
                        int price = (int) _class.get(getString(R.string.jsonPrice));
                        txtPrice.setText("$" + String.format("%,d", price));
                    } else {
                        txtPrice.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonAddress))) {
                        String address = (String) _class.get(getString(R.string.jsonAddress));
                        txtAddress.setText(address);
                    } else {
                        txtAddress.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonTeachers))) {
                        JSONArray teachers = (JSONArray) _class.get(getString(R.string.jsonTeachers));
                        String result = "";
                        for (int i=0; i<teachers.length(); i++){
                            JSONObject teacher = teachers.getJSONObject(i);
                            String lastName = teacher.getString(getString(R.string.jsonLastName));
                            String firstName = teacher.getString(getString(R.string.jsonFirstName));
                            if (i != 1){
                                result +=  lastName + " " + firstName;
                            } else {
                                result += ", " + lastName + " " + firstName;
                            }

                        }
                        txtTeachers.setText(result);
                    }


                    btnGallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(SearchClassesResultActivity.this, SearchAttachmentActivity.class);
                            i.putExtra(getString(R.string.jsonClassID),classId);
                            i.putExtra(getString(R.string.jsonToken), _common.getToken());
                            startActivity(i);
                        }
                    });

                    if (ACTION != null && ACTION == SearchStudentFragment.ActionRecommendClassesResult) {
                        btnJoin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTask = new JoinClass(classId, _student_id).execute();
                            }
                        });
                    } else {
                        btnJoin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(SearchClassesResultActivity.this, JoinClassActivity.class);
                                i.putExtra(getString(R.string.jsonClassID), classId);
//                            startActivityForResult(i,resJoin);
                                startActivity(i);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void invisibleBtnJoin() {
                btnJoin.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public class JoinClass extends AsyncTask<Void, Void, Boolean> {

        private String class_id;
        private String student_id;

        public JoinClass(String class_id, String student_id) {
            this.class_id = class_id;
            this.student_id = student_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            load_callback = false;
            _common.showLoadingProgress(list_view, progressBar, load_callback);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(getString(R.string.jsonStudentID), student_id);
                jsonObject.put(getString(R.string.jsonClassID), class_id);
                jsonObject.put(getString(R.string.jsonToken), _common.getToken());
                _webSocketIO._socket.emit("joinClass2", jsonObject);
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
                _common.showErrorMessage(list_view, null);
            }
            load_callback = true;
            mTask = null;
            _common.showLoadingProgress(list_view, progressBar, load_callback);
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            _common.showLoadingProgress(list_view, progressBar, load_callback);
        }
    }





}
