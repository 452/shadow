package com.github.shadow.processing;

public interface AudioProcessor {

    void start();
    void stop();
    void doIt();
    int getVoiceActivityLevel();
    void setVoiceActivityLevel(int delayTime);
    void setDelayTime(int voiceActivityLevel);
}
