package com.signage.cms.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.List;

/**
 * Utility class for advanced WebView configuration and management for digital signage.
 */
public class WebViewHelper {
    private static final String TAG = "WebViewHelper";
    private static final String USER_AGENT_SUFFIX = " SignageDevice/1.0";

    /**
     * Master configuration method for signage WebView.
     * @param webView The WebView to configure.
     * @param context The context.
     */
    @SuppressLint("SetJavaScriptEnabled")
    public static void configureWebViewForSignage(WebView webView, Context context) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCacheEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        customizeUserAgent(webView);
        enableOptimalPerformance(webView);
        configureSecuritySettings(webView);
        setupJavaScriptInterface(webView, context);
        setupOfflineSupport(webView, context);
        enableHardwareAcceleration(webView);
    }

    /**
     * Enables optimal performance settings for 24/7 operation.
     * @param webView The WebView.
     */
    public static void enableOptimalPerformance(WebView webView) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        }
    }

    /**
     * Sets up offline support (cache, storage).
     * @param webView The WebView.
     * @param context The context.
     */
    public static void setupOfflineSupport(WebView webView, Context context) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        settings.setAppCachePath(context.getCacheDir().getAbsolutePath());
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    /**
     * Configures security settings for WebView.
     * @param webView The WebView.
     */
    public static void configureSecuritySettings(WebView webView) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setSavePassword(false);
        settings.setSaveFormData(false);
    }

    /**
     * Sets up JavaScript interface for CMS communication.
     * @param webView The WebView.
     * @param context The context.
     */
    public static void setupJavaScriptInterface(WebView webView, Context context) {
        if (webView == null) return;
        webView.addJavascriptInterface(new SignageJavaScriptInterface(context), "SignageJS");
    }

    /**
     * Customizes the user agent string for signage device identification.
     * @param webView The WebView.
     */
    public static void customizeUserAgent(WebView webView) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        String ua = settings.getUserAgentString();
        if (ua == null) ua = "";
        if (!ua.contains(USER_AGENT_SUFFIX)) {
            settings.setUserAgentString(ua + USER_AGENT_SUFFIX);
        }
    }

    /**
     * Configures cache settings for signage content.
     * @param webView The WebView.
     * @param context The context.
     */
    public static void configureCacheSettings(WebView webView, Context context) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        settings.setAppCachePath(context.getCacheDir().getAbsolutePath());
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    /**
     * Clears the WebView cache.
     * @param webView The WebView.
     * @param context The context.
     */
    public static void clearWebViewCache(WebView webView, Context context) {
        if (webView == null) return;
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
        Toast.makeText(context, "WebView cache cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Gets the cache size (dummy implementation, as Android does not expose this directly).
     * @param context The context.
     * @return Cache size in bytes.
     */
    public static long getCacheSize(Context context) {
        return context.getCacheDir().length();
    }

    /**
     * Preloads a list of URLs for offline use.
     * @param webView The WebView.
     * @param urls List of URLs to preload.
     */
    public static void preloadContent(WebView webView, List<String> urls) {
        if (webView == null || urls == null) return;
        for (String url : urls) {
            webView.loadUrl(url);
        }
    }

    /**
     * Enables hardware acceleration for the WebView.
     * @param webView The WebView.
     */
    public static void enableHardwareAcceleration(WebView webView) {
        if (webView == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        }
    }

    /**
     * Optimizes memory usage for long-running operation.
     * @param webView The WebView.
     */
    public static void optimizeMemoryUsage(WebView webView) {
        if (webView == null) return;
        webView.freeMemory();
        webView.clearHistory();
    }

    /**
     * Configures network timeout (not directly supported, but can be handled via JS interface or custom client).
     * @param webView The WebView.
     */
    public static void configureNetworkTimeout(WebView webView) {
        // Not directly supported; implement in WebViewClient if needed
    }

    /**
     * Enables fast scrolling for large content pages.
     * @param webView The WebView.
     */
    public static void enableFastScrolling(WebView webView) {
        if (webView == null) return;
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
    }

    /**
     * Sets up SSL handling (should be handled in WebViewClient).
     * @param webView The WebView.
     */
    public static void setupSSLHandling(WebView webView) {
        // SSL error handling should be implemented in WebViewClient
    }

    /**
     * Configures content security policies (not directly supported, but can be enforced via JS or headers).
     * @param webView The WebView.
     */
    public static void configureContentSecurity(WebView webView) {
        // Implement via CSP headers in CMS content
    }

    /**
     * Prevents data leakage by disabling risky features.
     * @param webView The WebView.
     */
    public static void preventDataLeakage(WebView webView) {
        if (webView == null) return;
        WebSettings settings = webView.getSettings();
        settings.setSavePassword(false);
        settings.setSaveFormData(false);
    }

    /**
     * Sets up file access securely.
     * @param webView The WebView.
     * @param allowFileAccess Whether to allow file access.
     */
    public static void setupFileAccess(WebView webView, boolean allowFileAccess) {
        if (webView == null) return;
        webView.getSettings().setAllowFileAccess(allowFileAccess);
    }

    /**
     * Sets up download manager for content downloads.
     * @param webView The WebView.
     * @param context The context.
     */
    public static void setupDownloadManager(WebView webView, Context context) {
        // Implement download handling as needed
    }

    /**
     * Configures file providers for secure file sharing.
     * @param webView The WebView.
     * @param context The context.
     */
    public static void configureFileProviders(WebView webView, Context context) {
        // Implement file provider setup as needed
    }

    /**
     * Handles dynamic content updates.
     * @param webView The WebView.
     */
    public static void handleContentUpdates(WebView webView) {
        // Implement content update logic as needed
    }

    /**
     * Validates content source URLs before loading.
     * @param url The URL to validate.
     * @return True if valid, false otherwise.
     */
    public static boolean validateContentSource(String url) {
        // Implement domain whitelist/validation as needed
        return url != null && url.startsWith("https://");
    }

    /**
     * Standardized error logging for WebView.
     * @param tag Log tag.
     * @param message Error message.
     * @param throwable Throwable.
     */
    public static void logWebViewError(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
    }

    /**
     * Handles WebView crashes and attempts recovery.
     * @param webView The WebView.
     * @param context The context.
     */
    public static void handleWebViewCrash(WebView webView, Context context) {
        if (webView == null) return;
        Toast.makeText(context, "WebView crashed. Restarting...", Toast.LENGTH_LONG).show();
        webView.reload();
    }

    /**
     * Monitors WebView health for long-running operation.
     * @param webView The WebView.
     */
    public static void monitorWebViewHealth(WebView webView) {
        // Implement health checks as needed
    }

    /**
     * Generates a diagnostic report for the WebView/system.
     * @param webView The WebView.
     * @param context The context.
     */
    public static void generateDiagnosticReport(WebView webView, Context context) {
        // Implement diagnostics as needed
    }

    /**
     * Checks if network is available.
     * @param context The context.
     * @return True if network is available, false otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        return false;
    }

    /**
     * JavaScript interface for CMS communication.
     */
    public static class SignageJavaScriptInterface {
        private final Context context;
        public SignageJavaScriptInterface(Context context) {
            this.context = context;
        }
        @JavascriptInterface
        public String getDeviceInfo() {
            // Return device info as JSON
            return "{\"model\":\"" + Build.MODEL + "\",\"manufacturer\":\"" + Build.MANUFACTURER + "\",\"version\":\"" + Build.VERSION.RELEASE + "\"}";
        }
        @JavascriptInterface
        public void reportStatus(String status) {
            Log.i(TAG, "Status reported from JS: " + status);
        }
        @JavascriptInterface
        public void updateConfiguration(String config) {
            Log.i(TAG, "Config update from JS: " + config);
            // Implement config update logic
        }
        @JavascriptInterface
        public void restartApplication() {
            Log.i(TAG, "Restart requested from JS");
            Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        }
        @JavascriptInterface
        public void captureScreenshot() {
            Log.i(TAG, "Screenshot requested from JS");
            // Implement screenshot logic
        }
    }
} 