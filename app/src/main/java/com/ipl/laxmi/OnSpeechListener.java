package com.ipl.laxmi;

import net.gotev.speech.SpeechRecognitionNotAvailable;

/**
 * Created by z021722 on 06-11-2017.
 */

public interface OnSpeechListener {
    void onSpeechStart();
    void onSpeechError(Exception exc);
}
