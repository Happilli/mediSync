package com.bca.medisync.data.remote;

import android.content.Context;
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
  public static final String BASE_URL = "http://10.0.2.2:8000/";

  private static Retrofit retrofit;
  private static SessionManager sessionManager;

  public static void init(Context context) {
    sessionManager = new SessionManager(context);
  }

  public static Retrofit getRetrofit() {
    if (retrofit == null) {
      HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
      logging.setLevel(HttpLoggingInterceptor.Level.BODY);

      OkHttpClient client =
          new OkHttpClient.Builder()
              .addInterceptor(new AuthInterceptor())
              .addInterceptor(logging)
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
      String token = sessionManager != null ? sessionManager.getToken() : null;
      if (token == null || token.isEmpty()) {
        return chain.proceed(original);
      }
      token = token.trim().replace("\"", "");
      Request authorized =
          original
              .newBuilder()
              .header("Authorization", "Bearer " + token)
              .header("Accept", "application/json")
              .build();
      return chain.proceed(authorized);
    }
  }
}
