package com.matmax.signage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.matmax.signage.graphql.RegisterDeviceMutation;
import com.matmax.signage.graphql.SendHeartbeatMutation;
import com.matmax.signage.graphql.GetRemoteCommandsQuery;
import com.matmax.signage.network.GraphQLManager;
import java.util.List;

public class DeviceManager {
    private static DeviceManager instance;
    private boolean paired = false;
    private String pairingCode = "";
    private String deviceId = null;
    private static final String PREFS = "device_prefs";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_PAIRED = "paired";
    private final Context context;

    private DeviceManager(Context context) {
        this.context = context.getApplicationContext();
        SharedPreferences prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        deviceId = prefs.getString(KEY_DEVICE_ID, null);
        paired = prefs.getBoolean(KEY_PAIRED, false);
    }

    public static synchronized DeviceManager getInstance(Context context) {
        if (instance == null) {
            instance = new DeviceManager(context.getApplicationContext());
        }
        return instance;
    }

    public void registerDevice(String code, String deviceInfo) {
        GraphQLManager.getInstance(context).registerDevice(code, deviceInfo, new GraphQLManager.ApolloCallback<RegisterDeviceMutation.Data>() {
            @Override
            public void onSuccess(RegisterDeviceMutation.Data data) {
                if (data != null && data.getRegisterDevice() != null && data.getRegisterDevice().getSuccess()) {
                    paired = true;
                    pairingCode = code;
                    deviceId = data.getRegisterDevice().getDeviceId();
                    savePairingState();
                    Log.i("DeviceManager", "Device paired successfully: " + deviceId);
                } else {
                    Log.e("DeviceManager", "Pairing failed: " + (data != null ? data.getRegisterDevice().getMessage() : "null response"));
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("DeviceManager", "Pairing error", e);
            }
        });
    }

    public void sendHeartbeat(String status, String timestamp) {
        if (deviceId == null) return;
        GraphQLManager.getInstance(context).sendHeartbeat(deviceId, status, timestamp, new GraphQLManager.ApolloCallback<SendHeartbeatMutation.Data>() {
            @Override
            public void onSuccess(SendHeartbeatMutation.Data data) {
                Log.i("DeviceManager", "Heartbeat sent");
            }
            @Override
            public void onError(Exception e) {
                Log.e("DeviceManager", "Heartbeat error", e);
            }
        });
    }

    public void pollRemoteCommands() {
        if (deviceId == null) return;
        GraphQLManager.getInstance(context).getRemoteCommands(deviceId, new GraphQLManager.ApolloCallback<GetRemoteCommandsQuery.Data>() {
            @Override
            public void onSuccess(GetRemoteCommandsQuery.Data data) {
                if (data != null && data.getRemoteCommands() != null) {
                    List<GetRemoteCommandsQuery.RemoteCommand> commands = data.getRemoteCommands();
                    for (GetRemoteCommandsQuery.RemoteCommand cmd : commands) {
                        Log.i("DeviceManager", "Received remote command: " + cmd.getType());
                        // TODO: Handle remote command (reboot, update, etc.)
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("DeviceManager", "Remote command error", e);
            }
        });
    }

    public boolean isPaired() {
        return paired;
    }
    public String getPairingCode() {
        return pairingCode;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public void setPaired(boolean paired) {
        this.paired = paired;
        savePairingState();
    }
    private void savePairingState() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .putBoolean(KEY_PAIRED, paired)
            .apply();
    }
} 