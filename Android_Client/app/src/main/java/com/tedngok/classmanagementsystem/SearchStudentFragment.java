package com.tedngok.classmanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

public class SearchStudentFragment extends Fragment {
    public static String ActionRecommendClassesResult = "ActionRecommendClassesResult";

    private static final String ARG_PARAM1 = "param1", ARG_PARAM2 = "param2";
    private String mParam1, mParam2, class_id, _student_id;
    private OnFragmentInteractionListener mListener;
    private SearchView search;
    private ImageView imgReload;
    private FloatingActionButton addStudentButton;
    private WebSocketIO _webSocketIO;
    private Common _common;
    private RecyclerView list_students;
    private RecyclerView.Adapter list_students_adapter;
    private RecyclerView.LayoutManager list_students_layout_manger;
    private View view;
    private ConstraintLayout new_view;
    private ProgressBar progressBar;
    private boolean load_callback = false;
    private AsyncTask<?, ?, Boolean> mTask = null;

    public SearchStudentFragment() {
    }

    public static SearchStudentFragment newInstance(String param1, String param2) {
        SearchStudentFragment fragment = new SearchStudentFragment();
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
        view = inflater.inflate(R.layout.fragment_search_student, container, false);
        search = (SearchView) view.findViewById(R.id.search);
        new_view = (ConstraintLayout) view.findViewById(R.id.new_view);

        _common = new Common(getContext());
        _webSocketIO = new WebSocketIO(getContext());
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        list_students = (RecyclerView) view.findViewById(R.id.list_students);
        list_students_layout_manger = new LinearLayoutManager(getContext());
        list_students.setLayoutManager(list_students_layout_manger);

        imgReload = (ImageView) view.findViewById(R.id.imgReload);
        imgReload.setVisibility(View.GONE);

        //checking argument & try to get value
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(getString(R.string.jsonClassID))) {
            class_id = getArguments().getString(getString(R.string.jsonClassID));
        }

        addStudentButton = (FloatingActionButton) view.findViewById(R.id.addStudentButton);
        setAddStudentButton();
        setAction();
        setResponse();
        return view;
    }

    private void setAddStudentButton() {
        addStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AddStudentActivity.class);
                startActivity(i);
            }
        });
    }

    private void setResponse() {

        _webSocketIO._socket.off("recommendClass");
        _webSocketIO._socket.on("recommendClass", new Emitter.Listener() {
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
                                    showTryAgainError(null);
                                }
                            } else {
                                showTryAgainError(null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showTryAgainError(null);
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });


        _webSocketIO._socket.off("reloadStudentList");
        _webSocketIO._socket.on("reloadStudentList", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imgReload.performClick();
                    }
                });
            }
        });

        _webSocketIO._socket.off("joinClass");
        _webSocketIO._socket.on("joinClass", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                showSuccess(getString(R.string.msg_join_success));


//                                System.out.print(json.toString());
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

                                        mTask = new RecommendClassTask(_student_id).execute();
//                                        Intent i = new Intent(getContext(), RecommendClassActivity.class);
////                                        i.putExtra(getString(R.string.class_id), class_id);
//                                        i.putExtra(getString(R.string.student_id), _student_id);
//                                        startActivity(i);
                                        showTryAgainError(json);
                                    }
                                } else {
                                    showTryAgainError(json);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showTryAgainError(null);
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });

        _webSocketIO._socket.off("getStudents");
        _webSocketIO._socket.on("getStudents", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                //showUpdateSuccess();

                                if (json.has(getString(R.string.jsonStudents))) {
                                    JSONArray students = json.getJSONArray(getString(R.string.jsonStudents));
                                    if (students.length() > 0) {
                                        list_students_adapter = new StudentListAdapter(getContext(), students, class_id);
                                        list_students.setAdapter(list_students_adapter);
                                    } else {
                                        showEmptyResultError();
                                    }
                                } else {
                                    showTryAgainError(json);
                                }
                            } else {
                                showTryAgainError(json);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showTryAgainError(null);
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });
    }

//    private void setListItems(final JSONArray students) {
//        String[] fields = new String[]{"student_id", "hkid", "lastname", "firstname"};
//        int[] fields_int = new int[]{R.id.student_id, R.id.hkid, R.id.lastname, R.id.firstname};
//
//        listAdapter = new JSONArrayAdapter(getContext(), students, R.layout.list_student, fields, fields_int);
//        list.setAdapter(listAdapter);
//
//        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent i = new Intent(getContext(), EditStudentActivity.class);
//                try {
//                    JSONObject student = students.getJSONObject(position);
//                    i.putExtra(getString(R.string.jsonStudentID), student.getString(getString(R.string.jsonStudentID)));
//                    startActivity(i);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    private void setAction() {
        search.onActionViewExpanded();
        search.setIconified(false);
        search.setQueryHint(getString(R.string.search_student_hint));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mTask = new GetStudentsTask().execute(query);
                imgReload.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                imgReload.setVisibility(View.GONE);
                return false;
            }
        });

        imgReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = search.getQuery().toString();
                mTask = new GetStudentsTask().execute(query);
                imgReload.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoadingProgress() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!load_callback) {
                    progressBar.setVisibility(View.VISIBLE);
                    list_students.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    list_students.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showEmptyResultError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(view, R.string.error_empty_result, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadSuccess() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Snackbar.make(progressBar,R.string.msg_update_success, Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    private void showClasses(JSONArray jsonArray) {
        SearchClassesResultActivity.data = jsonArray;
        Intent i = new Intent(getActivity(), SearchClassesResultActivity.class);
        i.putExtra(getString(R.string.jsonStudentID), _student_id);
        //i.putExtra(getString(R.string.jsonClasses), jsonArray.toString());
        i.putExtra(getString(R.string.jsonQuery), "");
        i.setAction(ActionRecommendClassesResult);

//        startActivity(i);
        startActivity(i);
    }

    private void showSuccess(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showTryAgainError(final JSONObject json) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (json != null && json.has(getString(R.string.jsonErrorMessage))) {
                    try {
                        String msg = json.getString(getString(R.string.jsonErrorMessage));
                        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Snackbar.make(view, R.string.error_try_again_later, Snackbar.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(view, R.string.error_try_again_later, Snackbar.LENGTH_SHORT).show();
                }

            }
        });
    }

    public class GetStudentsTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            load_callback = false;
            showLoadingProgress();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(getString(R.string.jsonQuery), params[0]);
                jsonObject.put(getString(R.string.jsonToken), _common.getToken());
                _webSocketIO._socket.emit("getStudents", jsonObject);
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
                showTryAgainError(null);
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

    public void showEmptyClassesError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(new_view, R.string.error_empty_result, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public class StudentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private JSONArray jsonArray;
        private Context context;
        private String class_id;

        public StudentListAdapter(Context context, JSONArray jsonArray, String class_id) {
            this.context = context;
            this.jsonArray = jsonArray;
            this.class_id = class_id;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new StudentListViewHolder(getActivity().getLayoutInflater().from(context).inflate(R.layout.list_student, parent, false), class_id);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject student = (JSONObject) jsonArray.getJSONObject(position);
                ((StudentListViewHolder) holder).bindDate(student);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class StudentListViewHolder extends RecyclerView.ViewHolder {
            private CardView list_student;
            private TextView txtStudentId, txtLastName, txtFirstName, txtHKID;
            private ImageView imgUser;
            private String class_id, student_id;
            private Button btnJoin, btnRecommend, btnGallery, btnBilling;

            public StudentListViewHolder(View itemView, String class_id) {
                super(itemView);
                this.class_id = class_id;
                txtStudentId = (TextView) itemView.findViewById(R.id.student_id);
                txtLastName = (TextView) itemView.findViewById(R.id.lastname);
                txtFirstName = (TextView) itemView.findViewById(R.id.firstname);
                txtHKID = (TextView) itemView.findViewById(R.id.hkid);
                btnBilling = (Button) itemView.findViewById(R.id.btnBilling);
                btnJoin = (Button) itemView.findViewById(R.id.btnJoin);
                btnRecommend = (Button) itemView.findViewById(R.id.btnRecommend);
                btnGallery = (Button) itemView.findViewById(R.id.btnGallery);
                list_student = (CardView) itemView.findViewById(R.id.list_student);
                imgUser = (ImageView) itemView.findViewById(R.id.imgUser);

            }

            public void bindDate(final JSONObject student) {
                try {
                    if (student.has(getString(R.string.jsonStudentID))) {
                        student_id = student.getString(getString(R.string.jsonStudentID));
                        txtStudentId.setText(student_id);
                    } else {
                        txtStudentId.setVisibility(View.INVISIBLE);
                    }

                    if (student.has(getString(R.string.jsonFirstName))) {
                        String firstName = student.getString(getString(R.string.jsonFirstName));
                        txtFirstName.setText(firstName);
                    } else {
                        txtFirstName.setVisibility(View.INVISIBLE);
                    }

                    if (student.has(getString(R.string.jsonLastName))) {
                        String lastName = student.getString(getString(R.string.jsonLastName));
                        txtLastName.setText(lastName);
                    } else {
                        txtLastName.setVisibility(View.INVISIBLE);
                    }

                    if (student.has(getString(R.string.jsonHKID))) {
                        String hkid = student.getString(getString(R.string.jsonHKID));
                        txtHKID.setText(hkid);
                    } else {
                        txtHKID.setVisibility(View.INVISIBLE);
                    }

                    if (student.has(getString(R.string.jsonImage))) {
                        String base64_image = student.getString(getString(R.string.jsonImage));
                        imgUser.setImageBitmap(_common.base64ToBitmap(base64_image));
                    } else {
                        //txtHKID.setVisibility(View.INVISIBLE);
                    }

                    btnGallery.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getContext(), SearchAttachmentActivity.class);
                            if (class_id != null) {
                                i.putExtra(getString(R.string.jsonClassID), class_id);
                            }
                            i.putExtra(getString(R.string.jsonStudentID), student_id);
                            i.putExtra(getString(R.string.jsonToken), _common.getToken());
                            startActivity(i);
                        }
                    });

                    if (class_id != null) {
                        btnRecommend.setVisibility(View.GONE);
                        btnJoin.setVisibility(View.VISIBLE);
                        btnJoin.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                _student_id = student_id;
                                mTask = new JoinClass(class_id, student_id).execute();
                            }
                        });
                    } else {
                        btnRecommend.setVisibility(View.VISIBLE);
                        btnRecommend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                _student_id = student_id;
                                mTask = new RecommendClassTask(student_id).execute();
                            }
                        });
                        btnJoin.setVisibility(View.GONE);
                    }

                    list_student.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getContext(), EditStudentActivity.class);
                            i.putExtra(getString(R.string.jsonStudentID), student_id);
                            startActivity(i);
                        }
                    });

                    btnBilling.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getContext(), SearchBillingResultActivity.class);
                            i.putExtra(getString(R.string.jsonToken), _common.getToken());
                            i.putExtra(getString(R.string.jsonStudentID), student_id);
                            startActivity(i);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class RecommendClassTask extends AsyncTask<Void, Void, Boolean> {

        private String student_id;

        public RecommendClassTask(String student_id) {
            this.student_id = student_id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            load_callback = false;
            showLoadingProgress();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(getString(R.string.jsonStudentID), student_id);
                jsonObject.put(getString(R.string.jsonToken), _common.getToken());
                _webSocketIO._socket.emit("recommendClass", jsonObject);
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
                showTryAgainError(null);
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
            showLoadingProgress();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(getString(R.string.jsonStudentID), student_id);
                jsonObject.put(getString(R.string.jsonClassID), class_id);
                jsonObject.put(getString(R.string.jsonToken), _common.getToken());
                _webSocketIO._socket.emit("joinClass", jsonObject);
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
                showTryAgainError(null);
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
