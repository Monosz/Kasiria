package com.example.kasiria.interfaces;

import com.example.kasiria.data.DocumenteroResultData;
import com.example.kasiria.data.DocumenteroPostData;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DocumenteroAPI {

    @POST("api")
    Call<DocumenteroResultData> postJson(@Body RequestBody json);

}
