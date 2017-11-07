package com.ipl.laxmi;

/**
 * Created by z021722 on 06-11-2017.
 */

public interface ApiResponseListener {
    void  onApiResponse(String response);
    void  onApiErrorResponse();
    void  onBeforeRequest(String query);
}
