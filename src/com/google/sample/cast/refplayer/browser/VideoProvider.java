/*
 * Copyright (C) 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cast.refplayer.browser;

import com.google.sample.cast.refplayer.utils.MediaItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoProvider {

    private static final String TAG = "VideoProvider";
    private static final String TAG_VIDEOS = "videos";
    private static final String TAG_HLS = "hls";
    private static final String TAG_DASH = "dash";
    private static final String TAG_MP4 = "mp4";
    private static final String TAG_IMAGES = "images";
    private static final String TAG_VIDEO_TYPE = "type";
    private static final String TAG_VIDEO_URL = "url";
    private static final String TAG_VIDEO_MIME = "mime";

    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_NAME = "name";
    private static final String TAG_STUDIO = "studio";
    private static final String TAG_SOURCES = "sources";
    private static final String TAG_SUBTITLE = "subtitle";
    private static final String TAG_DURATION = "duration";
    private static final String TAG_THUMB = "image-480x270"; // "thumb";
    private static final String TAG_IMG_780_1200 = "image-780x1200";
    private static final String TAG_TITLE = "title";

    public static final String KEY_DESCRIPTION = "description";

    private static final String TARGET_FORMAT = TAG_MP4;
    private static List<MediaItem> mediaList;

    protected JSONObject parseUrl(String urlString) {
        InputStream is = null;
        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"), 1024);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            return new JSONObject(json);
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static List<MediaItem> buildMedia(String url) throws JSONException {

        if (null != mediaList) {
            return mediaList;
        }
        Map<String, String> urlPrefixMap = new HashMap<>();
        mediaList = new ArrayList<>();
        JSONObject jsonObj = new VideoProvider().parseUrl(url);
        JSONArray categories = jsonObj.getJSONArray(TAG_CATEGORIES);
        if (null != categories) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                urlPrefixMap.put(TAG_HLS, category.getString(TAG_HLS));
                urlPrefixMap.put(TAG_DASH, category.getString(TAG_DASH));
                urlPrefixMap.put(TAG_MP4, category.getString(TAG_MP4));
                urlPrefixMap.put(TAG_IMAGES, category.getString(TAG_IMAGES));
                category.getString(TAG_NAME);
                JSONArray videos = category.getJSONArray(TAG_VIDEOS);
                if (null != videos) {
                    for (int j = 0; j < videos.length(); j++) {
                        String videoUrl = null;
                        String mimeType = null;
                        JSONObject video = videos.getJSONObject(j);
                        String subTitle = video.getString(TAG_SUBTITLE);
                        JSONArray videoSpecs = video.getJSONArray(TAG_SOURCES);
                        if (null == videoSpecs || videoSpecs.length() == 0) {
                            continue;
                        }
                        for (int k = 0; k < videoSpecs.length(); k++) {
                            JSONObject videoSpec = videoSpecs.getJSONObject(k);
                            if (TARGET_FORMAT.equals(videoSpec.getString(TAG_VIDEO_TYPE))) {
                                videoUrl = urlPrefixMap.get(TARGET_FORMAT) + videoSpec
                                        .getString(TAG_VIDEO_URL);
                                mimeType = videoSpec.getString(TAG_VIDEO_MIME);
                            }
                        }
                        if (videoUrl == null) {
                            continue;
                        }
                        String imageUrl = urlPrefixMap.get(TAG_IMAGES) + video.getString(TAG_THUMB);
                        String bigImageUrl = urlPrefixMap.get(TAG_IMAGES) + video
                                .getString(TAG_IMG_780_1200);
                        String title = video.getString(TAG_TITLE);
                        String studio = video.getString(TAG_STUDIO);
                        int duration = video.getInt(TAG_DURATION);
                        mediaList.add(buildMediaInfo(title, studio, subTitle, duration, videoUrl,
                                mimeType, imageUrl, bigImageUrl));
                    }
                }
            }
        }
        return mediaList;
    }

    private static MediaItem buildMediaInfo(String title, String studio, String subTitle,
            int duration, String url, String mimeType, String imgUrl, String bigImageUrl) {
        MediaItem media = new MediaItem();
        media.setUrl(url);
        media.setTitle(title);
        media.setSubTitle(subTitle);
        media.setStudio(studio);
        media.addImage(imgUrl);
        media.addImage(bigImageUrl);
        media.setContentType(mimeType);
        media.setDuration(duration);

        return media;
    }

}


