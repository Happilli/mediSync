package com.bca.medisync.data.remote;

import android.content.Context;
import android.content.Intent;
import com.bca.medisync.MainActivity;
import com.bca.medisync.data.local.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
  public static final String BASE_URL = "http://192.168.217.1:8000/";
  private static Retrofit retrofit;
  private static SessionManager sessionManager;
  private static Context context;

  public static void init(Context context) {
    ApiClient.context = context.getApplicationContext();
    sessionManager = new SessionManager(context);
  }

  public static Retrofit getRetrofit() {
    if (retrofit == null) {
      HttpLoggingInterceptor loggin = new HttpLoggingInterceptor();
      loggin.setLevel(HttpLoggingInterceptor.Level.BODY);

      OkHttpClient client =
              new OkHttpClient.Builder()
                      .addInterceptor(new AuthInterceptor())
                      .addInterceptor(loggin)
                      .connectTimeout(30, TimeUnit.SECONDS)
                      .readTimeout(30, TimeUnit.SECONDS)
                      .build();
      retrofit =
              new Retrofit.Builder()
                      .baseUrl(BASE_URL)
                      .client(client)
                      .addConverterFactory(GsonConverterFactory.create())
                      .build();
    }
    return retrofit;
  }

  private static class AuthInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

      Request original = chain.request();

      String token = sessionManager.getToken();

      android.util.Log.d("API", "URL = " + original.url());
      android.util.Log.d("API", "TOKEN = " + token);

      Request.Builder builder = original.newBuilder();

      if (token != null && !token.isEmpty()) {
        builder.header("Authorization", "Bearer " + token);
      }

      Request request = builder.build();

      android.util.Log.d("API", "Authorization = " + request.header("Authorization"));

      Response response = chain.proceed(request);

      if (response.code() == 401 && !request.url().encodedPath().contains("/auth/login")) {
        synchronized (ApiClient.class) {
          if (sessionManager.getToken() != null) {
            sessionManager.clearSession();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
          }
        }
      }

      return response;
    }
  }
}