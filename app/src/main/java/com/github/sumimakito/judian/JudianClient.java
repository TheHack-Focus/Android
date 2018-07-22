package com.github.sumimakito.judian;

import android.location.Location;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JudianClient {
    private static OkHttpClient client = new OkHttpClient();

    public static void login(String username, String password, ResultCallback resultCallback) {
        new Thread(() -> {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", password);
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                Request request = new Request.Builder()
                        .url("https://hackshdemowk1.eastasia.cloudapp.azure.com/account/login")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                resultCallback.onResult(response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                resultCallback.onFailed();
            }
        }).start();
    }

    public static void post(double[] latlon, String title, String text, String description, ResultCallback resultCallback) {
        new Thread(() -> {
            try {
                JSONObject jsonLocObject = new JSONObject();
                jsonLocObject.put("lat", latlon[0]);
                jsonLocObject.put("lon", latlon[1]);
                jsonLocObject.put("description", description);
                JSONObject jsonObject = new JSONObject();
                if (title != null) jsonObject.put("title", title);
                if (text != null) jsonObject.put("text", text);
                jsonObject.put("location", jsonLocObject);
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                Request request = new Request.Builder()
                        .url("https://hackshdemowk1.eastasia.cloudapp.azure.com/stream/add")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                resultCallback.onResult(response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                resultCallback.onFailed();
            }
        }).start();
    }

    public static void live(double[] clatlon, int radius, ResultCallback resultCallback) {
        new Thread(() -> {
            try {
                JSONObject jsonLocObject = new JSONObject();
                jsonLocObject.put("lat", clatlon[0]);
                jsonLocObject.put("lon", clatlon[1]);
                jsonLocObject.put("radius", radius);
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonLocObject.toString());
                Request request = new Request.Builder()
                        .url("https://hackshdemowk1.eastasia.cloudapp.azure.com/stream/live")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                resultCallback.onResult(response.body().string());
            } catch (Exception e) {
                e.printStackTrace();
                resultCallback.onFailed();
            }
        }).start();
    }

    public interface ResultCallback {
        void onResult(String result);

        void onFailed();
    }
}
