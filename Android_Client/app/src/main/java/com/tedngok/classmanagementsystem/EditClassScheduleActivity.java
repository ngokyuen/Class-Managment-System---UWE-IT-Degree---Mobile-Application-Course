package com.tedngok.classmanagementsystem;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

import static com.tedngok.classmanagementsystem.SearchClassesResultActivity.reqMap;

public class EditClassScheduleActivity extends AppCompatActivity {

    private Common _common;
    private WebSocketIO _webSocketIO;
    private CaldroidFragment caldroidFragment;

    private RecyclerView class_schedule_list_container;
    private RecyclerView.Adapter list_adapter;
    private RecyclerView.LayoutManager list_layout_manger;
    private JSONArray classScheduleClassList;
    private String _classID, _startDate, _endDate;
    private int _startTime, _endTime;
    private FragmentManager _fm;
    private FragmentTransaction _ft;
    private Date _current_date;
    private AlertDialog upload_class_schedule_dialog;
    private CheckBox chkMyself;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_class_schedule);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        _classID = bundle.getString(getString(R.string.jsonClassID));
//        _startDate = bundle.getString(getString(R.string.jsonStartDate));
//        _startTime = bundle.getInt(getString(R.string.jsonStartTime));
//        _endDate = bundle.getString(getString(R.string.jsonEndDate));
//        _endTime = bundle.getInt(getString(R.string.jsonEndTime));

        _common = new Common(this);

        _fm = getSupportFragmentManager();
        _ft = _fm.beginTransaction();
        caldroidFragment = new CaldroidFragment();
        _ft.replace(R.id.class_fragment_container, caldroidFragment);
        _ft.commit();

        class_schedule_list_container = (RecyclerView) findViewById(R.id.class_schedule_list_container);
        list_layout_manger = new LinearLayoutManager(this);
        list_adapter = new ListAdapter(this, classScheduleClassList);
        class_schedule_list_container.setAdapter(list_adapter);
        class_schedule_list_container.setLayoutManager(list_layout_manger);

        LinearLayout selectedClassView = (LinearLayout) findViewById(R.id.selectedClassView);
        selectedClassView.setVisibility(View.VISIBLE);
        chkMyself = (CheckBox) findViewById(R.id.chkMyself);
        setAction();
        setResponse();
    }


    private void setResponse() {
        //for upload class schedule
        _webSocketIO._socket.off("getClass");
        _webSocketIO._socket.on("getClass", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject jsonObjects = (JSONObject) args[0];
                        uploadCalendar(_current_date, jsonObjects);
                    }
                });
            }
        });

        _webSocketIO._socket.off("uploadClassSchedule");
        _webSocketIO._socket.on("uploadClassSchedule", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject json = (JSONObject) args[0];
                        try {
                            if (!json.getBoolean(getString(R.string.jsonResult))) {
                                _common.showErrorMessage(class_schedule_list_container, json );
                                upload_class_schedule_dialog.dismiss();
                            } else {
                                upload_class_schedule_dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        _common.updateCalendar();
                    }
                });
            }
        });

        _webSocketIO._socket.off("deleteClassSchedule");
        _webSocketIO._socket.on("deleteClassSchedule", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _common.updateCalendar(_current_date);
                        Calendar c = Calendar.getInstance();
                        c.setTime(_current_date);
                        _common.updateCalendar(c.get(Calendar.YEAR), c.get(Calendar.MONTH));
                        caldroidFragment.refreshView();
                    }
                });
            }
        });

        _webSocketIO._socket.off("getDayClassSchedule");
        _webSocketIO._socket.on("getDayClassSchedule", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];

                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult)) && json.has(getString(R.string.jsonClassSchedules))) {
                                Boolean result = json.getBoolean(getString(R.string.jsonResult));
                                classScheduleClassList = json.getJSONArray(getString(R.string.jsonClassSchedules));
                                class_schedule_list_container.setAdapter(new ListAdapter(EditClassScheduleActivity.this, classScheduleClassList));

                            }

                            caldroidFragment.refreshView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        _webSocketIO._socket.off("getMonthClassSchedule");
        _webSocketIO._socket.on("getMonthClassSchedule", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject json = (JSONObject) args[0];

                    if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult)) && json.has(getString(R.string.jsonClassSchedules))) {
                        Boolean result = json.getBoolean(getString(R.string.jsonResult));
                        JSONArray jsonClassSchedules = json.getJSONArray(getString(R.string.jsonClassSchedules));
                        HashMap<Date, Drawable> hashMap = new HashMap<Date, Drawable>();
                        for (int i = 0; i < jsonClassSchedules.length(); i++) {
                            //Calendar calendar = Calendar.getInstance();
                            //calendar.add(Calendar.DAY_OF_YEAR, 10);
                            JSONObject jsonClassSchedule = jsonClassSchedules.getJSONObject(i);
                            JSONObject jsonClass = jsonClassSchedule.getJSONObject(getString(R.string.jsonClass));
                            String classID_ = jsonClass.getString(getString(R.string.jsonClassID));
                            String jsonStartDate = jsonClassSchedule.getString(getString(R.string.jsonStartDate));
                            String startDate = _common.dbDateTimeToDateTimeString(jsonStartDate);

                            ColorDrawable colorDrawable;
                            if (classID_ != null && _classID != null && classID_.equals(_classID)) {
                                //colorDrawable = new ColorDrawable(Color.YELLOW);
                                colorDrawable = new ColorDrawable(getResources().getColor(R.color.yellow));
                            } else {
                                //colorDrawable = new ColorDrawable(Color.GREEN);

                                colorDrawable  = new ColorDrawable(getResources().getColor(R.color.green));
                            }


                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = formatter.parse(startDate);
                            hashMap.put(date, colorDrawable);
//                            caldroidFragment.setBackgroundDrawableForDate( green , date);
                        }

                        caldroidFragment.setBackgroundDrawableForDates(hashMap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                caldroidFragment.refreshView();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void setAction() {

        chkMyself.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                caldroidFragment.getBackgroundForDateTimeMap().clear();
                caldroidFragment.refreshView();
                if (chkMyself.isChecked()){
                    _common.updateCalendar(true);
                } else {
                    _common.updateCalendar();
                }
            }
        });

        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                _current_date = date;

                if (chkMyself.isChecked()){
                    _common.updateCalendar(date, true);
                } else {
                    _common.updateCalendar(date);
                }
            }

            @Override
            public void onChangeMonth(int month, int year) {
                super.onChangeMonth(month, year);

                if (chkMyself.isChecked()){
                    _common.updateCalendar(year, month, true);
                } else {
                    _common.updateCalendar(year, month);
                }
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                _current_date = date;
                super.onLongClickDate(date, view);
                uploadCalendarPre(_current_date);
                //uploadCalendar(date);
            }

            @Override
            public void onCaldroidViewCreated() {
                super.onCaldroidViewCreated();
                if (chkMyself.isChecked()){
                    _common.updateCalendar(true);
                } else {
                    _common.updateCalendar();
                }
            }
        });


//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DAY_OF_YEAR, 10);
//        ColorDrawable green = new ColorDrawable(Color.GREEN);
//        caldroidFragment.setBackgroundDrawableForDate( green ,calendar.getTime() );
    }

    private void uploadCalendarPre(Date date){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(getString(R.string.jsonToken), _common.getToken());
            jsonObject.put(getString(R.string.jsonClassID), _classID);
            _webSocketIO._socket.emit("getClass", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadCalendar(Date date, JSONObject class_) {
        try {
            JSONObject class_JSONObject = class_.getJSONObject(getString(R.string.jsonClass));

            View view = getLayoutInflater().inflate(R.layout.dialog_add_class_schedule, null);
            final TextView txtStartTime = (TextView) view.findViewById(R.id.txtStartTime);
            final TextView txtEndTime = (TextView) view.findViewById(R.id.txtEndTime);
            final TextView txtStartDate = (TextView) view.findViewById(R.id.txtStartDate);

            Calendar temp_c = Calendar.getInstance();
            temp_c.setTime(_current_date);

            int year = temp_c.get(Calendar.YEAR);
            int month = temp_c.get(Calendar.MONTH);
            int day = temp_c.get(Calendar.DAY_OF_MONTH);

            txtStartDate.setText(String.valueOf(year)+"-"+String.valueOf(month+1)+"-"+String.valueOf(day));
            txtEndTime.setText(_common.dbSecondToTimeString(class_JSONObject.getInt(getString(R.string.jsonEndTime))));
            txtStartTime.setText(_common.dbSecondToTimeString(class_JSONObject.getInt(getString(R.string.jsonStartTime))));


            txtStartTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TextView me = (TextView) v;
                    final Calendar c = Calendar.getInstance();
                    new TimePickerDialog(EditClassScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            me.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                        }
                    }, 0, 0, true).show();
                }
            });

            txtEndTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final TextView me = (TextView) v;
                    final Calendar c = Calendar.getInstance();
                    new TimePickerDialog(EditClassScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            me.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                        }
                    }, 0, 0, true).show();
                }
            });

            Button btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
            Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

            upload_class_schedule_dialog = new AlertDialog.Builder(EditClassScheduleActivity.this)
                    .setView(view)
                    .setTitle(getString(R.string.msg_add_class_schedule))
                    .show();

            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject json = new JSONObject();
                        Calendar c = Calendar.getInstance();
                        c.setTime(_current_date);
                        json.put(getString(R.string.jsonToken), _common.getToken());
                        json.put(getString(R.string.jsonClassID), _classID);
                        json.put(getString(R.string.jsonStartDate), txtStartDate.getText().toString());

                        String[] startTime = txtStartTime.getText().toString().split(":");
                        int startHour = Integer.valueOf(startTime[0]);
                        int startMins = Integer.valueOf(startTime[1]);
                        json.put(getString(R.string.jsonStartTime), startHour * 3600 + startMins * 60);

                        String[] endTime = txtEndTime.getText().toString().split(":");
                        int endHour = Integer.valueOf(endTime[0]);
                        int endMins = Integer.valueOf(endTime[1]);
                        json.put(getString(R.string.jsonEndTime), endHour * 3600 + endMins * 60);

                        _webSocketIO._socket.emit("uploadClassSchedule", json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upload_class_schedule_dialog.dismiss();
                }
            });



        } catch (JSONException e) {
            e.printStackTrace();
        }


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
            return new ListAdapter.ListViewHolder(getLayoutInflater().from(context).inflate(R.layout.list_class_schedule_simple, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject classSchedule = (JSONObject) jsonArray.getJSONObject(position);
                ((ListAdapter.ListViewHolder) holder).bindData(classSchedule);

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
            private TextView txtStartDate, txtStartTime, txtEndTime, txtClassId, txtClassName;
            private CardView list;
            private String classId;
            private LinearLayout startDateTimeView, endDateTimeView;
            private ImageView imgDelete;

            public ListViewHolder(View itemView) {
                super(itemView);
                txtStartDate = (TextView) itemView.findViewById(R.id.txtStartDate);
                txtStartTime = (TextView) itemView.findViewById(R.id.txtStartTime);
                txtEndTime = (TextView) itemView.findViewById(R.id.txtEndTime);
                txtClassId = (TextView) itemView.findViewById(R.id.txtClassId);
                txtClassName = (TextView) itemView.findViewById(R.id.txtClassName);
                imgDelete = (ImageView) itemView.findViewById(R.id.imgDelete);
                list = (CardView) itemView.findViewById(R.id.list_class_view);
            }

            public void bindData(final JSONObject _classSchedule) {
                try {
                    JSONObject _class = _classSchedule.getJSONObject(getString(R.string.jsonClass));
                    String startDate = _classSchedule.getString(getString(R.string.jsonStartDate));
                    int startTime = _classSchedule.getInt(getString(R.string.jsonStartTime));
                    int endTime = _classSchedule.getInt(getString(R.string.jsonEndTime));

                    txtStartDate.setText(_common.dbDateTimeToDateTimeString(startDate));
                    txtStartTime.setText(_common.dbSecondToTimeString(startTime));
                    txtEndTime.setText(_common.dbSecondToTimeString(endTime));

                    if (_class.has(getString(R.string.jsonClassID))) {
                        classId = (String) _class.get(getString(R.string.jsonClassID));
                        txtClassId.setText(classId);

                        if (classId.equals(_classID)) {
                            imgDelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog dialog = new AlertDialog.Builder(EditClassScheduleActivity.this)
                                            .setTitle(getString(R.string.alert_title))
                                            .setMessage(getString(R.string.msg_confirm_delete))
                                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        JSONObject json = new JSONObject();
                                                        json.put(getString(R.string.jsonToken), _common.getToken());
                                                        json.put(getString(R.string.jsonID), _classSchedule.getString(getString(R.string.jsonID)));
                                                        _webSocketIO._socket.emit("deleteClassSchedule", json);
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
                            imgDelete.setVisibility(View.VISIBLE);
                        }

                        list.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(EditClassScheduleActivity.this, EditClassActivity.class);
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
