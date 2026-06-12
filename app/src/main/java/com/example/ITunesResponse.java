package com.example;

import com.squareup.moshi.Json;
import java.util.List;

public class ITunesResponse {
    @Json(name = "resultCount")
    private int resultCount;

    @Json(name = "results")
    private List<Song> results;

    public ITunesResponse() {}

    public ITunesResponse(int resultCount, List<Song> results) {
        this.resultCount = resultCount;
        this.results = results;
    }

    public int getResultCount() { return resultCount; }
    public void setResultCount(int resultCount) { this.resultCount = resultCount; }

    public List<Song> getResults() { return results; }
    public void setResults(List<Song> results) { this.results = results; }
}
