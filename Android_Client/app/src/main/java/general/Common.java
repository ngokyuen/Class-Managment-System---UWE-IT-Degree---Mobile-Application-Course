package general;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.tedngok.classmanagementsystem.NetworkErrorActivity;
import com.tedngok.classmanagementsystem.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.tedngok.classmanagementsystem.R.id.ReloadImg;
import static com.tedngok.classmanagementsystem.R.id.fbShare;
import static com.tedngok.classmanagementsystem.R.id.photo;

public class Common {
    private static WebSocketIO webSocketIO;
    private Context context;
    final public int time_zone = 8;
    final public int reqTakePhotoCapture = 1, reqTakePhotoFromFile = 2, reqTakeVideoCapture = 3, reqPageWithoutEmpty = 4;

    public Common(Context context) {
        this.context = context;
        setResponse();
    }

    private void setResponse() {


        if (webSocketIO != null && webSocketIO._socket != null){
            webSocketIO._socket.off("disconnect");
            webSocketIO._socket.on("disconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Intent i = new Intent(context, NetworkErrorActivity.class);
                    context.startActivity(i);
                }
            });
        }

    }

    public void sendEmail() {

    }

    public class GetUrlVideoClass extends AsyncTask<String, Void, Uri> {

        private VideoView view;
        private ImageView view_reload, view_fb_share, view_in_share;
        private TextView classItem, studentItem, created_at;
        private CardView list_attachment;
        private String url;
        private Uri uri;

        public GetUrlVideoClass(CardView list_attachment) {
            this.list_attachment = list_attachment;
            this.view = (VideoView) list_attachment.findViewById(R.id.video);
            this.view_reload = (ImageView) list_attachment.findViewById(R.id.ReloadImg);
            this.view_fb_share = (ImageView) list_attachment.findViewById(R.id.fbShare);
            this.view_in_share = (ImageView) list_attachment.findViewById(R.id.inShare);
            classItem = (TextView) list_attachment.findViewById(R.id.classItem);
            studentItem = (TextView) list_attachment.findViewById(R.id.studentItem);
            created_at = (TextView) list_attachment.findViewById(R.id.created_at);
        }

        @Override
        protected Uri doInBackground(String... params) {
            try {
                url = params[0];
                uri = Uri.parse(url);
                return uri;
                //Base64InputStream in = (Base64InputStream) url_connect.getInputStream();

                //Bitmap bitmap = BitmapFactory.decodeStream(in);
//                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Uri uri) {
            super.onPostExecute(uri);

            view.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    view.setVisibility(View.GONE);
                    view_reload.setVisibility(View.VISIBLE);
                    view_reload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new GetUrlVideoClass(list_attachment).execute(url);
                        }
                    });
                    return false;
                }
            });

            view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    view.setVisibility(View.VISIBLE);
                    view_reload.setVisibility(View.GONE);
                    view_reload.setOnClickListener(null);

                    final String classItemStr = classItem.getText().toString();
                    final String studentItemStr = studentItem.getText().toString();
                    final String createAtStr = created_at.getText().toString();
                    view_fb_share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            ShareVideo fb_video = new ShareVideo.Builder()
//                                    .setLocalUrl(uri)
//                                    .build();
//                            ShareVideoContent  fb_content = new ShareVideoContent.Builder()
//                                    .setVideo(fb_video)
//                                    .build();

                            ShareHashtag fb_hash_tag = new ShareHashtag.Builder()
                                    .setHashtag("#UWE_CMS_" + classItemStr.replaceAll(" ", "").replaceAll("ClassID:", "") +
                                            "_" + studentItemStr.replaceAll(" ", "").replaceAll("StudentID:", "") +
                                            "_" + createAtStr.replaceAll(" ", ".").replaceAll("-", "_"))
                                    .build();
                            ShareLinkContent fb_content = new ShareLinkContent.Builder()
                                    .setContentUrl(uri)
                                    .setQuote("UWE CMS VIDEO SHARING: " + "#" + classItemStr + "#" + studentItemStr + "#" + createAtStr)
                                    .setShareHashtag(fb_hash_tag)
                                    .build();
                            ShareDialog fb_dialog = new ShareDialog((Activity) context);
                            fb_dialog.show(fb_content);
                        }
                    });
                    view_fb_share.setVisibility(View.VISIBLE);

                    view_in_share.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("text/*");
                            i.putExtra(Intent.EXTRA_SUBJECT, "UWE SHARE VIDEO LINK");
                            i.putExtra(Intent.EXTRA_TEXT, "UWE CMS VIDEO SHARING: \n#" + classItemStr + "\n#" + studentItemStr + "\n#" + createAtStr + "\n" + url);
                            context.startActivity(Intent.createChooser(i, "Share Video Link To"));
                        }
                    });
                    view_in_share.setVisibility(View.VISIBLE);
                }
            });

            view.setVisibility(View.VISIBLE);
            MediaController mc = new MediaController(context);
            view.setMediaController(mc);
            view.setVideoURI(uri);
            view.getHolder().setSizeFromLayout();
            //view.start();
        }
    }

    public class GetUrlImageClass extends AsyncTask<String, Void, Bitmap> {

        private ImageView view, view_reload, view_fb_share, view_in_share;
        private TextView classItem, studentItem, created_at;
        private CardView list_attachment;
        private String url;

        public GetUrlImageClass(CardView list_attachment) {
            list_attachment = list_attachment;
            view = (ImageView) list_attachment.findViewById(R.id.photo);
            view_reload = (ImageView) list_attachment.findViewById(R.id.ReloadImg);
            view_fb_share = (ImageView) list_attachment.findViewById(R.id.fbShare);
            view_in_share = (ImageView) list_attachment.findViewById(R.id.inShare);
            classItem = (TextView) list_attachment.findViewById(R.id.classItem);
            studentItem = (TextView) list_attachment.findViewById(R.id.studentItem);
            created_at = (TextView) list_attachment.findViewById(R.id.created_at);
        }

        @Override

        protected Bitmap doInBackground(String... params) {
            try {
                url = params[0];
                URL target_url = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) target_url.openConnection();
                conn.setDoInput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String result = "";
                String str;
                while ((str = in.readLine()) != null) {
                    result = result + str;
                }
                in.close();
                return base64ToBitmap(result);

                //Base64InputStream in = (Base64InputStream) url_connect.getInputStream();

                //Bitmap bitmap = BitmapFactory.decodeStream(in);
//                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                view_reload.setVisibility(View.GONE);
                view_reload.setOnClickListener(null);
                view.setImageBitmap(bitmap);
                view.setVisibility(View.VISIBLE);

                final String classItemStr = classItem.getText().toString();
                final String studentItemStr = studentItem.getText().toString();
                final String createAtStr = created_at.getText().toString();
                view_fb_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        SharePhoto fb_photo = new SharePhoto.Builder()
                                .setBitmap(bitmap)
                                .build();
                        ShareHashtag fb_hash_tag = new ShareHashtag.Builder()
                                .setHashtag("#UWE_CMS_" + classItemStr.replaceAll(" ", "").replaceAll("ClassID:", "") +
                                        "_" + studentItemStr.replaceAll(" ", "").replaceAll("StudentID:", "") +
                                        "_" + createAtStr.replaceAll(" ", ".").replaceAll("-", "_"))
                                .build();
                        SharePhotoContent fb_content = new SharePhotoContent.Builder()
                                .addPhoto(fb_photo)
                                .setShareHashtag(fb_hash_tag)
                                .build();
                        ShareDialog fb_dialog = new ShareDialog((Activity) context);
                        fb_dialog.show(fb_content);
                    }
                });
                view_in_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "share_image", null);

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("image/*");
                        i.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                        i.putExtra(Intent.EXTRA_TEXT, "UWE CMS IMAGE SHARING: \n#" + classItemStr + "\n#" + studentItemStr + "\n#" + createAtStr + "\n" + url);
                        i.putExtra(Intent.EXTRA_SUBJECT, "UWE SHARE IMAGE");
                        context.startActivity(Intent.createChooser(i, "Share Image To"));
                    }
                });
                view_fb_share.setVisibility(View.VISIBLE);
                view_in_share.setVisibility(View.VISIBLE);

            } else {
//                view.setImageResource(R.drawable.reload); view.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
                view_reload.setVisibility(View.VISIBLE);
                view_reload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new GetUrlImageClass(list_attachment).execute(url);
                    }
                });
            }

        }
    }

    public View.OnClickListener takePhotoOnClickListener(final AlertDialog photoDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(context.getPackageManager()) != null) {
                    ((Activity) context).startActivityForResult(i, reqTakePhotoCapture);
                    if (photoDialog != null)
                        photoDialog.dismiss();
                }
            }
        };
    }

    public View.OnClickListener takeVideoOnClickListener(final AlertDialog photoDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                if (i.resolveActivity(context.getPackageManager()) != null) {
                    ((Activity) context).startActivityForResult(i, reqTakeVideoCapture);
                    photoDialog.dismiss();
                }
            }
        };
    }

    public void reqCameraPermission() {
    }

    public String dbDateTimeToTimeString(Object dbDateTime) {
        SimpleDateFormat db_df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        Date date = null;
        String resultDateTime = null;

        try {
            date = db_df.parse(dbDateTime.toString());
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, time_zone);
            resultDateTime = df.format(calendar.getTime());
            return resultDateTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String dbDateTimeToDateTimeString(Object dbDateTime) {
        SimpleDateFormat db_df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Date date = null;
        String resultDateTime = null;

        try {
            date = db_df.parse(dbDateTime.toString());
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, time_zone);
            resultDateTime = df.format(calendar.getTime());
            return resultDateTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int dbTimeStringToSecond(String time) {
        String[] Time = time.split(":");
        int Hour = Integer.valueOf(Time[0]);
        int Mins = Integer.valueOf(Time[1]);
        return Hour * 3600 + Mins * 60;
    }

    public String dbSecondToTimeString(int second) {
        int hour = (int) (second / 3600);
        int mins = (int) ((second % 3600) / 60);
        return String.format("%02d", hour) + ":" + String.format("%02d", mins);
    }

    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream steam = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, steam);
        byte[] byte_array = steam.toByteArray();
        String img_base64 = Base64.encodeToString(byte_array, Base64.DEFAULT);
        return img_base64;
    }

    public Bitmap base64ToBitmap(String img_base64) {
        byte[] byte_array = Base64.decode(img_base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byte_array, 0, byte_array.length);
        return bitmap;
    }


    public JSONObject verifyPassword(String password, String retype_password) {
        JSONObject json = new JSONObject();
        try {
            if (!password.equals(retype_password)) {
                json.put(context.getString(R.string.jsonResult), false);
                json.put(context.getString(R.string.jsonErrorMsg), context.getString(R.string.error_retype_password_not_match));
            } else if (password.length() < 4 && !password.equals("")) {
                json.put(context.getString(R.string.jsonResult), false);
                json.put(context.getString(R.string.jsonErrorMsg), context.getString(R.string.error_invalid_password));
            } else {
                json.put(context.getString(R.string.jsonResult), true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public String getToken() {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.login_preference_file), Context.MODE_PRIVATE);
        return sp.getString(context.getString(R.string.token), "");
    }

    public void showMessage(View view, int string_id) {
        Snackbar.make(view, context.getString(string_id), Snackbar.LENGTH_SHORT).show();
    }

    public void showErrorMessage(View view, JSONObject json) {
        if (json != null) {
            if (json.has(context.getString(R.string.jsonErrorMessage))) {
                try {
                    String msg = json.getString(context.getString(R.string.jsonErrorMessage));
                    showMessage(view, msg);
                } catch (JSONException e) {
                    showMessage(view, R.string.error_try_again_later);
                    e.printStackTrace();
                }

            } else {
                showMessage(view, R.string.error_try_again_later);
            }
        }
    }

    public void showMessage(View view, final String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public ArrayAdapter<CharSequence> getStatusAdpater() {
        ArrayAdapter<CharSequence> statusAdpater = android.widget.ArrayAdapter.createFromResource(context, R.array.status, android.R.layout.simple_spinner_item);
        statusAdpater.setDropDownViewResource(R.layout.dropdown_status);
        return statusAdpater;
    }

    public ArrayAdapter<CharSequence> getMinLevelTypeAdpater() {
        ArrayAdapter<CharSequence> minLevelTypeAdapter = android.widget.ArrayAdapter.createFromResource(context, R.array.minLevelType, android.R.layout.simple_spinner_item);
        minLevelTypeAdapter.setDropDownViewResource(R.layout.dropdown_status);
        return minLevelTypeAdapter;
    }

    public ArrayAdapter<CharSequence> getBillingStatusAdapter() {
        ArrayAdapter<CharSequence> billingStatusAdapter = android.widget.ArrayAdapter.createFromResource(context, R.array.billingStatus, android.R.layout.simple_spinner_item);
        billingStatusAdapter.setDropDownViewResource(R.layout.dropdown_status);
        return billingStatusAdapter;
    }

    public int getStatus(String value) {
        String[] status = context.getResources().getStringArray(R.array.status);
        String[] statusValue = context.getResources().getStringArray(R.array.statusValue);
        for (int i = 0; i < status.length; i++) {
            if (statusValue[i].toString().equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public int getBillingStatus(String value) {
        String[] status = context.getResources().getStringArray(R.array.billingStatus);
        String[] statusValue = context.getResources().getStringArray(R.array.billingStatusValue);
        for (int i = 0; i < status.length; i++) {
            if (statusValue[i].toString().equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public String statusToValue(String value) {
        String[] status = context.getResources().getStringArray(R.array.status);
        String[] statusValue = context.getResources().getStringArray(R.array.statusValue);
        for (int i = 0; i < status.length; i++) {
            if (status[i].toString().equals(value)) {
                return statusValue[i];
            }
        }
        return "";
    }

    public String billingStatusToValue(String value) {
        String[] status = context.getResources().getStringArray(R.array.billingStatus);
        String[] statusValue = context.getResources().getStringArray(R.array.billingStatusValue);
        for (int i = 0; i < status.length; i++) {
            if (status[i].toString().equals(value)) {
                return statusValue[i];
            }
        }
        return "";
    }

    public String valueToStatus(String value) {
        String[] status = context.getResources().getStringArray(R.array.status);
        String[] statusValue = context.getResources().getStringArray(R.array.statusValue);
        for (int i = 0; i < statusValue.length; i++) {
            if (statusValue[i].toString().equals(value)) {
                return status[i];
            }
        }
        return "";
    }

    public String billingValueToStatus(String value) {
        String[] status = context.getResources().getStringArray(R.array.billingStatus);
        String[] statusValue = context.getResources().getStringArray(R.array.billingStatusValue);
        for (int i = 0; i < statusValue.length; i++) {
            if (statusValue[i].toString().equals(value)) {
                return status[i];
            }
        }
        return "";
    }

    public int getMinLevelType(String value) {
        String[] minLevelType = context.getResources().getStringArray(R.array.minLevelType);
        for (int i = 0; i < minLevelType.length; i++) {
            if (minLevelType[i].toString().equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public boolean onSupportNavigateUp() {
        ((Activity) context).onBackPressed();
        return true;
    }


    public void showLoadingProgress(final View contentView, final ProgressBar progressBar, final Boolean load_callback) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!load_callback) {
                    progressBar.setVisibility(View.VISIBLE);
                    contentView.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    contentView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void updateCalendar(boolean myself) {
        try {
            Calendar c = Calendar.getInstance();
            JSONObject json = new JSONObject();
            json.put("year", c.get(Calendar.YEAR));
            json.put("month", c.get(Calendar.MONTH));
            json.put(context.getString(R.string.jsonToken),getToken());

            if (myself){
                json.put("myself", true);
            } else {
                json.put("myself", false);
            }
            webSocketIO._socket.emit("getMonthClassSchedule", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateCalendar() {
        updateCalendar(false);
    }

    public void updateCalendar(int year, int month, boolean myself) {
        try {
            Calendar c = Calendar.getInstance();
            JSONObject json = new JSONObject();
            json.put("year", c.get(Calendar.YEAR));
            json.put("month", c.get(Calendar.MONTH));
            json.put(context.getString(R.string.jsonToken),getToken());
            if (myself){
                json.put("myself", true);
            } else {
                json.put("myself", false);
            }
            webSocketIO._socket.emit("getMonthClassSchedule", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateCalendar(int year, int month) {
        updateCalendar(year, month, false);
    }

    public void updateCalendar(Date date, boolean myself) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        try {
            JSONObject json = new JSONObject();
            json.put("year", year);
            json.put("month", month);
            json.put("day", day);
            json.put(context.getString(R.string.jsonToken),getToken());
            if (myself){
                json.put("myself", true);
            } else {
                json.put("myself", false);
            }
            webSocketIO._socket.emit("getDayClassSchedule", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateCalendar(Date date) {
        updateCalendar(date, false);
    }

    //permission
    public boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public boolean isQualificationValueFromTo(String from, String to) {
        if (from.equals("") && to.equals("")) {
            return true;
        }

        if ((!from.equals("") && to.equals("")) || (!to.equals("") && from.equals(""))) {
            return true;
        }

        int to_ = Integer.parseInt(to);
        int from_ = Integer.parseInt(from);
        int compare = Integer.compare(to_, from_);
        if (compare >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isQualificationValueValid(String value) {
        if (value.equals(null) || value.equals("")) {
            return true;
        } else {

            int i = Integer.parseInt(value);
            if (i > 0 && i < 10) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isDateTimeFromTo(String from, String to) {
        if (from.equals("") && to.equals("")) {
            return true;
        }

        if ((!from.equals("") && to.equals("")) || (!to.equals("") && from.equals(""))) {
            return true;
        }

        String[] from_ = from.split("-");
        String[] to_ = to.split("-");

        boolean result = true;
        for(int i=0;i<from_.length;i++){
            if (Integer.parseInt(to_[i]) < Integer.parseInt(from_[i])){
                result = false;
            }
        }

        if (result) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isTimeFromTo(String from, String to) {
        if (from.equals("") && to.equals("")) {
            return true;
        }

        if ((!from.equals("") && to.equals("")) || (!to.equals("") && from.equals(""))) {
            return true;
        }

        int from_ = dbTimeStringToSecond(from);
        int to_ = dbTimeStringToSecond(to);

        int compare = Integer.compare(to_, from_);
        if (compare >= 0) {
            return true;
        } else {
            return false;
        }

    }


    public boolean isPriceFromTo(String from, String to) {
        if (from.equals("") && to.equals("")) {
            return true;
        }

        if ((!from.equals("") && to.equals("")) || (!to.equals("") && from.equals(""))) {
            return true;
        }

        double from_ = Double.parseDouble(from);
        double to_ = Double.parseDouble(to);

        int compare = Double.compare(to_, from_);
        if (compare >= 0) {
            return true;
        } else {
            return false;
        }

    }

}




