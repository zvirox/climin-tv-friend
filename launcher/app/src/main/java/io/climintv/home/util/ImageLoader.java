package io.climintv.home.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static final int CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024) / 8; // 1/8th of memory
    private static final LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void load(Context context, String urlString, ImageView targetView, int placeholderRes) {
        if (urlString == null || urlString.isEmpty()) {
            if (placeholderRes != 0) targetView.setImageResource(placeholderRes);
            return;
        }

        targetView.setTag(urlString);

        // 1. Check Memory Cache
        Bitmap cached = memoryCache.get(urlString);
        if (cached != null) {
            targetView.setImageBitmap(cached);
            return;
        }

        if (placeholderRes != 0) {
            targetView.setImageResource(placeholderRes);
        }

        // 2. Fetch in background (Check Disk Cache, then Download)
        executor.execute(() -> {
            try {
                Bitmap bitmap = loadFromDisk(context, urlString);
                if (bitmap == null) {
                    bitmap = downloadBitmap(urlString);
                    if (bitmap != null) {
                        saveToDisk(context, urlString, bitmap);
                    }
                }

                if (bitmap != null) {
                    memoryCache.put(urlString, bitmap);
                    Bitmap finalBitmap = bitmap;
                    mainHandler.post(() -> {
                        if (urlString.equals(targetView.getTag())) {
                            targetView.setAlpha(0f);
                            targetView.setImageBitmap(finalBitmap);
                            targetView.animate().alpha(1f).setDuration(220).start();
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, "Image load error: " + e.getMessage());
            }
        });
    }

    private static Bitmap downloadBitmap(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "CliminTV/1.0");

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                opts.inDither = true;
                Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                is.close();
                return bmp;
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private static File getDiskFile(Context context, String urlString) {
        File cacheDir = new File(context.getCacheDir(), "tmdb_images");
        if (!cacheDir.exists()) cacheDir.mkdirs();
        return new File(cacheDir, md5(urlString) + ".img");
    }

    private static Bitmap loadFromDisk(Context context, String urlString) {
        try {
            File file = getDiskFile(context, urlString);
            if (file.exists()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                opts.inDither = true;
                return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static void saveToDisk(Context context, String urlString, Bitmap bitmap) {
        try {
            File file = getDiskFile(context, urlString);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.close();
        } catch (Exception ignored) {}
    }

    private static String md5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(s.hashCode());
        }
    }
}
