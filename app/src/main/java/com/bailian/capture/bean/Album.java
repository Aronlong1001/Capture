package com.bailian.capture.bean;

import java.util.ArrayList;

public class Album extends RisoBean {


    private String albumUri;
    private String title;
    private ArrayList<PhotoItem> photos;
    private int maxCount;

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public Album(String title, String uri, ArrayList<PhotoItem> photos) {
        this.title = title;
        this.albumUri = uri;
        this.photos = photos;
    }

    public String getAlbumUri() {
        return albumUri;
    }

    public void setAlbumUri(String albumUri) {
        this.albumUri = albumUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<PhotoItem> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<PhotoItem> photos) {
        this.photos = photos;
    }

//    @Override
//    public int hashCode() {
//        if (albumUri == null) {
//            return super.hashCode();
//        } else {
//            return albumUri.hashCode();
//        }
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o != null && (o instanceof Album)) {
//            return equals(albumUri, ((Album) o).getAlbumUri());
//        }
//        return false;
//    }

    public static boolean equals(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

}
