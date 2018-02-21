package com.inaka.galgo.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.inaka.galgo.Galgo;
import com.inaka.galgo.GalgoOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomLogger {

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 9879;
    private static final String TAG = "CustomLogger";
    private static boolean ON = false;
    private static boolean showOnScreen = false;

    /* be careful - static context leakage*/
    private static TextView logsView;
    private static CustomLogger mInstance;

    private Context context;
    private LogCatTask logCatTask = new LogCatTask();


    /*Call this when you start your application, possibly in the onCreate of the Application class*/
    public static void init(Context _context) {
        mInstance = new CustomLogger(_context);
        if (mInstance.context != null) {
            CustomLogger.i(TAG, "init: CustomLogger is now ON");
            ON = true;
        } else {
            Log.w(TAG, "init: you didn't activate the logger due to null context");
        }
    }

    public CustomLogger(Context _context) {
        mInstance = this;
        mInstance.context = _context;
    }

    public static boolean isShowing() {
        return showOnScreen;
    }

    private boolean checkPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                return false;
            } else
                return true;
        }
        return true;
    }

    public static void s(String TAG, String msg) {
        if (showOnScreen)
            Galgo.log(TAG + ": S: " + msg);
    }

    public static void v(String TAG, String msg) {
        Logv(TAG, msg);
        if (showOnScreen)
            Galgo.log(TAG + ": V: " + msg);
    }

    public static void d(String TAG, String msg) {
        Logd(TAG, msg);
        if (showOnScreen)
            Galgo.log(TAG + ": D: " + msg);
    }

    public static void i(String TAG, String msg) {
        Logi(TAG, msg);
        if (showOnScreen)
            Galgo.log(TAG + ": I: " + msg);
    }

    public static void e(String TAG, String msg, Throwable t) {
        Loge(TAG, msg, t);
        if (showOnScreen)
            Galgo.log(TAG + ": E: " + msg + ", throwable: " + t);
    }

    public static void w(String TAG, String msg) {
        Logw(TAG, msg);
        if (showOnScreen)
            Galgo.log(TAG + ": W: " + msg);
    }

    public static void e(String TAG, String msg) {
        Loge(TAG, msg);
        if (showOnScreen)
            Galgo.log(TAG + ": E: " + msg);
    }


    private static void Loge(String tag, String msg, Throwable t) {
        if (ON) {
            Log.e(tag, msg, t);
        }
    }

    private static void Logv(String tag, String msg) {
        if (ON) {
            Log.v(tag, msg);
        }
    }

    private static void Logd(String tag, String msg) {
        if (ON) {
            Log.d(tag, msg);
        }
    }

    private static void Logi(String tag, String msg) {
        if (ON) {
            Log.i(tag, msg);
        }
    }

    private static void Logw(String tag, String msg) {
        if (ON) {
            Log.w(tag, msg);
        }
    }

    private static void Loge(String tag, String msg) {
        if (ON) {
            Log.e(tag, msg);
        }
    }

    public static void showLogCatLogsOverlay(Activity activity, boolean toggle) {
        if (ON) {
            showOnScreen = toggle;
            if (toggle) {
                // add some customization to the log messages
                GalgoOptions options = new GalgoOptions.Builder()
                        .numberOfLines(12)
                        .backgroundColor(android.R.color.holo_blue_bright)
                        .textColor(Color.BLACK)
                        .textSize(10)
                        .build();

                if (mInstance.checkPermission(activity))
                    Galgo.enable(mInstance.context, options);
            } else {
                try {
                    Galgo.disable(activity);

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.w(TAG, "showLogCatLogsOverlay: if you want to see galgo, enable CustomLogger");
        }
    }

    private static void showLogCatLogsInView(TextView tv) {
        if (tv != null) {
            if (logsView != null &&
                    logsView.isAttachedToWindow()) {
                logsView.clearComposingText();
            }
            logsView = tv;
            mInstance.logCatTask.execute();
        }
    }

    public static void startLogcatTask(TextView _logsView) {
        logsView = _logsView;
        mInstance.logCatTask = new CustomLogger.LogCatTask();
        showLogCatLogsInView(logsView);
    }

    public static void stopLogcatTask() {
        mInstance.logCatTask.cancel(true);
    }

    public static class LogCatTask extends AsyncTask<Void, String, Void> {
        public static final String TAG = "LogCatTask";

        AtomicBoolean run = new AtomicBoolean(true);

        @Override
        protected void onProgressUpdate(String... values) {
            try {
                if (logsView != null && logsView.getLayout() != null) {

                    logsView.setText(values[0]);
                    final int scrollAmount =
                            logsView.getLayout().getLineTop(
                                    logsView.getLineCount()
                            ) - logsView.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0)
                        logsView.scrollTo(0, scrollAmount);
                    else
                        logsView.scrollTo(0, 0);
                }
            } catch (Exception e) {
                e(TAG, "onProgressUpdate: ignore: ", e);
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Runtime.getRuntime().exec("logcat -c");
                Process process = Runtime.getRuntime().exec("logcat");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder log = new StringBuilder();
                String line;
                while (run.get()) {
                    line = bufferedReader.readLine();
                    if (line != null) {
                        log.append(line);
                        publishProgress(log.toString());
                    }
                    Thread.sleep(100);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }
}
