package com.xagu.xxb.bean;

import java.io.Serializable;

/**
 * Created by XAGU on 2020/3/15
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class Active implements Serializable {
    private String id;
    private String name;
    private String activeType;
    private String time;
    private String status;
    private String url;
    private String cover_url;
    private String activeTypeName;
    private boolean isSigned;
    private String signCode;

    @Override
    public String toString() {
        return "Active{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", activeType='" + activeType + '\'' +
                ", time='" + time + '\'' +
                ", status='" + status + '\'' +
                ", url='" + url + '\'' +
                ", cover_url='" + cover_url + '\'' +
                ", activeTypeName='" + activeTypeName + '\'' +
                ", isSigned=" + isSigned +
                ", signCode='" + signCode + '\'' +
                '}';
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveType() {
        return activeType;
    }

    public void setActiveType(String activeType) {
        this.activeType = activeType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCover_url() {
        return cover_url;
    }

    public void setCover_url(String cover_url) {
        this.cover_url = cover_url;
    }

    public String getActiveTypeName() {
        return activeTypeName;
    }

    public void setActiveTypeName(String activeTypeName) {
        this.activeTypeName = activeTypeName;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(boolean signed) {
        isSigned = signed;
    }
}
