package com.ipl.laxmi;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import com.mapzen.speakerbox.Speakerbox;

import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;
import net.gotev.speech.ui.SpeechProgressView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import ai.api.AIConfiguration;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.AIServiceException;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

/**
 * Created by Santhosh on 06/11/2017.
 */
public class MainActivity extends AppCompatActivity implements AIListener, OnSpeechListener, ApiResponseListener, View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "ChatActivity";
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 12;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 35;

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private FloatingActionButton sendButton;
    private FloatingActionButton listenButton;
    private AIService aiService;
    private Animation pop_in_anim;
    private Animation pop_out_anim;
    private Speakerbox speakerbox;
    private boolean rightSide = true; //true if you want message on right rightSide
    SpeechService mSpeechService;
    View progress, speechInputView;
    SpeechProgressView speechProgressView;

    public static final String SAN = "san";
    public static final String SIVA = "siva";
    public static final String NAME = SAN;
    public static final String SEQ_ONE = "wishme";
    public static final String SEQ_TWO = "checkfuellevel";
    public static final String SEQ_THREE = "tirepressurelow";

    public String currentSeq = "";

    //boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View screen = findViewById(R.id.screen);
        screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpeechService.startListening();
            }
        });
        screen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(TextUtils.isEmpty(currentSeq)) {
                    currentSeq = SEQ_ONE;
                } else if(currentSeq.equals(SEQ_ONE)) {
                    currentSeq = SEQ_TWO;
                } else {
                    if(NAME.equals(SIVA)) {
                        currentSeq = SEQ_THREE;
                    } else {
                        currentSeq = SEQ_ONE;
                    }
                }
                mSpeechService.speakRequest(currentSeq);
                return true;
            }
        });
        Speech.init(this);
        mSpeechService = new SpeechService(new WeakReference<>(this), this, this);

        speakerbox = new Speakerbox(this.getApplication());
        sendButton = (FloatingActionButton) findViewById(R.id.btn_send);
        listView = (ListView) findViewById(R.id.msgview);
        listenButton = (FloatingActionButton) findViewById(R.id.btn_mic);
        chatText = (EditText) findViewById(R.id.msg);
        progress = findViewById(R.id.progress);
        speechInputView = findViewById(R.id.speech_input_view);
        speechProgressView = (SpeechProgressView) findViewById(R.id.speech_progress);

        pop_in_anim = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        pop_out_anim = AnimationUtils.loadAnimation(this, R.anim.pop_out);
        sendButton.setAnimation(pop_out_anim);
        sendButton.setAnimation(pop_in_anim);
        listenButton.setAnimation(pop_in_anim);
        listenButton.setAnimation(pop_out_anim);
        sendButton.clearAnimation();
        listenButton.clearAnimation();

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);

        listView.setAdapter(chatArrayAdapter);

        // Get INTERNET permission

        while (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {

                showExplanation("Permission Needed", "Rationale", Manifest.permission.INTERNET,
                        MY_PERMISSIONS_REQUEST_INTERNET);

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.

                requestPermission(Manifest.permission.INTERNET,
                        MY_PERMISSIONS_REQUEST_INTERNET);

                // MY_PERMISSIONS_REQUEST_INTERNET is an
                // app-defined int constant. The callback method gets the
                // result of the request (onRequestPermissionsResult).
            }
        }

        /**
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage(chatText.getText().toString());
                }
                return false;
            }
        });
        **/

        chatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 && listenButton.getVisibility() == View.GONE) {
                    sendButton.clearAnimation();
                    sendButton.startAnimation(pop_out_anim);
                    sendButton.setVisibility(View.GONE);
                    sendButton.setEnabled(false);
                    listenButton.clearAnimation();
                    listenButton.setVisibility(View.VISIBLE);
                    listenButton.startAnimation(pop_in_anim);
                    listenButton.setEnabled(true);

                } else if (s.length() > 0 && sendButton.getVisibility() == View.GONE) {
                    listenButton.clearAnimation();
                    listenButton.startAnimation(pop_out_anim);
                    listenButton.setVisibility(View.GONE);
                    listenButton.setEnabled(false);
                    sendButton.clearAnimation();
                    sendButton.setVisibility(View.VISIBLE);
                    sendButton.startAnimation(pop_in_anim);
                    sendButton.setEnabled(true);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(chatText.getText().toString());
                final Handler mHandler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final AIResponse response = aiService.textRequest(aiRequest);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onResult(response);
                                }
                            });
                        } catch (AIServiceException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                //sendChatMessage(chatText.getText().toString());
            }
        });

        listenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO)) {

                        showExplanation("Permission Needed", "Rationale", Manifest.permission.RECORD_AUDIO,
                                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed, we can request the permission.

                        requestPermission(Manifest.permission.RECORD_AUDIO,
                                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                        // MY_PERMISSIONS_REQUEST_RECORD_AUDIO is an
                        // app-defined int constant. The callback method gets the
                        // result of the request (onRequestPermissionsResult).
                    }

                } else {
                    //aiService.startListening();
                    mSpeechService.startListening();
                }
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        final AIConfiguration config = new AIConfiguration("",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
    }

    private boolean sendResponse(String text, JSONObject respObj) {
        chatArrayAdapter.add(new ChatMessage(!rightSide, text, respObj));
        return true;
    }

    private boolean sendChatMessage(String text) {
        if (text.length() == 0)
            return false;
        chatArrayAdapter.add(new ChatMessage(rightSide, text));
        chatText.setText("");
        return true;
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    // whether Permissions were granted
                    aiService.startListening();
                return;
            } case MY_PERMISSIONS_REQUEST_INTERNET: {
            } default: return;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, SpeechService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    *//** Defines callbacks for service binding, passed to bindService() *//*
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeechService.LocalBinder binder = (SpeechService.LocalBinder) service;
            mSpeechService = binder.getService();
            mSpeechService.apiResponseListener = MainActivity.this;
            mSpeechService.speechListener = MainActivity.this;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };*/

    public void onResult(final AIResponse response) { // here process response
        Result result = response.getResult();
        Log.i(TAG, "Action: " + result.getAction());
        // process response object
        sendChatMessage(response.getResult().getResolvedQuery());
        speakerbox.play(result.getFulfillment().getSpeech());
        sendResponse(result.getFulfillment().getSpeech(), null);

        /**

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        // Show results in TextView.
        sendChatMessage("Query:" + result.getResolvedQuery() +
                        "\nAction: " + result.getAction() +
                        "\nParameters: " + parameterString);

        **/
    }

    @Override
    public void onError(AIError error) { // here process error
        sendResponse(error.toString(), null);
    }

    @Override
    public void onAudioLevel(float level) { // callback for sound level visualization

    }

    @Override
    public void onListeningStarted() { // indicate start listening here

    }

    @Override
    public void onListeningCanceled() { // indicate stop listening here

    }

    @Override
    public void onListeningFinished() { // indicate stop listening here

    }

    @Override
    public void onApiResponse(String response) {
        Log.d("response is ", response);
        try {
            JSONObject responseObj = new JSONObject(response);
            String message = responseObj.getString("result");
            JSONObject dataObj = null;
            try {
                dataObj = new JSONObject(message);
                message = dataObj.getString("message");
            } catch (JSONException e) {}
            sendResponse(message,dataObj);
            chatArrayAdapter.notifyDataSetChanged();
            speakerbox.play(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

       // mSpeechService.startListening();
    }

    @Override
    public void onApiErrorResponse() {
      //  mSpeechService.startListening();
    }

    @Override
    public void onBeforeRequest(String query) {
        speechProgressView.stop();
        progress.setVisibility(View.GONE);
        speechInputView.setVisibility(View.VISIBLE);
        if(!TextUtils.isEmpty(query)) {
            sendChatMessage(query);
        }
    }

    @Override
    public void onSpeechStart() {
        speechInputView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        speechProgressView.play();
    }

    @Override
    public void onSpeechError(Exception exc) {

    }

    @Override
    public void onClick(View view) {
        mSpeechService.startListening();
    }

    @Override
    public boolean onLongClick(View view) {
        mSpeechService.speakRequest("Hi");
        return false;
    }
}
