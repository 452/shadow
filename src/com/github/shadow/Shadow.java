package com.github.shadow;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.shadow.events.VoiceActivityDetectedEvent;
import com.github.shadow.events.VoiceActivityListeningEvent;
import com.github.shadow.processing.Audio;

import de.greenrobot.event.EventBus;

public class Shadow extends Activity implements View.OnClickListener {


    private EventBus eventBus = EventBus.getDefault();
    private TextView txtViewStatus;
    private Button btnStartListening;
    private Button btnStopListening;
    private Audio audio = new Audio();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        txtViewStatus = (TextView) findViewById(R.id.txtViewStatus);
        btnStartListening = (Button) findViewById(R.id.btnStartListening);
        btnStartListening.setOnClickListener(this);
        btnStopListening = (Button) findViewById(R.id.btnStopListening);
        btnStopListening.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == btnStartListening) {
            audio.start();
            txtViewStatus.setText("Status: listening");
        } else if (v == btnStopListening) {
            txtViewStatus.setText("Status: listening ended");
            audio.stop();
        }
    }

    public void onEvent(VoiceActivityDetectedEvent event) {
        txtViewStatus.setText("Status: Voice Activity Detected");
    }

    public void onEvent(VoiceActivityListeningEvent event) {
        txtViewStatus.setText("Status: Voice Activity Listening");
    }

    @Override
    protected void onStart() {
        eventBus.register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

}