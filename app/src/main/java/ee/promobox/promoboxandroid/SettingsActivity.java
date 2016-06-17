package ee.promobox.promoboxandroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Joiner;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.promobox.promoboxandroid.data.Settings;
import ee.promobox.promoboxandroid.service.FileService;
import ee.promobox.promoboxandroid.util.TimeUtil;


public class SettingsActivity extends Activity implements View.OnClickListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsActivity.class);

    private Settings settings;

    private EditText deviceId;
    private EditText server;
    private TextView RAM;
    private TextView CPU;
    private ListView logs;

    private List<String> logsList;

    private static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }

    private static List<String> readLogs() {


        File file = new File(FileService.ROOT, String.format("promobox.%tF.log", new Date()));
        List<String> listLines = new ArrayList<>();
        int count = 100;

        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

            int lines = 0;

            StringBuilder builder = new StringBuilder();

            long length = file.length();
            length--;
            randomAccessFile.seek(length);
            for (long seek = length; seek >= 0; --seek) {
                randomAccessFile.seek(seek);
                char c = (char) randomAccessFile.read();
                builder.append(c);
                if (c == '\n') {
                    builder = builder.reverse();
                    listLines.add(builder.toString());
                    lines++;
                    builder = null;
                    builder = new StringBuilder();

                    if (lines == count) {
                        break;
                    }
                }

            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return listLines;
    }

    private void hideSystemUI() {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();

        settings = getIntent().getParcelableExtra("settings");

        LOGGER.debug(settings.toString());

        setContentView(R.layout.settings);

        deviceId = (EditText) findViewById(R.id.settings_device_id);
        deviceId.setText(settings.getUuid());

        server = (EditText) findViewById(R.id.serverUrl);
        server.setText(settings.getServer());


        TextView version = (TextView) findViewById(R.id.settings_version);
        TextView upTime = (TextView) findViewById(R.id.settings_uptime);
        Button systemSettingsBtn = (Button) findViewById(R.id.settings_system_settings_btn);
        Button browserBtn = (Button) findViewById(R.id.settings_browser_btn);
        Button fileManagerBtn = (Button) findViewById(R.id.settings_file_mngr_btn);
        Button saveButton = (Button) findViewById(R.id.settings_save_btn);
        Button sendLogsButton = (Button) findViewById(R.id.settings_send_logs_btn);
        ImageButton backButton = (ImageButton) findViewById(R.id.settings_back);
        RAM = (TextView) findViewById(R.id.settings_ram);
        CPU = (TextView) findViewById(R.id.settings_cpu);
        logs = (ListView) findViewById(R.id.settings_logs_list);

        version.setText(BuildConfig.VERSION_CODE + "");
        upTime.setText(TimeUtil.getTimeString(SystemClock.uptimeMillis()));
        systemSettingsBtn.setOnClickListener(this);
        browserBtn.setOnClickListener(this);
        fileManagerBtn.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        sendLogsButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new LogsAsyncTask().execute();
        new CPUAsyncTask().execute();
        new RamAsyncTask().execute();

        hideSystemUI();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private String getCPUProcessInfo() {
        java.lang.Process p = null;
        BufferedReader in = null;
        String returnString = null;
        try {
            p = Runtime.getRuntime().exec("top -n 1");
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (returnString == null || returnString.contentEquals("")) {
                returnString = in.readLine();
            }
        } catch (IOException e) {
            LOGGER.debug("executeTop", "error in getting first line of top");
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                in.close();
                p.destroy();
            } catch (IOException e) {
                Log.e("executeTop",
                        "error in closing and destroying top process");
                e.printStackTrace();
            }
        }
        return returnString;
    }

    private String getRAMInfo() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        long totalMegs = mi.totalMem / 1048576L;
        float percentUsed = 100 - (availableMegs * 100f / totalMegs);
        return String.format("Used:%.1f", percentUsed) + "% Total:" + totalMegs + "MB Free:" + availableMegs + "MB";
    }

    @Override
    public void onClick(View v) {
        hideSystemUI();
        switch (v.getId()) {
            case R.id.settings_system_settings_btn:
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                break;
            case R.id.settings_browser_btn:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.promobox.ee"));
                startActivity(browserIntent);
                break;
            case R.id.settings_file_mngr_btn:
                openApp(this, "com.android.rockchip");
                break;
            case R.id.settings_save_btn:
                saveDeviceSettings();
                break;
            case R.id.settings_back:
                finish();
                break;
            case R.id.settings_send_logs_btn:
                new SendLogsTask().execute(logsList.toArray());
                break;
        }
    }

    private void saveDeviceSettings() {
        String uuid = deviceId.getText().toString();

        settings.setUuid(uuid);
        settings.setServer(server.getText().toString());

        Intent saveSettings = new Intent(MainActivity.SETTINGS_CHANGE);
        saveSettings.putExtra("settings", settings);
        sendBroadcast(saveSettings);

        LOGGER.debug(settings.toString());
    }

    private class RamAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return getRAMInfo();
        }

        @Override
        protected void onPostExecute(String s) {
            RAM.setText(s);
        }
    }

    private class CPUAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return getCPUProcessInfo();
        }

        @Override
        protected void onPostExecute(String s) {
            CPU.setText(s);
        }
    }

    private class LogsAsyncTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            return readLogs();
        }

        @Override
        protected void onPostExecute(List<String> list) {
            logsList = list;
            logs.setAdapter(new ArrayAdapter<String>(SettingsActivity.this, R.layout.settings_logs_listview_item, logsList));
        }
    }

    private class SendLogsTask extends AsyncTask<Object, Void, Void> {


        @Override
        protected Void doInBackground(Object... params) {

            HttpURLConnection conn = null;

            try {
                URL url = new URL(String.format("%s/service/device/%s/saveError", settings.getServer(), settings.getUuid()));

                JSONObject json = new JSONObject();

                json.put("name", "Logs");
                json.put("message", "Logs fom device " + settings.getUuid());
                json.put("date", System.currentTimeMillis());
                json.put("stackTrace", Joiner.on("\n").join(readLogs()));

                conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setDoOutput(true);

                String query = "error=" + URLEncoder.encode(json.toString(), "UTF-8");

                IOUtils.write(query.getBytes(), conn.getOutputStream());

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    LOGGER.info("Logs send");
                }

            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new LogsAsyncTask().execute();
            Toast.makeText(SettingsActivity.this, "Logs are sent", Toast.LENGTH_LONG).show();
        }
    }
}
