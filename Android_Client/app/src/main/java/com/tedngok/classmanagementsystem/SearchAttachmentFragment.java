package com.tedngok.classmanagementsystem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import general.Common;
import general.WebSocketIO;
import io.socket.emitter.Emitter;

import com.facebook.FacebookSdk;


public class SearchAttachmentFragment extends Fragment {
    private SearchView search;
    private ImageView imgReload;
    private WebSocketIO _webSocketIO;
    private Common _common;
    private RecyclerView lists;
    private RecyclerView.Adapter lists_adapter;
    private RecyclerView.LayoutManager lists_layout_manger;
    private LinearLayout searchView;
    private View view;
    private ConstraintLayout new_view;
    private AppBarLayout app_bar;
    private Button btnNew;
    private ProgressBar progressBar;
    private boolean load_callback = false;
    private AsyncTask<?, ?, Boolean> mTask = null;
    private String _class_id, _student_id;
    private String[] classesIDArray = new String[0];
    public ArrayAdapter<String> classIDArrayAdapter;

    private OnFragmentInteractionListener mListener;

    public SearchAttachmentFragment() {
    }

    public static SearchAttachmentFragment newInstance(String param1, String param2) {
        SearchAttachmentFragment fragment = new SearchAttachmentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _class_id = getArguments().getString(getString(R.string.jsonClassID));
            _student_id = getArguments().getString(getString(R.string.jsonStudentID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_attachment, container, false);
        search = (SearchView) view.findViewById(R.id.search);
        new_view = (ConstraintLayout) view.findViewById(R.id.new_view);
        app_bar = (AppBarLayout) view.findViewById(R.id.app_bar);

        _common = new Common(getContext());
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        btnNew = (Button) view.findViewById(R.id.btnNew);
        searchView = (LinearLayout) view.findViewById(R.id.searchView);
        lists = (RecyclerView) view.findViewById(R.id.lists);
        lists_layout_manger = new LinearLayoutManager(getContext());
        lists.setLayoutManager(lists_layout_manger);
        lists.setAdapter(new ListAdapter(getContext(), null));
        imgReload = (ImageView) view.findViewById(R.id.imgReload);
        imgReload.setVisibility(View.GONE);

        setResponse();
        setAction();
        return view;
    }

    private void setResponse() {
        _webSocketIO._socket.off("getStudentClass");
        _webSocketIO._socket.on("getStudentClass", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                try {
                    JSONObject json = (JSONObject) args[0];
                    if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                        if (json.has(getString(R.string.jsonClasses))) {
                            JSONArray jsonClasses = json.getJSONArray(getString(R.string.jsonClasses));
                            classesIDArray = new String[jsonClasses.length()];
                            for (int i = 0; i < jsonClasses.length(); i++) {
                                String class_id = jsonClasses.getJSONObject(i).getString(getString(R.string.jsonClassID));
                                classesIDArray[i] = class_id;
                            }

                            classIDArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, classesIDArray);
                            classIDArrayAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                load_callback = true;
                showLoadingProgress();
            }
        });

        _webSocketIO._socket.off("uploadFile");
        _webSocketIO._socket.on("uploadFile", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                _common.showMessage(getView(), R.string.msg_update_image_success);

                            } else {
                                _common.showErrorMessage(getView(), json);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _common.showErrorMessage(getView(), null);
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });

        _webSocketIO._socket.off("searchAttachments");
        _webSocketIO._socket.on("searchAttachments", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = (JSONObject) args[0];
                            if (json.has(getString(R.string.jsonResult)) && json.getBoolean(getString(R.string.jsonResult))) {
                                //showUpdateSuccess();

                                if (json.has(getString(R.string.jsonAttachments))) {
                                    JSONArray attachments = json.getJSONArray(getString(R.string.jsonAttachments));
                                    if (attachments.length() > 0) {
                                        lists_adapter = new ListAdapter(getContext(), attachments);
                                        lists.setAdapter(lists_adapter);
                                    } else {
                                        showEmptyResultError();
                                    }
                                } else {

                                    _common.showErrorMessage(getView(), json);
                                }
                            } else {

                                _common.showErrorMessage(getView(), json);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            _common.showErrorMessage(getView(), null);
                        }
                        load_callback = true;
                        showLoadingProgress();
                    }
                });
            }
        });
    }

    private void setAction() {
        if (_class_id != null) {
            searchView.setVisibility(View.GONE);
            try {
                JSONObject json = new JSONObject();
                json.put(getString(R.string.jsonClassID), _class_id);
                json.put(getString(R.string.jsonToken), _common.getToken());
                mTask = new TaskClass("searchAttachmentsByClassId").execute(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (_student_id != null) {
            searchView.setVisibility(View.GONE);
            try {

                JSONObject jsonStudentClassID = new JSONObject();
                jsonStudentClassID.put(getString(R.string.jsonToken), _common.getToken());
                jsonStudentClassID.put(getString(R.string.jsonStudentID), _student_id);
                new TaskClass("getStudentClass").execute(jsonStudentClassID);

                JSONObject json = new JSONObject();
                if (_class_id != null) {
                    json.put(getString(R.string.jsonClassID), _class_id);
                }
                json.put(getString(R.string.jsonStudentID), _student_id);
                json.put(getString(R.string.jsonToken), _common.getToken());
                mTask = new TaskClass("searchStudentAttachments").execute(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            btnNew.setVisibility(View.GONE);
        }

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout capture_method = (LinearLayout) getLayoutInflater(null).inflate(R.layout.capture_method, null);
                LinearLayout takePhotoCaptureView = (LinearLayout) capture_method.findViewById(R.id.takePhotoCaptureView);
                LinearLayout takeVideoCaptureView = (LinearLayout) capture_method.findViewById(R.id.takeVideoCaptureView);
                takeVideoCaptureView.setVisibility(View.VISIBLE);

                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setView(capture_method)
                        .show();
                takePhotoCaptureView.setOnClickListener(_common.takePhotoOnClickListener(alertDialog));
                takeVideoCaptureView.setOnClickListener(_common.takeVideoOnClickListener(alertDialog));
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    JSONObject json = new JSONObject();
                    json.put(getString(R.string.token), _common.getToken());
                    json.put(getString(R.string.jsonQuery), query);
                    mTask = new TaskClass("searchAttachments").execute(json);
                    imgReload.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                imgReload.setVisibility(View.GONE);
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            final JSONObject json = new JSONObject();
            json.put(getString(R.string.jsonToken), _common.getToken());
            json.put(getString(R.string.jsonClassID), _class_id);

            LinearLayout dialog_add_attachment = null;
            ImageView photo = null;
            EditText editDescription = null;
            VideoView video = null;
            Spinner spinnerClassID = null;

            if (resultCode == Activity.RESULT_OK) {
                dialog_add_attachment = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.dialog_add_attachment, null);
                spinnerClassID = (Spinner) dialog_add_attachment.findViewById(R.id.spinnerClassID);
                photo = (ImageView) dialog_add_attachment.findViewById(R.id.photo);
                editDescription = (EditText) dialog_add_attachment.findViewById(R.id.editDescription);
                video = (VideoView) dialog_add_attachment.findViewById(R.id.video);

                //video.start();
                if (_student_id != null) {
                    spinnerClassID.setVisibility(View.VISIBLE);
                    spinnerClassID.setAdapter(classIDArrayAdapter);
                } else {
                    spinnerClassID.setVisibility(View.GONE);
                }
            }

            if (requestCode == _common.reqTakePhotoCapture) {
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    final Bitmap imgBitmap = (Bitmap) bundle.get("data");
                    photo.setImageBitmap(imgBitmap);
                    photo.setVisibility(View.VISIBLE);
                    final EditText finalEditText = editDescription;
                    final Spinner finalSpinnerClassID = spinnerClassID;
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setNegativeButton(getString(R.string.no), null)
                            .setPositiveButton(getContext().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    try {
                                        if (_student_id != null) {
                                            json.put(getContext().getString(R.string.jsonStudentID), _student_id);
                                            if (finalSpinnerClassID != null) {
                                                 Object selectedItem = finalSpinnerClassID.getSelectedItem();
                                                if (selectedItem != null){
                                                    json.put(getContext().getString(R.string.jsonClassID), finalSpinnerClassID.getSelectedItem().toString());
                                                }

                                            }
                                        }

                                        String description = finalEditText.getText().toString();
                                        json.put(getContext().getString(R.string.jsonDescription), description);
                                        json.put(getContext().getString(R.string.jsonImage), _common.bitmapToBase64(imgBitmap));

                                        _webSocketIO._socket.emit("uploadFile", json);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setView(dialog_add_attachment)
                            .show();
                }
            } else if (requestCode == _common.reqTakeVideoCapture) {
                if (resultCode == Activity.RESULT_OK) {
                    final Uri video_uri = data.getData();
                    //ImageView photo = (ImageView) dialog_add_attachment.findViewById(R.id.photo);

                    MediaController mc = new MediaController(getContext());
                    video.setMediaController(mc);
                    video.setVisibility(View.VISIBLE);
                    video.setVideoURI(video_uri);
                    video.start();
                    //video.setZOrderOnTop(true);

                    video.requestFocus();

                    final EditText finalEditText1 = editDescription;
                    final Spinner finalSpinnerClassID = spinnerClassID;
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setNegativeButton(getString(R.string.no), null)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    try {
                                        if (_student_id != null) {
                                            json.put(getContext().getString(R.string.jsonStudentID), _student_id);
                                        }
                                        if (finalSpinnerClassID.getSelectedItem() != null && !finalSpinnerClassID.getSelectedItem().toString().equals("")) {
                                            json.put(getContext().getString(R.string.jsonClassID), finalSpinnerClassID.getSelectedItem().toString());
                                        }

                                        String description = finalEditText1.getText().toString();
                                        json.put(getContext().getString(R.string.jsonDescription), description);

                                        InputStream is = getActivity().getContentResolver().openInputStream(video_uri);
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                                        int thisLine;
                                        while ((thisLine = is.read()) != -1) {
                                            baos.write(thisLine);
                                        }
                                        baos.flush();
                                        byte[] buffer = baos.toByteArray();

                                        json.put(getContext().getString(R.string.jsonVideo), buffer);
                                        _webSocketIO._socket.emit("uploadFile", json);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setView(dialog_add_attachment)
                            .show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
            return new ListViewHolder(getActivity().getLayoutInflater().from(context).inflate(R.layout.list_attachment, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                JSONObject json = (JSONObject) jsonArray.getJSONObject(position);
                holder.setIsRecyclable(false);
                ((ListViewHolder) holder).bindDate(json);

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
            private CardView list_attachment;
            private TextView txtDescription, txtCreatedAt, classItem, studentItem;
            private ImageView photo, delete, ReloadImg, fbShare;
            private VideoView video;


            public ListViewHolder(View itemView) {
                super(itemView);
//                this.class_id = class_id;
                photo = (ImageView) itemView.findViewById(R.id.photo);
                delete = (ImageView) itemView.findViewById(R.id.imgDelete);
                video = (VideoView) itemView.findViewById(R.id.video);
                ReloadImg = (ImageView) itemView.findViewById(R.id.ReloadImg);
                fbShare = (ImageView) itemView.findViewById(R.id.fbShare);
                txtDescription = (TextView) itemView.findViewById(R.id.description);
                txtCreatedAt = (TextView) itemView.findViewById(R.id.created_at);
                list_attachment = (CardView) itemView.findViewById(R.id.list_attachment);
                classItem = (TextView) itemView.findViewById(R.id.classItem);
                studentItem = (TextView) itemView.findViewById(R.id.studentItem);
            }

            public void bindDate(final JSONObject json) {

                if (json == null) {
                    return;
                }

                try {
                    //for description
                    if (json.has(getString(R.string.jsonDescription))) {
                        String description = json.getString(getString(R.string.jsonDescription));
                        txtDescription.setText(description);
                    } else {
                        txtDescription.setVisibility(View.INVISIBLE);
                    }

                    //show photo
                    if (json.has(getString(R.string.jsonAttachExtension)) && json.has(getString(R.string.jsonAttachFileName))) {
                        String file_name = json.getString(getString(R.string.jsonAttachFileName));
                        String ext = json.getString(getString(R.string.jsonAttachExtension));
                        if (ext.equals(getString(R.string.jsonAttachExtensionPNG))) {
                            _common.new GetUrlImageClass(list_attachment).execute(getString(R.string.file_server) + "/public/" + file_name + ext);
                        } else if (ext.equals(getString(R.string.jsonAttachExtensionMP4))) {
                            _common.new GetUrlVideoClass(list_attachment).execute(getString(R.string.file_server) + "/public/" + file_name + ext);
                        }
                    }


                    if (json.has(getString(R.string.jsonCreatedAt))) {
                        String created_at = json.getString(getString(R.string.jsonCreatedAt));
                        txtCreatedAt.setText(_common.dbDateTimeToDateTimeString(created_at) + " " + _common.dbDateTimeToTimeString(created_at));
                    } else {
                        txtCreatedAt.setVisibility(View.INVISIBLE);
                    }

                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                    .setMessage(getString(R.string.msg_delete_confirm))
                                    .setNegativeButton(getString(R.string.no), null)
                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                JSONObject send_json = new JSONObject();
                                                send_json.put(getString(R.string.jsonToken), _common.getToken());
                                                send_json.put(getString(R.string.jsonID), json.getString(getString(R.string.jsonID)));
                                                _webSocketIO._socket.emit("deleteAttachment", send_json);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                                    .show();
                        }
                    });



                    if (json.has(getString(R.string.jsonClass))) {
                        JSONObject class_ = json.getJSONObject(getString(R.string.jsonClass));
                        String class_id_ = class_.getString(getString(R.string.jsonClassID));
                        classItem.setText(getString(R.string.class_id) + ": " + class_id_);
                    }

                    if (json.has(getString(R.string.jsonStudent))) {
                        JSONObject student_ = json.getJSONObject(getString(R.string.jsonStudent));
                        String student_id_ = student_.getString(getString(R.string.jsonStudentID));
                        studentItem.setText(getString(R.string.student_id) + ": " + student_id_);
                    }

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
            showLoadingProgress();
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
            showLoadingProgress();
        }

        @Override
        protected void onCancelled() {
            mTask = null;
            showLoadingProgress();
        }
    }

    private void showLoadingProgress() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!load_callback) {
                    progressBar.setVisibility(View.VISIBLE);
                    lists.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    lists.setVisibility(View.VISIBLE);
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

    private void showSuccess(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
