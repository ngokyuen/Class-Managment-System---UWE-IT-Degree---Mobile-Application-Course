package com.tedngok.classmanagementsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class SMSLogFragment extends Fragment {

    private WebSocketIO _webSocketIO;
    private Common _common;
    private RecyclerView lists;
    private RecyclerView.Adapter lists_adapter;
    private RecyclerView.LayoutManager lists_layout_manger;
    private OnFragmentInteractionListener mListener;
    private AppBarLayout app_bar;
    private ProgressBar progressBar;
    private FloatingActionButton refresh;
    private boolean load_callback = false;
    private AsyncTask<?, ?, Boolean> mTask = null;

    public SMSLogFragment() {
    }

    public static SMSLogFragment newInstance() {
        SMSLogFragment fragment = new SMSLogFragment();
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
        View view = inflater.inflate(R.layout.fragment_list, null);
        app_bar = (AppBarLayout) view.findViewById(R.id.app_bar);
        _common = new Common(getContext());
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        refresh = (FloatingActionButton) view.findViewById(R.id.refresh);
        refresh.setVisibility(View.VISIBLE);
        lists = (RecyclerView) view.findViewById(R.id.lists);
        lists_layout_manger = new LinearLayoutManager(getContext());
        lists.setLayoutManager(lists_layout_manger);
        lists.setAdapter(new ListAdapter(getContext(), null));

//        refresh = (FloatingActionButton) view.findViewById(R.id.refresh);
//        refresh.setVisibility(View.VISIBLE);
//        lists = (RecyclerView) view.findViewById(R.id.list);
//        lists_layout_manger = new LinearLayoutManager(getContext());
//        lists.setLayoutManager(lists_layout_manger);
//        lists.setAdapter(new ListAdapter(getContext(), null));
//        AppBarLayout app_bar = (AppBarLayout) view.findViewById(R.id.app_bar);
//        app_bar.setVisibility(View.GONE);

        setResponse();
        setAction();
        return view;
    }

    private void setResponse() {
        _webSocketIO._socket.off("getSMSLog");
        _webSocketIO._socket.on("getSMSLog", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final JSONObject json = (JSONObject) args[0];
                try {
                    if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {

                        if (json.has(getString(R.string.jsonSMSs))) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    JSONArray smss = null;
                                    try {
                                        smss = json.getJSONArray(getString(R.string.jsonSMSs));
                                        if (smss.length() > 0) {
                                            lists_adapter = new ListAdapter(getContext(), smss);
                                            lists.setAdapter(lists_adapter);
                                        } else {
                                            _common.showErrorMessage(getView(), json);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            _common.showErrorMessage(getView(), json);
                        }
                    } else {
                        _common.showErrorMessage(getView(), json);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                load_callback = true;
                _common.showLoadingProgress(lists, progressBar, load_callback);
            }
        });
    }

    private void reload() {
        try {
            JSONObject json = new JSONObject();
            json.put(getString(R.string.jsonToken), _common.getToken());
            new TaskClass("getSMSLog").execute(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setAction() {
        reload();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private JSONArray jsonArray;
        private Context context;

        public ListAdapter(Context context, JSONArray jsonArray) {
            this.context = context;
            this.jsonArray = jsonArray;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListAdapter.ListViewHolder(getActivity().getLayoutInflater().from(context).inflate(R.layout.list_sms2, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject json = (JSONObject) jsonArray.getJSONObject(position);
                holder.setIsRecyclable(false);
                ((ListAdapter.ListViewHolder) holder).bindDate(json);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (jsonArray != null)
                return jsonArray.length();
            else
                return 0;
        }

        public class ListViewHolder extends RecyclerView.ViewHolder {

            //private TextView txtMobile, txtStudentName;
            private TextView txtClassName, txtCreatedAt;
            private EditText editSMSContent;
            private GridLayout list_sms_tabs_view;

            public ListViewHolder(View itemView) {
                super(itemView);
//                txtMobile = (TextView) itemView.findViewById(R.id.txtMobile);
                txtClassName = (TextView) itemView.findViewById(R.id.txtClassName);
//                txtStudentName = (TextView) itemView.findViewById(R.id.txtStudentName);
                editSMSContent = (EditText) itemView.findViewById(R.id.editSMSContent);
                txtCreatedAt = (TextView) itemView.findViewById(R.id.txtCreatedAt);
                editSMSContent.setEnabled(false);
                list_sms_tabs_view = (GridLayout) itemView.findViewById(R.id.list_sms_tabs_view);


            }

            public void bindDate(final JSONObject json) {
                if (json == null) {
                    return;
                }

                try {
                    JSONArray contents = json.getJSONArray(getString(R.string.jsonContents));
                    JSONArray billings = json.getJSONArray(getString(R.string.jsonBillings));
                    JSONObject billing = billings.getJSONObject(0);
                    JSONObject class_ = billing.getJSONObject(getString(R.string.jsonClass));
                    txtClassName.setText(class_.getString(getString(R.string.jsonClassName)));
                    String className = billings.getJSONObject(0).getJSONObject(getString(R.string.jsonClass)).toString();
                    String createdAt = json.getString(getString(R.string.jsonCreatedAt));
                    txtCreatedAt.setText(_common.dbDateTimeToDateTimeString(createdAt) + " " + _common.dbDateTimeToTimeString(createdAt) );

                    for(int i=0; i< billings.length(); i++){
                        JSONObject student = billings.getJSONObject(i).getJSONObject(getString(R.string.jsonStudent));
                        String mobile = student.getString(getString(R.string.jsonMobileNo));
                        String student_name = student.getString(getString(R.string.jsonLastName)) + " " + student.getString(getString(R.string.jsonFirstName));
                        final String content = contents.getString(i);

                        LinearLayout list_sms_tab = (LinearLayout) getLayoutInflater(null).inflate(R.layout.list_sms_tab, null);
                        TextView tab_mobile = (TextView) list_sms_tab.findViewById(R.id.txtMobile);
                        tab_mobile.setText(mobile);
                        TextView tab_student_name = (TextView) list_sms_tab.findViewById(R.id.txtStudentName);
                        tab_student_name.setText(student_name);

                        list_sms_tab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                editSMSContent.setText(content);
                            }
                        });
                        list_sms_tabs_view.addView(list_sms_tab);
                    }

//                    txtMobile.setText(mobile_no_str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public class TaskClass extends AsyncTask<JSONObject, Void, Boolean> {

        private String task_name;

        public TaskClass(String task_name) {
            this.task_name = task_name;
        }

        @Override
        protected Boolean doInBackground(JSONObject... params) {
            load_callback = false;
            _common.showLoadingProgress(lists, progressBar, load_callback);
            JSONObject json = (JSONObject) params[0];
            _webSocketIO._socket.emit(task_name, json);
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
                _common.showErrorMessage(getView(), null);
            }
            load_callback = true;
            mTask = null;
            ;
            _common.showLoadingProgress(lists, progressBar, load_callback);
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            ;
            _common.showLoadingProgress(lists, progressBar, load_callback);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
