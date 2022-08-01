package com.example.nativeandroidapp.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAbs0I5QU:APA91bHborHCExj2WI1l2digcGBlQTtCsvsuMXgro0DU8iqI1zSlNLRpE2K3316Jq49dJNOUtXxjoPlG0ja75qwzVygNELnx7023YREmBz67Ek4oi5PjR1FPAbJdfIbcSyiqitOJ_30J"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
