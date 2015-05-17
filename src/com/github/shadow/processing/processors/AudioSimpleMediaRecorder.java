package com.github.shadow.processing.processors;


import android.media.MediaRecorder;

import com.github.shadow.processing.AudioProcessor;

import java.io.IOException;

public class AudioSimpleMediaRecorder implements AudioProcessor {

    private MediaRecorder recorder;
    private int maxAmplitude;

    public void stop() {
        recorder.stop();
        recorder.release();
    }

    public void start() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile("/dev/null");
        try {
            recorder.prepare();
        } catch (IOException e) {
        }
        recorder.start();
    }

    public int getVoiceActivityLevel() {
        if (recorder == null) {
            return 0;
        } else {
            return maxAmplitude = recorder.getMaxAmplitude();
        }
    }

    @Override
    public void setVoiceActivityLevel(int delayTime) {

    }

    @Override
    public void setDelayTime(int voiceActivityLevel) {

    }

    public double getdbLevel() {
        int ratio = maxAmplitude;
        int db = 0;
        if (ratio > 1)
            db = (int) (20 * Math.log10(ratio));
        return Math.log10(ratio);
    }


    public void doIt() {
    }

}