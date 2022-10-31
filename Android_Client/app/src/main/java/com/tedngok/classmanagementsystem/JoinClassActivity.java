package com.tedngok.classmanagementsystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class JoinClassActivity extends AppCompatActivity implements SearchStudentFragment.OnFragmentInteractionListener {


    private FragmentManager _fm;
    private FragmentTransaction _ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        _fm = getSupportFragmentManager();
        _ft = _fm.beginTransaction();
        SearchStudentFragment searchStudentFragment = new SearchStudentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.jsonClassID),getIntent().getStringExtra(getString(R.string.jsonClassID)));
        searchStudentFragment.setArguments(bundle);
        _ft.replace(R.id.content_main, searchStudentFragment);
        _ft.commit();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
