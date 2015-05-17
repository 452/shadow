package com.github.shadow.processing.processors;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.github.shadow.processing.AudioProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class AudioSimpleAudioRecord implements AudioProcessor {

    private static final int RECORDER_BPP = 16;
    private static int RECORDER_SAMPLE_RATE = 8000;
    private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private File audioFile;

    private boolean isVoiceActivityDetected = false;
    private boolean isRecording = false;
    private List<byte[]> list = new LinkedList<>();

    private short[] buffer;
    private int level;
    private long timeDelayStart;

    // Get the minimum buffer size required for the successful creation of an AudioRecord object.
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING
    );
    // Initialize Audio Recorder.
    private AudioRecord audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
            RECORDER_SAMPLE_RATE,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING,
            bufferSizeInBytes
    );

    private int bufferSizeInShorts = (bufferSizeInBytes / 2);
    private int voiceActivityLevel = 379;
    private int delayTime = 1789;

    public AudioSimpleAudioRecord() {
        init();
    }

    private void init() {
        // Save audio to file.
        String filepath = Environment.getExternalStorageDirectory().getPath();
        audioFile = new File(filepath, "shadow");
        if (!audioFile.exists())
            audioFile.mkdirs();
    }

    private void onRecording() {
        list.add(shortToByte(buffer, buffer.length));
    }

    private void onSave() {
        int size = list.size() * bufferSizeInBytes;
        ByteBuffer out = ByteBuffer.allocate(size * 2);
        for (byte[] b : list) {
            out.put(b);
        }
        list.clear();
        saveAudio(out.array());
    }

    public void doIt() {
        buffer = new short[bufferSizeInShorts];
        int bufferReadResult = audioRecorder.read(buffer, 0, bufferSizeInShorts);
        if (bufferReadResult != AudioRecord.ERROR_BAD_VALUE && AudioRecord.ERROR_INVALID_OPERATION != bufferReadResult) {
            int max = 0;
            for (short s : buffer) {
                if (Math.abs(s) > max) {
                    max = Math.abs(s);
                }
            }
            level = max;
            if (level > voiceActivityLevel) {
                isVoiceActivityDetected = true;
                isRecording = true;
                timeDelayStart = System.currentTimeMillis();
            } if ((timeDelayStart + delayTime) < System.currentTimeMillis() && isVoiceActivityDetected) {
                onSave();
                isVoiceActivityDetected = false;
                isRecording = false;
            }
            if (isRecording) {
                onRecording();
            }
        }
    }

    private void saveAudio(byte[] finalBuffer) {
        String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String fn = audioFile.getAbsolutePath() + "/" + fileName + ".wav";
        FileOutputStream out;
        try {
            out = new FileOutputStream(fn);
            try {
                out.write(makeWawFileStructure(finalBuffer.length/2, finalBuffer));
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private byte[] makeWawFileStructure(int totalReadBytes, byte[] totalByteBuffer) {
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLE_RATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / 8;
        totalAudioLen = totalReadBytes;
        totalDataLen = totalAudioLen + 36;
        byte finalBuffer[] = new byte[totalReadBytes + 44];

        finalBuffer[0] = 'R';  // RIFF/WAVE header
        finalBuffer[1] = 'I';
        finalBuffer[2] = 'F';
        finalBuffer[3] = 'F';
        finalBuffer[4] = (byte) (totalDataLen & 0xff);
        finalBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
        finalBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
        finalBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
        finalBuffer[8] = 'W';
        finalBuffer[9] = 'A';
        finalBuffer[10] = 'V';
        finalBuffer[11] = 'E';
        finalBuffer[12] = 'f';  // 'fmt ' chunk
        finalBuffer[13] = 'm';
        finalBuffer[14] = 't';
        finalBuffer[15] = ' ';
        finalBuffer[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        finalBuffer[17] = 0;
        finalBuffer[18] = 0;
        finalBuffer[19] = 0;
        finalBuffer[20] = 1;  // format = 1
        finalBuffer[21] = 0;
        finalBuffer[22] = (byte) channels;
        finalBuffer[23] = 0;
        finalBuffer[24] = (byte) (longSampleRate & 0xff);
        finalBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
        finalBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
        finalBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
        finalBuffer[28] = (byte) (byteRate & 0xff);
        finalBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
        finalBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
        finalBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
        finalBuffer[32] = (byte) (2 * 16 / 8);  // block align
        finalBuffer[33] = 0;
        finalBuffer[34] = RECORDER_BPP;  // bits per sample
        finalBuffer[35] = 0;
        finalBuffer[36] = 'd';
        finalBuffer[37] = 'a';
        finalBuffer[38] = 't';
        finalBuffer[39] = 'a';
        finalBuffer[40] = (byte) (totalAudioLen & 0xff);
        finalBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        finalBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        finalBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        for (int i = 0; i < totalReadBytes; ++i)
            finalBuffer[44 + i] = totalByteBuffer[i];
        return finalBuffer;
    }

    private byte[] shortToByte(short[] input, int elements) {
        int short_index, byte_index;
        int iterations = elements; //input.length;
        byte[] buffer = new byte[iterations * 2];
        short_index = byte_index = 0;
        for (/*NOP*/; short_index != iterations; /*NOP*/) {
            buffer[byte_index] = (byte) (input[short_index] & 0x00FF);
            buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);
            ++short_index;
            byte_index += 2;
        }
        return buffer;
    }

    public void start() {
        audioRecorder.startRecording();
    }

    public void stop() {
        audioRecorder.stop();
    }

    public int getVoiceActivityLevel() {
        return level;
    }

    @Override
    public void setVoiceActivityLevel(int voiceActivityLevel) {
        this.voiceActivityLevel = voiceActivityLevel;
    }

    @Override
    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

}