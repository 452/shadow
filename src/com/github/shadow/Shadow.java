package com.github.shadow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.shadow.config.EventBusConfig;
import com.github.shadow.events.MessageEvent;
import com.github.shadow.events.VoiceActivityDetectedEvent;
import com.github.shadow.events.VoiceActivityListeningEvent;
import com.github.shadow.services.VoiceActivityDetectorService;

import de.greenrobot.event.EventBus;

public class Shadow extends Activity implements View.OnClickListener {

    private EventBus eventBus = EventBusConfig.getInstance();
    private TextView txtViewStatus;
    private Button btnStartListening;
    private Button btnStopListening;
    private SeekBar seekBarVoiceActivityLevel;
    private SeekBar seekBarTime;
    private Intent voiceActivityDetectorService;
    private TextView txtViewVoiceActivityLevel;
    private TextView txtViewTime;

    private int delayTime = 1789;
    private int voiceActivityLevel = 379;

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar.getId() == R.id.seekBarVoiceActivityLevel) {
                txtViewVoiceActivityLevel.setText(getString(R.string.VoiceActivityLevel) + progress);
                voiceActivityLevel = progress;
            } else {
                txtViewTime.setText(getString(R.string.Time) + progress);
                delayTime = progress;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.setParameters("noise_suppression=auto");
        initUI();
        voiceActivityDetectorService = new Intent(this, VoiceActivityDetectorService.class);
    }

    private void initUI() {
        txtViewStatus = (TextView) findViewById(R.id.txtViewStatus);
        txtViewVoiceActivityLevel = (TextView) findViewById(R.id.textViewVoiceActivityLevel);
        txtViewTime = (TextView) findViewById(R.id.textViewTime);
        btnStartListening = (Button) findViewById(R.id.btnStartListening);
        btnStartListening.setOnClickListener(this);
        btnStopListening = (Button) findViewById(R.id.btnStopListening);
        btnStopListening.setOnClickListener(this);
        seekBarVoiceActivityLevel = (SeekBar) findViewById(R.id.seekBarVoiceActivityLevel);
        seekBarTime = (SeekBar) findViewById(R.id.seekBarTime);
        btnStopListening.setEnabled(false);
        seekBarVoiceActivityLevel.setOnSeekBarChangeListener(onSeekBarChangeListener);
        seekBarTime.setOnSeekBarChangeListener(onSeekBarChangeListener);
        txtViewTime.setText(getString(R.string.Time) + delayTime);
        txtViewVoiceActivityLevel.setText(getString(R.string.VoiceActivityLevel) + voiceActivityLevel);
    }

    public void onClick(View v) {
        if (v == btnStartListening) {
            voiceActivityDetectorService.setAction("start");
            voiceActivityDetectorService.putExtra("delayTime", delayTime);
            voiceActivityDetectorService.putExtra("voiceActivityLevel", voiceActivityLevel);
            startService(voiceActivityDetectorService);
            txtViewStatus.setText("Status: listening");
            btnStartListening.setEnabled(false);
            seekBarVoiceActivityLevel.setEnabled(false);
            seekBarTime.setEnabled(false);
            btnStopListening.setEnabled(true);
        } else if (v == btnStopListening) {
            txtViewStatus.setText("Status: listening ended");
            voiceActivityDetectorService.setAction("stop");
            startService(voiceActivityDetectorService);
            btnStartListening.setEnabled(true);
            seekBarVoiceActivityLevel.setEnabled(true);
            seekBarTime.setEnabled(true);
            btnStopListening.setEnabled(false);
        }
    }

    public void onEventMainThread(MessageEvent event) {
        setStatusTest(event.getData());
    }

    private void setStatusTest(String data) {
        txtViewStatus.setText("Status: " + data);
    }

    public void onEventMainThread(VoiceActivityDetectedEvent event) {
        setStatusTest("Voice Activity Detected " + event.getLevel());
    }

    public void onEventMainThread(VoiceActivityListeningEvent event) {
        setStatusTest("Voice Activity Listening");
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventBus.register(this, 1);
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