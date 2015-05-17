package com.github.shadow.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.github.shadow.config.EventBusConfig;
import com.github.shadow.events.VoiceActivityDetectedEvent;
import com.github.shadow.processing.AudioProcessor;
import com.github.shadow.processing.processors.AudioSimpleAudioRecord;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class VoiceActivityDetectorService extends Service {

    private static final String TAG = VoiceActivityDetectorService.class.getCanonicalName();
    private EventBus eventBus = EventBusConfig.getInstance();
    private VoiceActivityProcessorThread voiceActivityProcessorThread;
    private Timer timer = new Timer();
    private AudioProcessor audioProcessor = new AudioSimpleAudioRecord();
    private final Handler uiHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (audioProcessor != null) {
                            double data = audioProcessor.getVoiceActivityLevel();
                            eventBus.post(new VoiceActivityDetectedEvent(data));
                        }
                    }
                });

            }
        }, 0, 100);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals("start")) {
            voiceActivityProcessorThread = new VoiceActivityProcessorThread();
            audioProcessor.setDelayTime(intent.getIntExtra("delayTime", 1789));
            audioProcessor.setVoiceActivityLevel(intent.getIntExtra("voiceActivityLevel", 379));
            voiceActivityProcessorThread.start();
        }
        if (action.equals("stop")) {
            voiceActivityProcessorThread.stopThis();
        }
        return START_REDELIVER_INTENT;
    }

    private class VoiceActivityProcessorThread extends Thread {

        private volatile boolean isStopped;

        public VoiceActivityProcessorThread() {
        }

        @Override
        public void run() {
            audioProcessor.start();
            while (!isStopped) {
                audioProcessor.doIt();
            }
        }

        public void stopThis() {
            isStopped = true;
            audioProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        if (voiceActivityProcessorThread != null) {
            voiceActivityProcessorThread.stopThis();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}