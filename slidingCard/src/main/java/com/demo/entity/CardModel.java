package com.demo.entity;

/**
 * Created by zhangdroid on 2016/10/12.
 */
public class CardModel {
    String nickname;
    int age;
    String location;
    String thumbnailUrl;
    int imgCnt;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getImgCnt() {
        return imgCnt;
    }

    public void setImgCnt(int imgCnt) {
        this.imgCnt = imgCnt;
    }

    @Override
    public String toString() {
        return "CardModel{" +
                "nickname='" + nickname + '\'' +
                ", age='" + age + '\'' +
                ", location='" + location + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", imgCnt=" + imgCnt +
                '}';
    }

}
