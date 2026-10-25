package io.climintv.home.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.climintv.home.model.MediaItem;

public class TmdbRepository {
    private static final String TAG = "TmdbRepository";
    public static final String DEFAULT_API_KEY = "bd7277a9b9e5c6d03d883ee465f9fed5";
    private static final String PREF_CACHE_KEY = "tmdb_recommendations_cache";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface TmdbCallback {
        void onRecommendationsLoaded(List<MediaItem> items);
    }

    private static final Map<Integer, ProviderInfo> PROVIDER_MAP = new HashMap<>();

    static {
        PROVIDER_MAP.put(8, new ProviderInfo("Netflix", "com.netflix.ninja"));
        PROVIDER_MAP.put(9, new ProviderInfo("Prime Video", "com.amazon.amazonvideo.livingroom"));
        PROVIDER_MAP.put(119, new ProviderInfo("Prime Video", "com.amazon.amazonvideo.livingroom"));
        PROVIDER_MAP.put(337, new ProviderInfo("Disney+", "com.disney.disneyplus"));
        PROVIDER_MAP.put(192, new ProviderInfo("YouTube", "com.google.android.youtube.tv"));
        PROVIDER_MAP.put(2, new ProviderInfo("Apple TV", "com.apple.atve.androidtv.appletv"));
        PROVIDER_MAP.put(122, new ProviderInfo("Hotstar", "in.startv.hotstar"));
        PROVIDER_MAP.put(384, new ProviderInfo("Max", "com.wbd.stream"));
    }

    public static class ProviderInfo {
        public final String name;
        public final String packageName;

        public ProviderInfo(String name, String packageName) {
            this.name = name;
            this.packageName = packageName;
        }
    }

    public static void fetchRecommendations(Context context, TmdbCallback callback) {
        AppPreferences prefs = new AppPreferences(context);

        // 1. Return cached items immediately for instant UI
        String cachedJson = prefs.getWeatherCache(); // or separate pref
        List<MediaItem> cached = getCachedItems(context);
        if (!cached.isEmpty()) {
            callback.onRecommendationsLoaded(cached);
        } else {
            callback.onRecommendationsLoaded(getOfflineFallbackItems());
        }

        // 2. Fetch fresh recommendations in background
        executor.execute(() -> {
            try {
                String apiKey = DEFAULT_API_KEY;
                String urlStr = "https://api.themoviedb.org/3/trending/all/day?api_key=" + apiKey;
                String response = httpGet(urlStr);

                if (response == null || response.isEmpty()) {
                    return;
                }

                JSONObject root = new JSONObject(response);
                JSONArray results = root.optJSONArray("results");
                if (results == null || results.length() == 0) return;

                PackageManager pm = context.getPackageManager();
                List<MediaItem> freshItems = new ArrayList<>();

                for (int i = 0; i < results.length() && freshItems.size() < 12; i++) {
                    JSONObject item = results.getJSONObject(i);
                    String id = String.valueOf(item.optInt("id"));
                    String title = item.optString("title", item.optString("name", "Untitled"));
                    String overview = item.optString("overview", "");
                    String backdropPath = item.optString("backdrop_path", "");
                    String posterPath = item.optString("poster_path", "");
                    double vote = item.optDouble("vote_average", 7.5);
                    String release = item.optString("release_date", item.optString("first_air_date", "2026"));
                    String year = release.length() >= 4 ? release.substring(0, 4) : "2026";

                    String backdropUrl = backdropPath.isEmpty() ? "" : "https://image.tmdb.org/t/p/w780" + backdropPath;
                    String posterUrl = posterPath.isEmpty() ? "" : "https://image.tmdb.org/t/p/w500" + posterPath;

                    // Mixed layout: alternate wide (16:9) and square cards (Section 9)
                    boolean isWide = (i % 2 == 0);

                    // Assign streaming provider with smart priority
                    String providerName = "YouTube";
                    String providerPkg = "com.google.android.youtube.tv";

                    if (isInstalled(pm, "com.netflix.ninja") && (i % 3 == 0)) {
                        providerName = "Netflix";
                        providerPkg = "com.netflix.ninja";
                    } else if (isInstalled(pm, "com.amazon.amazonvideo.livingroom") && (i % 3 == 1)) {
                        providerName = "Prime Video";
                        providerPkg = "com.amazon.amazonvideo.livingroom";
                    } else if (isInstalled(pm, "com.disney.disneyplus") && (i % 3 == 2)) {
                        providerName = "Disney+";
                        providerPkg = "com.disney.disneyplus";
                    }

                    String deepLink = "https://www.youtube.com/results?search_query=" + title.replace(" ", "+");

                    freshItems.add(new MediaItem(id, title, overview, backdropUrl, posterUrl,
                            vote, year, providerName, providerPkg, deepLink, isWide));
                }

                saveCachedItems(context, response);

                mainHandler.post(() -> callback.onRecommendationsLoaded(freshItems));
            } catch (Exception e) {
                Log.w(TAG, "TMDB fetch fallback: " + e.getMessage());
            }
        });
    }

    private static boolean isInstalled(PackageManager pm, String pkg) {
        try {
            pm.getPackageInfo(pkg, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String httpGet(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setRequestProperty("User-Agent", "CliminTV/1.0");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return sb.toString();
            }
        } catch (Exception ignored) {
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    private static void saveCachedItems(Context context, String json) {
        context.getSharedPreferences("tmdb_cache", Context.MODE_PRIVATE)
                .edit().putString(PREF_CACHE_KEY, json).apply();
    }

    private static List<MediaItem> getCachedItems(Context context) {
        String json = context.getSharedPreferences("tmdb_cache", Context.MODE_PRIVATE)
                .getString(PREF_CACHE_KEY, "");
        if (json.isEmpty()) return new ArrayList<>();

        List<MediaItem> items = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);
            JSONArray results = root.optJSONArray("results");
            if (results != null) {
                for (int i = 0; i < results.length() && items.size() < 12; i++) {
                    JSONObject item = results.getJSONObject(i);
                    String id = String.valueOf(item.optInt("id"));
                    String title = item.optString("title", item.optString("name", ""));
                    String overview = item.optString("overview", "");
                    String backdropPath = item.optString("backdrop_path", "");
                    String posterPath = item.optString("poster_path", "");
                    double vote = item.optDouble("vote_average", 7.5);
                    String release = item.optString("release_date", item.optString("first_air_date", "2026"));
                    String year = release.length() >= 4 ? release.substring(0, 4) : "2026";

                    String backdropUrl = backdropPath.isEmpty() ? "" : "https://image.tmdb.org/t/p/w780" + backdropPath;
                    String posterUrl = posterPath.isEmpty() ? "" : "https://image.tmdb.org/t/p/w500" + posterPath;
                    boolean isWide = (i % 2 == 0);

                    items.add(new MediaItem(id, title, overview, backdropUrl, posterUrl,
                            vote, year, "YouTube", "com.google.android.youtube.tv", "", isWide));
                }
            }
        } catch (Exception ignored) {}
        return items;
    }

    public static List<MediaItem> getOfflineFallbackItems() {
        List<MediaItem> list = new ArrayList<>();
        list.add(new MediaItem("1", "Dune: Part Two", "Paul Atreides unites with Chani and the Fremen.",
                "https://image.tmdb.org/t/p/w780/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg",
                8.3, "2024", "YouTube", "com.google.android.youtube.tv", "", true));

        list.add(new MediaItem("2", "Interstellar", "A team of explorers travel through a wormhole in space.",
                "https://image.tmdb.org/t/p/w780/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                8.6, "2014", "YouTube", "com.google.android.youtube.tv", "", false));

        list.add(new MediaItem("3", "Spider-Man: Across the Spider-Verse", "Miles Morales catapults across the Multiverse.",
                "https://image.tmdb.org/t/p/w780/4HodYYKEIsGOdinkGi2Ucz6X9i0.jpg",
                "https://image.tmdb.org/t/p/w500/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
                8.4, "2023", "YouTube", "com.google.android.youtube.tv", "", true));

        list.add(new MediaItem("4", "Oppenheimer", "The story of American scientist J. Robert Oppenheimer.",
                "https://image.tmdb.org/t/p/w780/nb3xI8XI3Es4vm9n2BT8nvRIftW.jpg",
                "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                8.1, "2023", "YouTube", "com.google.android.youtube.tv", "", false));

        list.add(new MediaItem("5", "The Batman", "In his second year of fighting crime, Batman uncovers corruption.",
                "https://image.tmdb.org/t/p/w780/b0PlSFdDwbyK0cf5RxwDpaxtQvQ.jpg",
                "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg",
                7.7, "2022", "YouTube", "com.google.android.youtube.tv", "", true));

        return list;
    }
}
