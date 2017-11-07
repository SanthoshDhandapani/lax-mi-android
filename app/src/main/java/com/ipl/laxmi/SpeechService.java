package com.ipl.laxmi;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

/**
 * Main SDK class for working with API.AI service.
 */
public class SpeechService  {
//extends IntentService

    private static final String TAG = SpeechService.class.getName();
    ApiResponseListener apiResponseListener;
    OnSpeechListener speechListener;
    WeakReference<MainActivity> activityWeakReference;
    //http://172.32.18.114:8081
    private static final String BASE_URL = "http://172.32.11.17:8081";
    /*public SpeechService() {
        // Used to name the worker thread
        // Important only for debugging
        super(TAG);
    }*/

    SpeechService(WeakReference<MainActivity> weakRef, ApiResponseListener apiResponseListener, OnSpeechListener onSpeechListener) {
        this.activityWeakReference = weakRef;
        this.apiResponseListener = apiResponseListener;
        this.speechListener = onSpeechListener;
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        SpeechService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SpeechService.this;
        }
    }

    /*@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Invoked on the worker thread
        // Do some work in background without affecting the UI thread

    }*/


    /**
     * Starts listening process
     */
    public void startListening() {
        try {
            // you must have android.permission.RECORD_AUDIO granted at this point
            Speech.getInstance().startListening(new SpeechDelegate() {
                @Override
                public void onStartOfSpeech() {
                    Log.i("speech", "speech recognition is now active");
                    if(speechListener!=null) {
                        speechListener.onSpeechStart();
                    }
                }

                @Override
                public void onSpeechRmsChanged(float value) {
                    Log.d("speech", "rms is now: " + value);
                }

                @Override
                public void onSpeechPartialResults(List<String> results) {
                    StringBuilder str = new StringBuilder();
                    for (String res : results) {
                        str.append(res).append(" ");
                    }

                    Log.i("speech", "partial result: " + str.toString().trim());
                }

                @Override
                public void onSpeechResult(String result) {
                    Log.i("speech", "result: " + result);
                    onSpeechEnd(result);
                    //startListening();
                }
            });
        } catch (Exception exc) {
            Log.e("speech", "Speech recognition is not available on this device!");
            if(speechListener!=null)
                speechListener.onSpeechError(exc);
            // You can prompt the user if he wants to install Google App to have
            // speech recognition, and then you can simply call:
            //
            // SpeechUtil.redirectUserToGoogleAppOnPlayStore(this);
            //
            // to redirect the user to the Google App page on Play Store
        }
    };

    /**
     * On end of the speech
     */
    public  void onSpeechEnd(String result){
        if(apiResponseListener!=null) {
            apiResponseListener.onBeforeRequest(result);
        }
        speakRequest(result);
    };

    /**
     * Stop listening
     */
    public void stopListening() {
        Speech.getInstance().stopListening();
    };

    public void speakRequest(final String query) {
        RequestQueue queue = Volley.newRequestQueue(activityWeakReference.get());

        // Request a string response from the provided URL.
        StringRequest speakReq = new StringRequest(Request.Method.POST, BASE_URL+"/speak",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if(apiResponseListener!=null) {
                            apiResponseListener.onApiResponse(response);
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null && error.getMessage()!=null) {
                    Log.e("speech api error", error.getMessage());
                    if(apiResponseListener!=null) {
                        apiResponseListener.onApiErrorResponse();
                    }
                }
            }
        }) {
            //adding parameters to the request
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userName", MainActivity.NAME);
                params.put("userSays", query);
                return params;
            }
        };
        queue.add(speakReq);
    }

}
