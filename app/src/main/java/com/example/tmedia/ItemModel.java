package com.example.tmedia;

public class ItemModel {
    private long id;
    private String title;
    private int timeDuration;
    private String artist;

    public ItemModel(long id, String title, int timeDuration, String Artist) {
        this.id = id;
        this.title = title;
        this.timeDuration = timeDuration;
        this.artist = Artist;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(int timeDuration) {
        this.timeDuration = timeDuration;
    }
}
