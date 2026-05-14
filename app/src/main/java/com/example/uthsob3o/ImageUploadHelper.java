package com.example.uthsob3o;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class ImageUploadHelper {

    // ✅ Your Cloudinary details
    private static final String CLOUD_NAME = "dm1edgkoy";
    private static final String UPLOAD_PRESET = "uthsob_upload";
    private static final String UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    public static void uploadImage(Context context, Uri imageUri,
                                   UploadCallback callback) {
        new Thread(() -> {
            try {
                // ✅ This works on ALL Android versions (API 29+)
                byte[] bytes = readBytesFromUri(context, imageUri);

                if (bytes == null) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onFailure("Could not read image file")
                    );
                    return;
                }

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "crop_image.jpg",
                                RequestBody.create(bytes,
                                        MediaType.parse("image/jpeg")))
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .build();

                Request request = new Request.Builder()
                        .url(UPLOAD_URL)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onFailure(e.getMessage())
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response)
                            throws IOException {
                        String responseBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            if (json.has("secure_url")) {
                                String imageUrl = json.getString("secure_url");
                                new Handler(Looper.getMainLooper()).post(() ->
                                        callback.onSuccess(imageUrl)
                                );
                            } else {
                                new Handler(Looper.getMainLooper()).post(() ->
                                        callback.onFailure("Upload failed: "
                                                + responseBody)
                                );
                            }
                        } catch (Exception e) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onFailure("Parse error: "
                                            + e.getMessage())
                            );
                        }
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        callback.onFailure(e.getMessage())
                );
            }
        }).start();
    }

    // ✅ Works on API 29+ — No readAllBytes() needed!
    private static byte[] readBytesFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream =
                    context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192]; // Read in 8KB chunks
            int bytesRead;

            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }

            inputStream.close();
            return buffer.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }
}