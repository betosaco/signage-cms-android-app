package com.matmax.signage;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.matmax.signage.graphql.IsDevicePairedQuery;
import com.matmax.signage.network.GraphQLManager;
import java.security.SecureRandom;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.RelativeSizeSpan;
import android.graphics.Typeface;

public class PairingActivity extends AppCompatActivity {
    private String pairingCode;
    private Handler handler = new Handler();
    private boolean polling = false;
    private Runnable pollTask;
    private long startTime;
    private TextView instructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);

        TextView codeView = findViewById(R.id.textViewPairingCode);
        pairingCode = generatePairingCode();
        codeView.setText(pairingCode);

        instructions = findViewById(R.id.textViewInstructions);
        instructions.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        instructions.setGravity(android.view.Gravity.CENTER);
        startTime = System.currentTimeMillis();
        startTimer();
        startPollingForPairing();
    }

    private void startPollingForPairing() {
        polling = true;
        pollTask = new Runnable() {
            @Override
            public void run() {
                if (!polling) return;
                GraphQLManager.getInstance(PairingActivity.this).isDevicePaired(pairingCode, new GraphQLManager.ApolloCallback<IsDevicePairedQuery.Data>() {
                    @Override
                    public void onSuccess(IsDevicePairedQuery.Data data) {
                        if (data != null && data.isDevicePaired() != null && data.isDevicePaired().getPaired()) {
                            String deviceId = data.isDevicePaired().getDeviceId();
                            DeviceManager.getInstance(PairingActivity.this).setPaired(true);
                            // Save deviceId in DeviceManager
                            DeviceManager.getInstance(PairingActivity.this).registerDevice(pairingCode, android.os.Build.MODEL + " (" + android.os.Build.VERSION.RELEASE + ")");
                            Toast.makeText(PairingActivity.this, "Device paired!", Toast.LENGTH_SHORT).show();
                            polling = false;
                            finish();
                        } else {
                            handler.postDelayed(pollTask, 5000); // poll every 5 seconds
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        handler.postDelayed(pollTask, 5000);
                    }
                });
            }
        };
        handler.post(pollTask);
    }

    private void startTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!polling) return;
                long elapsed = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsed / 1000) % 60;
                int minutes = (int) ((elapsed / (1000 * 60)) % 60);
                int hours = (int) ((elapsed / (1000 * 60 * 60)) % 24);
                String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                String message = "\n\n" +
                        "Waiting for pairing..." +
                        "\n\n" +
                        time +
                        "\n\n" +
                        "Enter the code in your Dishub signage module." +
                        "\n\n";
                SpannableString spannable = new SpannableString(message);
                int timeStart = message.indexOf(time);
                int timeEnd = timeStart + time.length();
                if (timeStart >= 0) {
                    spannable.setSpan(new StyleSpan(Typeface.BOLD), timeStart, timeEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new RelativeSizeSpan(1.5f), timeStart, timeEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                instructions.setText(spannable);
                instructions.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                instructions.setGravity(android.view.Gravity.CENTER);
                instructions.setLineSpacing(16f, 1.2f);
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        polling = false;
        handler.removeCallbacksAndMessages(null);
    }

    private String generatePairingCode() {
        SecureRandom random = new SecureRandom();
        int part1 = random.nextInt(900) + 100; // 100-999
        int part2 = random.nextInt(900) + 100; // 100-999
        return String.format("%03d-%03d", part1, part2);
    }
} 