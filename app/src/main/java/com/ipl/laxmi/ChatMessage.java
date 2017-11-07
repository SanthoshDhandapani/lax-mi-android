package com.ipl.laxmi;

import org.json.JSONObject;

/**
 * Created by Santhosh on 06/11/2017.
 */

public class ChatMessage {
    public boolean rightSide;
    public String message;
    public JSONObject data;

    public ChatMessage(boolean rightSide, String message) {
        super();
        this.rightSide = rightSide;
        this.message = message;
    }

    public ChatMessage(boolean rightSide, String message, JSONObject data) {
        super();
        this.rightSide = rightSide;
        this.message = message;
        this.data = data;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
}