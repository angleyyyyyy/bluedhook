package com.zjfgh.bluedhook.simple.module;

import androidx.annotation.NonNull;

public class VisitorUser {
    private long uid;
    private String name;
    private String avatar;
    private int age;
    private int height;
    private int weight;
    private int vbadge;
    private String role;
    private double distance;
    private double latitude;
    private double longitude;
    private int location;
    private int vipGrade;
    private int isShadow;
    private int isVipAnnual;
    private int vipExpLvl;
    private int isCall;
    private String description;
    private int onlineState;
    private long visitorsTime;
    private int visitorsCnt;
    private int isHideDistance;
    private int isHideVipLook;
    private int isHideLastOperate;
    private long lastOperate;

    // 构造函数
    public VisitorUser() {
    }

    // Getter和Setter方法
    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getVbadge() {
        return vbadge;
    }

    public void setVbadge(int vbadge) {
        this.vbadge = vbadge;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public int getVipGrade() {
        return vipGrade;
    }

    public void setVipGrade(int vipGrade) {
        this.vipGrade = vipGrade;
    }

    public int getIsShadow() {
        return isShadow;
    }

    public void setIsShadow(int isShadow) {
        this.isShadow = isShadow;
    }

    public int getIsVipAnnual() {
        return isVipAnnual;
    }

    public void setIsVipAnnual(int isVipAnnual) {
        this.isVipAnnual = isVipAnnual;
    }

    public int getVipExpLvl() {
        return vipExpLvl;
    }

    public void setVipExpLvl(int vipExpLvl) {
        this.vipExpLvl = vipExpLvl;
    }

    public int getIsCall() {
        return isCall;
    }

    public void setIsCall(int isCall) {
        this.isCall = isCall;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOnlineState() {
        return onlineState;
    }

    public void setOnlineState(int onlineState) {
        this.onlineState = onlineState;
    }

    public long getVisitorsTime() {
        return visitorsTime;
    }

    public void setVisitorsTime(long visitorsTime) {
        this.visitorsTime = visitorsTime;
    }

    public int getVisitorsCnt() {
        return visitorsCnt;
    }

    public void setVisitorsCnt(int visitorsCnt) {
        this.visitorsCnt = visitorsCnt;
    }

    public int getIsHideDistance() {
        return isHideDistance;
    }

    public void setIsHideDistance(int isHideDistance) {
        this.isHideDistance = isHideDistance;
    }

    public int getIsHideVipLook() {
        return isHideVipLook;
    }

    public void setIsHideVipLook(int isHideVipLook) {
        this.isHideVipLook = isHideVipLook;
    }

    public int getIsHideLastOperate() {
        return isHideLastOperate;
    }

    public void setIsHideLastOperate(int isHideLastOperate) {
        this.isHideLastOperate = isHideLastOperate;
    }

    public long getLastOperate() {
        return lastOperate;
    }

    public void setLastOperate(long lastOperate) {
        this.lastOperate = lastOperate;
    }

    @NonNull
    @Override
    public String toString() {
        return "VisitorUser{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", distance=" + distance +
                ", onlineState=" + onlineState +
                '}';
    }
}
