package com.sparkexe.comeback;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.view.View;
import android.content.res.Configuration;

import androidx.webkit.WebViewAssetLoader;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Local, offline Android application shell for Spark.exe. No remote server needed. */
public class MainActivity extends Activity {
    private static final int PICK_BACKUP = 701;
    private static final int SAVE_BACKUP = 702;
    private static final String APP_HOST = "appassets.androidplatform.net";
    private WebView webView;
    private ValueCallback<Uri[]> pendingFileChooser;
    private String pendingBackupJson;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(250, 248, 255));
        getWindow().setNavigationBarColor(Color.rgb(250, 248, 255));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(250, 248, 255));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        // The only exposed JavaScript bridge is used to save a user-requested JSON backup.
        webView.addJavascriptInterface(new BackupBridge(), "SparkAndroid");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if ("https".equals(url.getScheme()) && APP_HOST.equals(url.getHost())
                        && url.getPath() != null && url.getPath().startsWith("/assets/")) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                } catch (ActivityNotFoundException ignored) { }
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (pendingFileChooser != null) pendingFileChooser.onReceiveValue(null);
                pendingFileChooser = callback;
                Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(pick, "Choose Spark backup JSON"), PICK_BACKUP);
                } catch (ActivityNotFoundException e) {
                    pendingFileChooser.onReceiveValue(null);
                    pendingFileChooser = null;
                    Toast.makeText(MainActivity.this, "No file picker available", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    public class BackupBridge {
        @JavascriptInterface
        public void exportBackup(String json, String filename) {
            runOnUiThread(() -> {
                pendingBackupJson = json;
                Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                create.addCategory(Intent.CATEGORY_OPENABLE);
                create.setType("application/json");
                create.putExtra(Intent.EXTRA_TITLE,
                        filename != null && filename.endsWith(".json") ? filename : "spark-exe-backup.json");
                try {
                    startActivityForResult(create, SAVE_BACKUP);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "No document saver available", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_BACKUP) {
            if (pendingFileChooser != null) {
                Uri selected = resultCode == RESULT_OK && data != null ? data.getData() : null;
                pendingFileChooser.onReceiveValue(selected == null ? null : new Uri[] {selected});
                pendingFileChooser = null;
            }
        } else if (requestCode == SAVE_BACKUP) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null
                    && pendingBackupJson != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out == null) throw new IllegalStateException("Could not write file");
                    out.write(pendingBackupJson.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "Backup saved", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Could not save backup", Toast.LENGTH_LONG).show();
                }
            }
            pendingBackupJson = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
