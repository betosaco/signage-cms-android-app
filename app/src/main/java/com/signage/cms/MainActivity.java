package com.signage.cms;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.signage.cms.utils.WebViewHelper;

/**
 * MainActivity for Digital Signage CMS Android Application.
 * Handles kiosk mode, WebView configuration, network monitoring, and crash recovery.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SignageCMS";
    private static final String CMS_URL = "https://your-cms-url.com"; // TODO: Load from config
    private WebView webView;
    private PowerManager.WakeLock wakeLock;
    private boolean isOnline = true;

    /**
     * Called when the activity is first created.
     * Sets up full-screen kiosk mode, WebView, and network monitoring.
     */
    @SuppressLint({"SetJavaScriptEnabled", "WakelockTimeout"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set full-screen immersive mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Prevent screenshots for security
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(new FrameLayout(this));

        // Lock orientation to landscape
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Initialize and configure WebView
        webView = new WebView(this);
        ((FrameLayout) findViewById(android.R.id.content)).addView(webView);
        WebViewHelper.configureWebViewForSignage(webView, this);
        webView.setWebViewClient(new SignageWebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        loadCmsContent();

        // Acquire wake lock to prevent device sleep
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);
        wakeLock.acquire();

        // Register network monitoring
        registerNetworkCallback();

        Log.i(TAG, "MainActivity created and initialized.");
    }

    /**
     * Loads the CMS content URL into the WebView.
     */
    private void loadCmsContent() {
        if (isOnline) {
            webView.loadUrl(CMS_URL);
        } else {
            // Show offline UI or cached content
            webView.loadUrl("file:///android_asset/offline.html");
            Toast.makeText(this, "Offline mode: displaying cached content.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Registers a network callback to monitor connectivity changes.
     */
    private void registerNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    isOnline = true;
                    runOnUiThread(() -> loadCmsContent());
                    Log.i(TAG, "Network available.");
                }
                @Override
                public void onLost(@NonNull Network network) {
                    isOnline = false;
                    runOnUiThread(() -> loadCmsContent());
                    Log.w(TAG, "Network lost.");
                }
            });
        } else {
            // Fallback for older devices
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isOnline = WebViewHelper.isNetworkAvailable(context);
                    loadCmsContent();
                }
            }, filter);
        }
    }

    /**
     * Ensures immersive full-screen mode is maintained.
     */
    @Override
    protected void onResume() {
        super.onResume();
        enterImmersiveMode();
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    /**
     * Releases resources and wake lock.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /**
     * Clean up WebView and resources.
     */
    @Override
    protected void onDestroy() {
        if (webView != null) {
            ((FrameLayout) findViewById(android.R.id.content)).removeView(webView);
            webView.destroy();
            webView = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    /**
     * Enters immersive sticky mode for kiosk operation.
     */
    private void enterImmersiveMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    /**
     * Disables the back button for kiosk mode.
     */
    @Override
    public void onBackPressed() {
        // Do nothing to disable back button
    }

    /**
     * Custom WebViewClient for signage error handling and SSL support.
     */
    private static class SignageWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            // Only allow navigation within CMS domain (TODO: implement domain check)
            return false;
        }
        @Override
        public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
            // Handle SSL errors gracefully for signage (accept self-signed if needed)
            handler.proceed(); // WARNING: Accepts all SSL certs. Replace with proper validation in production.
        }
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.e(TAG, "WebView error: " + description);
            view.loadUrl("file:///android_asset/error.html");
        }
    }
} 