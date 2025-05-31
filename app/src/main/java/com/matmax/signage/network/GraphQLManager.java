package com.matmax.signage.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.apollographql.apollo3.ApolloClient;
import com.apollographql.apollo3.api.ApolloResponse;
import com.apollographql.apollo3.exception.ApolloException;
import com.matmax.signage.graphql.GetPlaylistQuery;
import com.matmax.signage.graphql.RegisterDeviceMutation;
import com.matmax.signage.graphql.SendHeartbeatMutation;
import com.matmax.signage.graphql.GetRemoteCommandsQuery;
import com.matmax.signage.graphql.UpdateDeviceConfigMutation;
import com.matmax.signage.graphql.IsDevicePairedQuery;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import com.apollographql.apollo3.api.Optional;

public class GraphQLManager {
    private static GraphQLManager instance;
    private ApolloClient apolloClient;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private GraphQLManager(Context context) {
        apolloClient = new ApolloClient.Builder()
            .serverUrl("https://your-graphql-endpoint.com/graphql") // TODO: Replace with your endpoint
            .build();
    }

    public static synchronized GraphQLManager getInstance(Context context) {
        if (instance == null) {
            instance = new GraphQLManager(context.getApplicationContext());
        }
        return instance;
    }

    public void getPlaylist(String deviceId, ApolloCallback<GetPlaylistQuery.Data> callback) {
        executor.execute(() -> {
            try {
                ApolloResponse<GetPlaylistQuery.Data> response = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, cont) -> apolloClient.query(new GetPlaylistQuery(deviceId)).execute(cont));
                mainHandler.post(() -> callback.onSuccess(response.data));
            } catch (ApolloException | InterruptedException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void registerDevice(String pairingCode, String deviceInfo, ApolloCallback<RegisterDeviceMutation.Data> callback) {
        executor.execute(() -> {
            try {
                RegisterDeviceMutation mutation = new RegisterDeviceMutation(pairingCode, deviceInfo != null ? Optional.present(deviceInfo) : Optional.absent());
                ApolloResponse<RegisterDeviceMutation.Data> response = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, cont) -> apolloClient.mutation(mutation).execute(cont));
                mainHandler.post(() -> callback.onSuccess(response.data));
            } catch (ApolloException | InterruptedException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void sendHeartbeat(String deviceId, String status, String timestamp, ApolloCallback<SendHeartbeatMutation.Data> callback) {
        executor.execute(() -> {
            try {
                SendHeartbeatMutation mutation = new SendHeartbeatMutation(deviceId, status != null ? Optional.present(status) : Optional.absent(), timestamp != null ? Optional.present(timestamp) : Optional.absent());
                ApolloResponse<SendHeartbeatMutation.Data> response = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, cont) -> apolloClient.mutation(mutation).execute(cont));
                mainHandler.post(() -> callback.onSuccess(response.data));
            } catch (ApolloException | InterruptedException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void getRemoteCommands(String deviceId, ApolloCallback<GetRemoteCommandsQuery.Data> callback) {
        executor.execute(() -> {
            try {
                ApolloResponse<GetRemoteCommandsQuery.Data> response = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, cont) -> apolloClient.query(new GetRemoteCommandsQuery(deviceId)).execute(cont));
                mainHandler.post(() -> callback.onSuccess(response.data));
            } catch (ApolloException | InterruptedException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void updateDeviceConfig(String deviceId, String configJson, ApolloCallback<UpdateDeviceConfigMutation.Data> callback) {
        executor.execute(() -> {
            try {
                UpdateDeviceConfigMutation mutation = new UpdateDeviceConfigMutation(deviceId, configJson);
                ApolloResponse<UpdateDeviceConfigMutation.Data> response = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, cont) -> apolloClient.mutation(mutation).execute(cont));
                mainHandler.post(() -> callback.onSuccess(response.data));
            } catch (ApolloException | InterruptedException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public void isDevicePaired(String pairingCode, ApolloCallback<IsDevicePairedQuery.Data> callback) {
        executor.execute(() -> {
            try {
                ApolloResponse<IsDevicePairedQuery.Data> response = BuildersKt.runBlocking(Dispatchers.getIO(), (scope, cont) -> apolloClient.query(new IsDevicePairedQuery(pairingCode)).execute(cont));
                mainHandler.post(() -> callback.onSuccess(response.data));
            } catch (ApolloException | InterruptedException e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public interface ApolloCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }
} 