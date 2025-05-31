package com.matmax.signage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.PowerManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import android.os.Handler;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.BarcodeFormat;
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity {
    private PowerManager.WakeLock wakeLock;
    private FrameLayout container;
    private WebView webView;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private ImageView imageView;
    private Handler handler = new Handler();
    private Runnable contentSwitcher;
    private static final int CONTENT_DURATION_MS = 10000; // 10 seconds per content
    private FloatingActionButton fab;

    @Override
    public void onBackPressed() {
        // Disable back button for kiosk mode
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            container.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    }

    private Handler heartbeatHandler = new Handler();
    private Runnable heartbeatRunnable;
    private static final int HEARTBEAT_INTERVAL_MS = 60000; // 1 minute

    @SuppressLint({"SetJavaScriptEnabled", "WakelockTimeout"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        container = new FrameLayout(this);
        setContentView(container);

        // Full-screen immersive mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // Prevent device sleep
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MATMAXSignage:WakeLock");
        wakeLock.acquire();

        addHelpFab();
        // Fetch playlist from backend using deviceId
        String deviceId = DeviceManager.getInstance(this).getDeviceId();
        ContentManager.getInstance(this).fetchContentFromBackend(deviceId);
        playCurrentContent();
        startContentRotation();
        startHeartbeat();
    }

    private void addHelpFab() {
        fab = new FloatingActionButton(this);
        fab.setImageResource(android.R.drawable.ic_menu_help);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.rightMargin = 32;
        params.bottomMargin = 32;
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        fab.setLayoutParams(params);
        fab.setOnClickListener(v -> showHelpDialog());
        container.addView(fab);
    }

    private void showHelpDialog() {
        android.util.Log.d("MATMAX", "FAB clicked, showing help dialog");
        Toast.makeText(this, "Opening help dialog", Toast.LENGTH_SHORT).show();
        View view = LayoutInflater.from(this).inflate(R.layout.qr_help_dialog, null);
        ImageView qrView = view.findViewById(R.id.imageViewQrCode);
        TextView linkView = view.findViewById(R.id.textViewHelpLink);
        Button closeBtn = view.findViewById(R.id.buttonClose);
        String url = "https://wa.link/wo8gb9";
        linkView.setText(url);
        linkView.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        linkView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 200, 200);
            qrView.setImageBitmap(bitmap);
            android.util.Log.d("MATMAX", "QR code generated successfully");
        } catch (Exception e) {
            qrView.setImageResource(android.R.drawable.ic_dialog_alert);
            String errMsg = "Failed to generate QR code: " + e.getMessage();
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
            android.util.Log.e("MATMAX", errMsg, e);
        }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();
        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void playCurrentContent() {
        ContentManager cm = ContentManager.getInstance(this);
        ContentItem item = cm.getCurrentContent();
        if (item == null) {
            Toast.makeText(this, "No content to display", Toast.LENGTH_LONG).show();
            return;
        }
        container.removeAllViews();
        if (fab != null) container.addView(fab);
        switch (item.type) {
            case WEB:
                showWebContent(item.url);
                break;
            case VIDEO:
                showVideoContent(item.localPath != null ? item.localPath : item.url);
                break;
            case IMAGE:
                showImageContent(item.localPath != null ? item.localPath : item.url);
                break;
        }
    }

    private void showWebContent(String url) {
        if (webView == null) {
            webView = new WebView(this);
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            webSettings.setUserAgentString(webSettings.getUserAgentString() + " MATMAXSignage/1.0");
            webView.setWebChromeClient(new WebChromeClient());
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }
            });
        }
        container.addView(webView);
        webView.loadUrl(url);
    }

    private void showVideoContent(String url) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            playerView = new PlayerView(this);
            playerView.setPlayer(exoPlayer);
        }
        container.addView(playerView);
        MediaItem mediaItem = MediaItem.fromUri(url);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }

    private void showImageContent(String url) {
        if (imageView == null) {
            imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        container.addView(imageView);
        Glide.with(this).load(url).into(imageView);
    }

    private void startContentRotation() {
        contentSwitcher = new Runnable() {
            @Override
            public void run() {
                playNextContent();
                handler.postDelayed(this, CONTENT_DURATION_MS);
            }
        };
        handler.postDelayed(contentSwitcher, CONTENT_DURATION_MS);
    }

    private void playNextContent() {
        ContentManager cm = ContentManager.getInstance(this);
        ContentItem item = cm.getNextContent();
        if (item == null) return;
        container.removeAllViews();
        if (fab != null) container.addView(fab);
        switch (item.type) {
            case WEB:
                showWebContent(item.url);
                break;
            case VIDEO:
                showVideoContent(item.localPath != null ? item.localPath : item.url);
                break;
            case IMAGE:
                showImageContent(item.localPath != null ? item.localPath : item.url);
                break;
        }
    }

    private void startHeartbeat() {
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                String status = "ok"; // TODO: Gather real status
                String timestamp = java.time.Instant.now().toString();
                DeviceManager.getInstance(MainActivity.this).sendHeartbeat(status, timestamp);
                DeviceManager.getInstance(MainActivity.this).pollRemoteCommands();
                heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
            }
        };
        heartbeatHandler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (webView != null) {
            webView.destroy();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        if (handler != null && contentSwitcher != null) {
            handler.removeCallbacks(contentSwitcher);
        }
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
        }
        // TODO: Add crash recovery and watchdog logic for 24/7 operation
    }
} 