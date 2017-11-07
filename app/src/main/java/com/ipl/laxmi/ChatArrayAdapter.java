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
import android.widget.LinearLayout;
import android.widget.TextView;

import net.gotev.speech.ui.SpeechProgressView;

import java.util.ArrayList;
import java.util.List;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private SpeechProgressView speechProgressView;
    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private Context context;

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
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.rightSide) {
            row = inflater.inflate(R.layout.right, parent, false);
        } else {
            row = inflater.inflate(R.layout.left, parent, false);

            boolean isRemainder = true;
            boolean isFuel = true;
            boolean isJounney = false;

            if(isRemainder) {
                View fuelCard = row.findViewById(R.id.remainder_card_include);
                fuelCard.setVisibility(View.VISIBLE);
            }

            if(isFuel) {
                View fuelCard = row.findViewById(R.id.fuel_card_include);
                fuelCard.setVisibility(View.VISIBLE);
                LinearLayout fuelViewRoot = (LinearLayout) row.findViewById(R.id.fuel_item_root);
                View fuelItem = inflater.inflate(R.layout.fuel_item, null);
                fuelViewRoot.addView(fuelItem);
            }

            if(isJounney) {
                View journeyCard = row.findViewById(R.id.journey_card_include);
                journeyCard.setVisibility(View.VISIBLE);
            }



            //speechProgressView = (SpeechProgressView) row.findViewById(R.id.progress);
        }
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(chatMessageObj.message);





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