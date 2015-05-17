package com.github.shadow.processing.processors;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import de.greenrobot.event.EventBus;

public class Audio {
    private EventBus eventBus = EventBus.getDefault();
    int sampleSize = 8000;
    double fftOutWindowSize = 0;

    void init() {
        int channel_config = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int format = AudioFormat.ENCODING_PCM_16BIT;

        int bufferSize = AudioRecord.getMinBufferSize(sampleSize, channel_config, format);
        AudioRecord audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleSize, channel_config, format, bufferSize);

        int bytesRecorded = 0;


        byte[] audioBuffer = new byte[bufferSize];
        audioInput.startRecording();
        audioInput.read(audioBuffer, 0, bufferSize);

        double[] micBufferData = new double[bufferSize];
        final int bytesPerSample = 2; // As it is 16bit PCM
        final double amplification = 100.0; // choose a number as you like
        for (int index = 0, floatIndex = 0; index < bytesRecorded - bytesPerSample + 1; index += bytesPerSample, floatIndex++) {
            double sample = 0;
            for (int b = 0; b < bytesPerSample; b++) {
                int v = audioBuffer[index + b];
                if (b < bytesPerSample - 1 || bytesPerSample == 1) {
                    v &= 0xFF;
                }
                sample += v << (b * 8);
            }
            double sample32 = amplification * (sample / 32768.0);
            micBufferData[floatIndex] = sample32;
        }
    }

    private double ComputeFrequency(int arrayIndex) {
        return ((1.0 * sampleSize) / (1.0 * fftOutWindowSize)) * arrayIndex;
    }

}