package io.climintv.home.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MediaItem {
    private final String id;
    private final String title;
    private final String overview;
    private final String backdropUrl;
    private final String posterUrl;
    private final double rating;
    private final String year;
    private final String providerName;
    private final String providerPackage;
    private final String deepLinkUri;
    private final boolean isWide;

    public MediaItem(String id, String title, String overview, String backdropUrl, String posterUrl,
                     double rating, String year, String providerName, String providerPackage,
                     String deepLinkUri, boolean isWide) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.backdropUrl = backdropUrl;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.year = year;
        this.providerName = providerName;
        this.providerPackage = providerPackage;
        this.deepLinkUri = deepLinkUri;
        this.isWide = isWide;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getBackdropUrl() { return backdropUrl; }
    public String getPosterUrl() { return posterUrl; }
    public double getRating() { return rating; }
    public String getYear() { return year; }
    public String getProviderName() { return providerName; }
    public String getProviderPackage() { return providerPackage; }
    public String getDeepLinkUri() { return deepLinkUri; }
    public boolean isWide() { return isWide; }

    public String getImageUrl() {
        if (isWide && backdropUrl != null && !backdropUrl.isEmpty()) {
            return backdropUrl;
        }
        return (posterUrl != null && !posterUrl.isEmpty()) ? posterUrl : backdropUrl;
    }

    public void launch(Context context) {
        // Step 1: Try explicit deep link if present
        if (deepLinkUri != null && !deepLinkUri.isEmpty()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri));
                if (providerPackage != null && !providerPackage.isEmpty()) {
                    intent.setPackage(providerPackage);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }

        // Step 2: Fall back to launching the streaming app itself
        if (providerPackage != null && !providerPackage.isEmpty()) {
            try {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(providerPackage);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // Step 3: Web or search fallback
        try {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(title + " trailer")));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        } catch (Exception e) {
            Toast.makeText(context, "Opening " + title + "...", Toast.LENGTH_SHORT).show();
        }
    }
}
