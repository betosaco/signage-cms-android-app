package com.matmax.signage;

import android.content.Context;
import android.util.Log;
import com.matmax.signage.graphql.GetPlaylistQuery;
import com.matmax.signage.network.GraphQLManager;
import java.util.ArrayList;
import java.util.List;

public class ContentManager {
    private static ContentManager instance;
    private final List<ContentItem> playlist = new ArrayList<>();
    private int currentIndex = 0;
    private final Context context;

    private ContentManager(Context context) {
        this.context = context.getApplicationContext();
    }
    public static synchronized ContentManager getInstance(Context context) {
        if (instance == null) {
            instance = new ContentManager(context.getApplicationContext());
        }
        return instance;
    }
    public void fetchContentFromBackend(String deviceId) {
        GraphQLManager.getInstance(context).getPlaylist(deviceId, new GraphQLManager.ApolloCallback<GetPlaylistQuery.Data>() {
            @Override
            public void onSuccess(GetPlaylistQuery.Data data) {
                if (data != null && data.getPlaylist() != null && data.getPlaylist().getItems() != null) {
                    playlist.clear();
                    for (GetPlaylistQuery.Item item : data.getPlaylist().getItems()) {
                        ContentItem.Type type;
                        switch (item.getType().toUpperCase()) {
                            case "WEB":
                                type = ContentItem.Type.WEB;
                                break;
                            case "IMAGE":
                                type = ContentItem.Type.IMAGE;
                                break;
                            case "VIDEO":
                                type = ContentItem.Type.VIDEO;
                                break;
                            default:
                                type = ContentItem.Type.WEB;
                        }
                        playlist.add(new ContentItem(type, item.getUrl(), null));
                    }
                    currentIndex = 0;
                    Log.i("ContentManager", "Playlist updated from backend");
                } else {
                    Log.e("ContentManager", "Failed to fetch playlist: empty response");
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("ContentManager", "Error fetching playlist", e);
            }
        });
    }
    public ContentItem getCurrentContent() {
        if (playlist.isEmpty()) return null;
        return playlist.get(currentIndex);
    }
    public ContentItem getNextContent() {
        if (playlist.isEmpty()) return null;
        currentIndex = (currentIndex + 1) % playlist.size();
        return playlist.get(currentIndex);
    }
    public void resetPlaylist() {
        currentIndex = 0;
    }
    public List<ContentItem> getPlaylist() {
        return playlist;
    }
    public boolean isContentCached(ContentItem item) {
        // TODO: Check if content is cached locally
        return false;
    }
}

// ContentItem is a placeholder for your content model
class ContentItem {
    public enum Type { WEB, VIDEO, IMAGE }
    public Type type;
    public String url;
    public String localPath;
    public ContentItem(Type type, String url, String localPath) {
        this.type = type;
        this.url = url;
        this.localPath = localPath;
    }
} 