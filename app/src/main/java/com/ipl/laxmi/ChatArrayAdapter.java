package com.ipl.laxmi;

/**
 * Created by Santhosh on 06/11/2017.
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.gotev.speech.ui.SpeechProgressView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private SpeechProgressView speechProgressView;
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private Context context;

    public final String ACTION_REQUIRED_THINGS = "necessarythings";
    public final String ACTION_FUEL_LEVEL = "fuellevel";
    public final String ACTION_JOUNEYS = "journeys";

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View row, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        JSONArray data = null;
        String action = "";
        if(chatMessageObj.data!=null) {
            try {
                action = chatMessageObj.data.getString("action");
                data = chatMessageObj.data.getJSONArray("Data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.rightSide) {
            row = inflater.inflate(R.layout.right, parent, false);
        } else {
            row = inflater.inflate(R.layout.left, parent, false);
            //speechProgressView = (SpeechProgressView) row.findViewById(R.id.progress);
        }
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(chatMessageObj.message);

        if(action.equals(ACTION_JOUNEYS)) {

        }
        else if(action.equals(ACTION_FUEL_LEVEL)) {

        } else if(action.equals(ACTION_JOUNEYS)) {

        }
        /*if(speechProgressView!=null) {
            if (TextUtils.isEmpty(chatMessageObj.message)) {
                speechProgressView.setVisibility(View.VISIBLE);
                speechProgressView.play();
                chatText.setVisibility(View.GONE);
            } else {
                speechProgressView.stop();
                speechProgressView.setVisibility(View.GONE);
                chatText.setVisibility(View.VISIBLE);
            }
        } else {
            chatText.setVisibility(View.VISIBLE);
        }*/
        return row;
    }
}