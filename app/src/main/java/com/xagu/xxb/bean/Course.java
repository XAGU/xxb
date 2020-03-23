package com.xagu.xxb.bean;

import java.io.Serializable;

/**
 * Created by XAGU on 2020/3/15
 * Email:xagu_qc@foxmail.com
 * Describe: TODO
 */
public class Course implements Serializable {
    private String courseId;
    private String teacher;
    private String imageUrl;
    private String name;
    private String classId;
    private String classname;
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", teacher='" + teacher + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", name='" + name + '\'' +
                ", classId='" + classId + '\'' +
                ", classname='" + classname + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
