package com.tedngok.classmanagementsystem;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import general.Common;
import general.WebSocketIO;

public class SearchAttachmentActivity extends AppCompatActivity implements SearchAttachmentFragment.OnFragmentInteractionListener {

    private Common _common;
    private WebSocketIO _websocket;
    private FragmentManager _fm;
    private FragmentTransaction _ft;
    private SearchAttachmentFragment saf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.activity_content,null);
        AppBarLayout app_bar = (AppBarLayout) v.findViewById(R.id.app_bar);
        app_bar.setVisibility(View.GONE);
        setContentView(v);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        _common = new Common(this);
        _fm = getSupportFragmentManager();
        _ft = _fm.beginTransaction();
        saf = new SearchAttachmentFragment();

        saf.setArguments(getIntent().getExtras());

        _ft.replace(R.id.content_main, saf);
        _ft.commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        saf.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
       return _common.onSupportNavigateUp();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
