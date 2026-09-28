package thanhdnh.ueh.edu.article_app;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Downloader {

  public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess(File file);
    void onFailure(Exception e);
  }

  public static void downloadFileWithProgress(String url, File cacheDir, ProgressBar progressBar, DownloadListener listener) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).build();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    if (progressBar != null) {
      mainHandler.post(() -> {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
      });
    }

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        mainHandler.post(() -> {
          if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
          }
          if (listener != null) {
            listener.onFailure(e);
          }
        });
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (!response.isSuccessful() || response.body() == null) {
          mainHandler.post(() -> {
            if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
            if (listener != null) {
              listener.onFailure(new IOException("HTTP error code: " + response.code()));
            }
          });
          return;
        }

        long totalBytes = response.body().contentLength();
        InputStream inputStream = response.body().byteStream();
        String contentType = response.header("Content-Type", "");
        String extension = getExtensionFromMimeType(contentType);

        File outputFile;
        try {
          outputFile = File.createTempFile("downloaded_user_data", extension, cacheDir);
        } catch (IOException e) {
          mainHandler.post(() -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (listener != null) listener.onFailure(e);
          });
          return;
        }

        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
          byte[] buffer = new byte[2048];
          long downloadedBytes = 0;
          int bytesRead;

          while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedBytes += bytesRead;

            if (totalBytes > 0) {
              final int progress = (int) ((downloadedBytes * 100) / totalBytes);
              mainHandler.post(() -> {
                if (progressBar != null) {
                  progressBar.setProgress(progress);
                }
                if (listener != null) {
                  listener.onProgress(progress);
                }
              });
            }
          }
          outputStream.flush();

          mainHandler.post(() -> {
            if (progressBar != null) {
              progressBar.setProgress(100);
              progressBar.setVisibility(View.GONE);
            }
            if (listener != null) {
              listener.onSuccess(outputFile);
            }
          });
        } catch (Exception e) {
          mainHandler.post(() -> {
            if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
            if (listener != null) {
              listener.onFailure(e);
            }
          });
        }
      }
    });
  }

  private static String getExtensionFromMimeType(String mimeType) {
    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("image/jpeg", ".jpg");
    mimeMap.put("image/png", ".png");
    mimeMap.put("application/json", ".json");
    mimeMap.put("text/plain", ".json");
    return mimeMap.getOrDefault(mimeType, ".json");
  }
}
