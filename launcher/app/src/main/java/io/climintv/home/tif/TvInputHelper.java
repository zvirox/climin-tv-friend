package io.climintv.home.tif;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.climintv.home.model.TvInputEntry;

public class TvInputHelper {
    private static final String TAG = "TvInputHelper";

    public static List<TvInputEntry> getAvailableInputs(Context context) {
        List<TvInputEntry> list = new ArrayList<>();
        try {
            TvInputManager manager = (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
            if (manager == null) {
                return list;
            }

            List<TvInputInfo> inputs = manager.getTvInputList();
            if (inputs == null) {
                return list;
            }

            for (TvInputInfo info : inputs) {
                if (info.isPassthroughInput()) {
                    CharSequence label = TvInputEntry.resolveLabel(context, info);
                    list.add(new TvInputEntry(info.getId(), label, true, info.getType()));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error querying TvInputManager: " + e.getMessage());
        }
        return list;
    }

    public static boolean launchInput(Context context, TvInputEntry input) {
        if (input == null || !input.isPassthrough()) {
            return false;
        }

        try {
            Uri channelUri = input.buildUri();
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setData(channelUri);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(viewIntent);
            return true;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No activity found to handle TIF passthrough URI: " + e.getMessage());
            Toast.makeText(context, "Cannot open " + input.getLabel(), Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch TV input: " + e.getMessage());
            return false;
        }
    }
}
