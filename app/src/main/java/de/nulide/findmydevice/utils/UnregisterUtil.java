package de.nulide.findmydevice.utils;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.VolleyError;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.nulide.findmydevice.R;
import de.nulide.findmydevice.data.Settings;
import de.nulide.findmydevice.data.SettingsRepoSpec;
import de.nulide.findmydevice.data.SettingsRepository;

public class UnregisterUtil {

    public static void showUnregisterFailedDialog(Context context, VolleyError error, OnContinueClickedListener onContinueClickedListener) {

        String message = context.getString(R.string.server_unregister_failed_body);
        if (error != null && error.getMessage() != null) {
            message = message.replace("{ERROR}", error.getMessage());
        } else {
            message = message.replace("{ERROR}", "error or getMessage() was null!");
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.server_unregister_failed_title))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.server_unregister_continue_anyway),
                        (DialogInterface dialog, int which) -> {
                            Settings settings = SettingsRepository.Companion.getInstance(new SettingsRepoSpec(context)).getSettings();
                            settings.set(Settings.SET_FMDSERVER_ID, ""); // force local logout
                            onContinueClickedListener.onContinueClicked();
                            dialog.dismiss();
                        })
                .setNegativeButton(context.getString(R.string.server_unregister_dont_continue),
                        (DialogInterface dialog, int which) -> {
                            dialog.dismiss();
                        })
                .show();
    }

    public interface OnContinueClickedListener {
        void onContinueClicked();
    }

}
