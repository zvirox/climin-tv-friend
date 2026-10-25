package io.climintv.home.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.climintv.home.R;

public class WeatherRepository {
    private static final String TAG = "WeatherRepository";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static class HourlySlot {
        public final String timeLabel;
        public final int tempC;
        public final int weatherCode;
        public final int iconRes;

        public HourlySlot(String timeLabel, int tempC, int weatherCode, int iconRes) {
            this.timeLabel = timeLabel;
            this.tempC = tempC;
            this.weatherCode = weatherCode;
            this.iconRes = iconRes;
        }
    }

    public static class WeatherData {
        public final String cityName;
        public final int currentTempC;
        public final int apparentTempC;
        public final int humidity;
        public final int windKmh;
        public final int highC;
        public final int lowC;
        public final String condition;
        public final int iconRes;
        public final List<HourlySlot> hourlySlots;

        public WeatherData(String cityName, int currentTempC, int apparentTempC,
                           int humidity, int windKmh, int highC, int lowC,
                           String condition, int iconRes, List<HourlySlot> hourlySlots) {
            this.cityName = cityName;
            this.currentTempC = currentTempC;
            this.apparentTempC = apparentTempC;
            this.humidity = humidity;
            this.windKmh = windKmh;
            this.highC = highC;
            this.lowC = lowC;
            this.condition = condition;
            this.iconRes = iconRes;
            this.hourlySlots = hourlySlots;
        }
    }

    public interface WeatherCallback {
        void onWeatherLoaded(WeatherData data);
    }

    public static void fetchWeather(Context context, WeatherCallback callback) {
        AppPreferences prefs = new AppPreferences(context);
        String city = prefs.getCity();

        // 1. Immediately return cached data if available for instant UI rendering
        String cachedJson = prefs.getWeatherCache();
        if (!cachedJson.isEmpty()) {
            WeatherData cached = parseCachedJson(cachedJson);
            if (cached != null) {
                callback.onWeatherLoaded(cached);
            }
        } else {
            callback.onWeatherLoaded(getFallbackData(city));
        }

        // 2. Fetch fresh weather in the background
        executor.execute(() -> {
            try {
                // Step A: Geocode city name to lat/lon via Open-Meteo Geocoding
                String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + URLEncoder.encode(city, "UTF-8")
                        + "&count=1&language=en&format=json";

                String geoResponse = httpGet(geoUrl);
                if (geoResponse == null) return;

                JSONObject geoObj = new JSONObject(geoResponse);
                if (!geoObj.has("results")) return;
                JSONArray results = geoObj.getJSONArray("results");
                if (results.length() == 0) return;

                JSONObject loc = results.getJSONObject(0);
                double lat = loc.getDouble("latitude");
                double lon = loc.getDouble("longitude");
                String resolvedName = loc.optString("name", city);

                // Step B: Query Open-Meteo forecast
                String forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat
                        + "&longitude=" + lon
                        + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m"
                        + "&hourly=temperature_2m,weather_code"
                        + "&forecast_days=1";

                String forecastResponse = httpGet(forecastUrl);
                if (forecastResponse == null) return;

                JSONObject forecastObj = new JSONObject(forecastResponse);
                JSONObject current = forecastObj.getJSONObject("current");
                JSONObject hourly = forecastObj.getJSONObject("hourly");

                int currentTemp = (int) Math.round(current.getDouble("temperature_2m"));
                int apparentTemp = (int) Math.round(current.optDouble("apparent_temperature", currentTemp));
                int humidity = (int) Math.round(current.optDouble("relative_humidity_2m", 50));
                int wind = (int) Math.round(current.optDouble("wind_speed_10m", 8));
                int weatherCode = current.optInt("weather_code", 1);

                JSONArray hourlyTemps = hourly.getJSONArray("temperature_2m");
                JSONArray hourlyCodes = hourly.getJSONArray("weather_code");

                int high = currentTemp;
                int low = currentTemp;
                for (int i = 0; i < hourlyTemps.length(); i++) {
                    int t = (int) Math.round(hourlyTemps.getDouble(i));
                    if (t > high) high = t;
                    if (t < low) low = t;
                }

                // Build 6 hourly slots starting from current hour
                Calendar cal = Calendar.getInstance();
                int currentHour = cal.get(Calendar.HOUR_OF_DAY);

                List<HourlySlot> slots = new ArrayList<>();
                for (int i = 0; i < 6; i++) {
                    int targetHour = (currentHour + i) % 24;
                    int temp = (targetHour < hourlyTemps.length()) ? (int) Math.round(hourlyTemps.getDouble(targetHour)) : currentTemp;
                    int code = (targetHour < hourlyCodes.length()) ? hourlyCodes.getInt(targetHour) : weatherCode;

                    String label;
                    if (i == 0) {
                        label = "Now";
                    } else {
                        int h12 = targetHour % 12;
                        if (h12 == 0) h12 = 12;
                        label = h12 + (targetHour >= 12 ? " PM" : " AM");
                    }
                    slots.add(new HourlySlot(label, temp, code, getWeatherIcon(code)));
                }

                String condition = getWeatherCondition(weatherCode);
                int iconRes = getWeatherIcon(weatherCode);

                WeatherData fresh = new WeatherData(resolvedName, currentTemp, apparentTemp,
                        humidity, wind, high, low, condition, iconRes, slots);

                // Save to cache
                prefs.setWeatherCache(forecastResponse);

                mainHandler.post(() -> callback.onWeatherLoaded(fresh));
            } catch (Exception e) {
                Log.w(TAG, "Weather refresh offline fallback: " + e.getMessage());
            }
        });
    }

    private static String httpGet(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "CliminTV-Home/1.0");

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
        } catch (Exception e) {
            Log.d(TAG, "httpGet failed: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static WeatherData getFallbackData(String city) {
        List<HourlySlot> slots = new ArrayList<>();
        slots.add(new HourlySlot("Now", 31, 1, R.drawable.ic_weather_sun));
        slots.add(new HourlySlot("2 PM", 32, 1, R.drawable.ic_weather_sun));
        slots.add(new HourlySlot("3 PM", 32, 1, R.drawable.ic_weather_sun));
        slots.add(new HourlySlot("4 PM", 30, 2, R.drawable.ic_weather_cloud_sun));
        slots.add(new HourlySlot("5 PM", 28, 2, R.drawable.ic_weather_cloud_sun));
        slots.add(new HourlySlot("6 PM", 26, 3, R.drawable.ic_weather_cloud_sun));

        return new WeatherData(city, 31, 33, 48, 8, 32, 22,
                "Partly cloudy", R.drawable.ic_weather_sun, slots);
    }

    private static WeatherData parseCachedJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONObject current = obj.getJSONObject("current");
            JSONObject hourly = obj.getJSONObject("hourly");

            int currentTemp = (int) Math.round(current.getDouble("temperature_2m"));
            int apparentTemp = (int) Math.round(current.optDouble("apparent_temperature", currentTemp));
            int humidity = (int) Math.round(current.optDouble("relative_humidity_2m", 48));
            int wind = (int) Math.round(current.optDouble("wind_speed_10m", 8));
            int weatherCode = current.optInt("weather_code", 1);

            JSONArray hourlyTemps = hourly.getJSONArray("temperature_2m");
            JSONArray hourlyCodes = hourly.getJSONArray("weather_code");

            int high = currentTemp;
            int low = currentTemp;
            for (int i = 0; i < hourlyTemps.length(); i++) {
                int t = (int) Math.round(hourlyTemps.getDouble(i));
                if (t > high) high = t;
                if (t < low) low = t;
            }

            Calendar cal = Calendar.getInstance();
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);

            List<HourlySlot> slots = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                int targetHour = (currentHour + i) % 24;
                int temp = (targetHour < hourlyTemps.length()) ? (int) Math.round(hourlyTemps.getDouble(targetHour)) : currentTemp;
                int code = (targetHour < hourlyCodes.length()) ? hourlyCodes.getInt(targetHour) : weatherCode;

                String label = (i == 0) ? "Now" : (targetHour % 12 == 0 ? 12 : targetHour % 12) + (targetHour >= 12 ? " PM" : " AM");
                slots.add(new HourlySlot(label, temp, code, getWeatherIcon(code)));
            }

            return new WeatherData("Weather", currentTemp, apparentTemp, humidity, wind, high, low,
                    getWeatherCondition(weatherCode), getWeatherIcon(weatherCode), slots);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getWeatherCondition(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 61: case 63: case 65: return "Rain";
            case 71: case 73: case 75: return "Snow";
            case 80: case 81: case 82: return "Rain showers";
            case 95: case 96: case 99: return "Thunderstorm";
            default: return "Partly cloudy";
        }
    }

    public static int getWeatherIcon(int code) {
        switch (code) {
            case 0:
            case 1:
                return R.drawable.ic_weather_sun;
            case 2:
            case 3:
                return R.drawable.ic_weather_cloud_sun;
            default:
                return R.drawable.ic_weather_cloud_sun;
        }
    }
}
