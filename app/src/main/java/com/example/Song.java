package com.example;

import com.squareup.moshi.Json;

public class Song {
    @Json(name = "trackId")
    private long trackId;
    
    @Json(name = "trackName")
    private String trackName;
    
    @Json(name = "artistName")
    private String artistName;
    
    @Json(name = "collectionName")
    private String collectionName;
    
    @Json(name = "artworkUrl100")
    private String artworkUrl100;
    
    @Json(name = "previewUrl")
    private String previewUrl;

    @Json(name = "primaryGenreName")
    private String primaryGenreName;

    public Song() {}

    public Song(long trackId, String trackName, String artistName, String collectionName, String artworkUrl100, String previewUrl) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.artistName = artistName;
        this.collectionName = collectionName;
        this.artworkUrl100 = artworkUrl100;
        this.previewUrl = previewUrl;
    }

    public long getTrackId() { return trackId; }
    public void setTrackId(long trackId) { this.trackId = trackId; }

    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }

    public String getArtworkUrl100() { return artworkUrl100; }
    public void setArtworkUrl100(String artworkUrl100) { this.artworkUrl100 = artworkUrl100; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public String getPrimaryGenreName() { return primaryGenreName; }
    public void setPrimaryGenreName(String primaryGenreName) { this.primaryGenreName = primaryGenreName; }
}
