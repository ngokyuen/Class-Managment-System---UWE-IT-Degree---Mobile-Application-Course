package com.tedngok.classmanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class ClassScheduleFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private FragmentManager _fm;
    private FragmentTransaction _ft;
    private CaldroidFragment caldroidFragment;
    private WebSocketIO _webSocketIO;
    private boolean load_callback = false;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private AppBarLayout app_bar;
    private CheckBox chkMyself;

    private RecyclerView class_schedule_list_container;
    private RecyclerView.Adapter list_adapter;
    private RecyclerView.LayoutManager list_layout_manger;
    private JSONArray classScheduleClassList;
    private Common _common;

    public ClassScheduleFragment() {
    }

    public static ClassScheduleFragment newInstance() {
        ClassScheduleFragment fragment = new ClassScheduleFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_schedule, container, false);

        _common = new Common(getContext());

        _fm = getActivity().getSupportFragmentManager();
        _ft = _fm.beginTransaction();
        caldroidFragment = new CaldroidFragment();
        _ft.replace(R.id.class_fragment_container, caldroidFragment);
        _ft.commit();

        class_schedule_list_container = (RecyclerView) view.findViewById(R.id.class_schedule_list_container);
        list_layout_manger = new LinearLayoutManager(getContext());
        list_adapter = new ListAdapter(getContext(), classScheduleClassList);
        class_schedule_list_container.setAdapter(list_adapter);
        class_schedule_list_container.setLayoutManager(list_layout_manger);
        chkMyself = (CheckBox) view.findViewById(R.id.chkMyself);

        setAction();
        setResponse();
        return view;
    }

    private void setResponse() {
        _webSocketIO._socket.off("getDayClassSchedule");
        _webSocketIO._socket.on("getDayClassSchedule", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];

                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult)) && json.has(getString(R.string.jsonClassSchedules))) {
                                Boolean result = json.getBoolean(getString(R.string.jsonResult));
                                classScheduleClassList = json.getJSONArray(getString(R.string.jsonClassSchedules));
                                class_schedule_list_container.setAdapter(new ListAdapter(getContext(),classScheduleClassList));
                                caldroidFragment.refreshView();
                            }

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
                            String jsonStartDate = jsonClassSchedule.getString(getString(R.string.jsonStartDate));
                            String startDate = _common.dbDateTimeToDateTimeString(jsonStartDate);
                            ColorDrawable green = new ColorDrawable(getResources().getColor(R.color.green));

                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            Date date = formatter.parse(startDate);
                            hashMap.put(date, green);
//                            caldroidFragment.setBackgroundDrawableForDate( green , date);
                        }

                        caldroidFragment.setBackgroundDrawableForDates(hashMap);
                        getActivity().runOnUiThread(new Runnable() {
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
                } catch(IllegalStateException e){
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
                super.onLongClickDate(date, view);
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
            return new ListAdapter.ListViewHolder(getLayoutInflater(null).from(context).inflate(R.layout.list_class_schedule, parent, false));
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
            if (jsonArray == null){
                return 0;
            } else {
                return jsonArray.length();
            }
        }

        public class ListViewHolder extends RecyclerView.ViewHolder {
            private TextView txtStartDate, txtStartTime, txtEndTime,  txtAddress, txtClassId, txtClassName, txtStartDateTime, txtEndDateTime, txtPrice, txtMaxStudents, txtStatus, txtNoOfLession, txtTeachers;
            private CardView list;
            private ImageView imgMap, imgType;
            private String classId;
            private LinearLayout startDateTimeView, endDateTimeView;

            public ListViewHolder(View itemView) {
                super(itemView);

                txtStartDate = (TextView) itemView.findViewById(R.id.txtStartDate);
                txtStartTime = (TextView) itemView.findViewById(R.id.txtStartTime);
                txtEndTime = (TextView) itemView.findViewById(R.id.txtEndTime);

                txtAddress = (TextView) itemView.findViewById(R.id.txtAddress);
                txtClassId = (TextView) itemView.findViewById(R.id.txtClassId);
                txtClassName = (TextView) itemView.findViewById(R.id.txtClassName);
                txtStartDateTime = (TextView) itemView.findViewById(R.id.txtStartDateTime);
                txtEndDateTime = (TextView) itemView.findViewById(R.id.txtEndDateTime);
                txtPrice = (TextView) itemView.findViewById(R.id.txtPrice);
                txtMaxStudents = (TextView) itemView.findViewById(R.id.txtMaxStudents);
                txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
                txtNoOfLession = (TextView) itemView.findViewById(R.id.txtNoOfLession);
                imgType = (ImageView) itemView.findViewById(R.id.imgType);
                imgMap = (ImageView) itemView.findViewById(R.id.imgMap);
                txtTeachers = (TextView) itemView.findViewById(R.id.txtTeachers);

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

                    if (_class.has(getString(R.string.jsonLat)) && _class.has(getString(R.string.jsonLng))) {
                        final Double jsonLat = (Double) _class.get(getString(R.string.jsonLat));
                        final Double jsonLng = (Double) _class.get(getString(R.string.jsonLng));
                        final String jsonAddress = (String) _class.get(getString(R.string.jsonAddress));
                        imgMap.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getContext(), MapsActivity.class);
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
                        } else if (jsonType.equals(getString(R.string.jsonTypeSmall))) {
                            imgType.setImageResource(R.drawable.presentation);
                        }
                    } else {
                        imgType.setVisibility(View.INVISIBLE);
                    }

                    if (_class.has(getString(R.string.jsonClassID))) {
                        classId = (String) _class.get(getString(R.string.jsonClassID));
                        txtClassId.setText(classId);

                        list.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(getContext(), EditClassActivity.class);
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


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }


    }
}
