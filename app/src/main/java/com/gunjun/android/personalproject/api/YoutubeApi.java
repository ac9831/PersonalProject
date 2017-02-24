package com.gunjun.android.personalproject.api;

import android.app.Activity;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.gunjun.android.personalproject.BuildConfig;
import com.gunjun.android.personalproject.R;
import com.gunjun.android.personalproject.interfaces.YouTubeVideosReceiver;
import com.gunjun.android.personalproject.models.YouTubeVideo;
import com.gunjun.android.personalproject.models.Youtube;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by gunjunLee on 2017-02-17.
 */

public class YoutubeApi {

    private YouTube youtube;
    private String appName;
    private Activity activity;
    private Realm realm;
    private YouTubeVideosReceiver youTubeVideosReceiver;

    public YoutubeApi(Activity activity, Realm realm) {
        this.activity = activity;
        this.appName =  activity.getResources().getString(R.string.app_name);
        this.realm = realm;
    }

    public void setYouTubeVideosReceiver(YouTubeVideosReceiver youTubeVideosReceiver) {
        this.youTubeVideosReceiver = youTubeVideosReceiver;
    }

    public void searchVideos() {

        new Thread() {
            public void run() {
                try {
                    youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request) throws IOException {

                        }
                    }).setApplicationName(appName).build();
                    realm = Realm.getDefaultInstance();
                    List<Youtube> youtubeList = realm.where(Youtube.class).findAll();
                    ArrayList<YouTubeVideo> resultList = new ArrayList<YouTubeVideo>();
                    for(int i=0; i<youtubeList.size(); i++) {
                        YouTube.Search.List searchList;
                        searchList = youtube.search().list("id,snippet");
                        searchList.setKey(BuildConfig.GOOGLE_API_KEY);
                        searchList.setChannelId(youtubeList.get(i).getChannelName());
                        searchList.setType("video");
                        searchList.setMaxResults(50L);
                        searchList.setFields("items(id/videoId,snippet/title,snippet/thumbnails/default/url)");

                        SearchListResponse searchListResponse = searchList.execute();
                        List<SearchResult> searchResults = searchListResponse.getItems();

                        StringBuilder contentDetails = new StringBuilder();

                        int ii = 0;
                        for (SearchResult result : searchResults) {
                            contentDetails.append(result.getId().getVideoId());
                            if (ii < 49)
                                contentDetails.append(",");
                            ii++;
                        }

                        ArrayList<YouTubeVideo> items = new ArrayList<>();
                        for (int j = 0; j < searchResults.size(); j++) {
                            YouTubeVideo item = new YouTubeVideo();
                            item.setTitle(searchResults.get(j).getSnippet().getTitle());
                            item.setThumbnailURL(searchResults.get(j).getSnippet().getThumbnails().getDefault().getUrl());
                            item.setId(searchResults.get(j).getId().getVideoId());
                            item.setDuration("NA");

                            items.add(item);
                        }
                        resultList.addAll(items);
                    }
                    youTubeVideosReceiver.onVideosReceived(resultList);
                } catch (IOException e) {
                    Log.e("tag", "Could not initialize: " + e);
                    e.printStackTrace();
                    return;
                }
            }
        }.start();
    }

}
