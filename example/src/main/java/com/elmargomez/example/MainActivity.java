package com.elmargomez.example;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.elmargomez.dominohttp.data.NetworkHeader;
import com.elmargomez.dominohttp.data.RequestManager;
import com.elmargomez.dominohttp.data.WebRequest;
import com.elmargomez.dominohttp.inter.SuccessResponse;

public class MainActivity extends AppCompatActivity implements WebRequest.Header {

    private static final int IMAGE_REQUEST_ID = 1111;
    private RequestManager mRequestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRequestManager = RequestManager.initialize(this, this, savedInstanceState);

        // Assign the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Assign our click listener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mRequestManager.request(MainActivity.this, null)
                        .successListener(IMAGE_REQUEST_ID)
                        .execute();

            }
        });
    }

    @SuccessResponse(id = IMAGE_REQUEST_ID, responseType = SuccessResponse.BITMAP_RESPONSE)
    public void exampleImageDownload() {

    }

    @Override
    public void header(NetworkHeader header) {
        header.url = "http://www.example.com";
        header.setContentType(NetworkHeader.APPLICATION_JSON);
        header.setMethod(NetworkHeader.POST);
    }
}
