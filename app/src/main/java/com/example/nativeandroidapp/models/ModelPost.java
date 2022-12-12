package com.example.nativeandroidapp.models;

public class ModelPost {
    String pId, pTitle, pDescription, pImage,pTime, uid, uEmail, uDp, uName , pLikes
                , pComments , uIdLikes;
    boolean isEnable = false;
    public ModelPost(){}

    public ModelPost(String pId, String pTitle, String pDescription, String pImage, String pTime, String uid, String uEmail, String uDp, String uName, String pLikes, String pComments, String uIdLikes, boolean isEnable) {
        this.pId = pId;
        this.pTitle = pTitle;
        this.pDescription = pDescription;
        this.pImage = pImage;
        this.pTime = pTime;
        this.uid = uid;
        this.uEmail = uEmail;
        this.uDp = uDp;
        this.uName = uName;
        this.pLikes = pLikes;
        this.pComments = pComments;
        this.uIdLikes = uIdLikes;
        this.isEnable = isEnable;
    }


    @Override
    public String toString() {
        return "ModelPost{" +
                "pId='" + pId + '\'' +
                ", pTitle='" + pTitle + '\'' +
                ", pDescription='" + pDescription + '\'' +
                ", pImage='" + pImage + '\'' +
                ", pTime='" + pTime + '\'' +
                ", uid='" + uid + '\'' +
                ", uEmail='" + uEmail + '\'' +
                ", uDp='" + uDp + '\'' +
                ", uName='" + uName + '\'' +
                ", pLikes='" + pLikes + '\'' +
                ", pComments='" + pComments + '\'' +
                ", uIdLikes='" + uIdLikes + '\'' +
                ", isEnable=" + isEnable +
                '}';
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitle() {
        return pTitle;
    }

    public void setpTitle(String pTitle) {
        this.pTitle = pTitle;
    }

    public String getpDescription() {
        return pDescription;
    }

    public void setpDescription(String pDescription) {
        this.pDescription = pDescription;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuDp() {
        return uDp;
    }

    public void setuDp(String uDp) {
        this.uDp = uDp;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }

    public String getuIdLikes() {
        return uIdLikes;
    }

    public void setuIdLikes(String uIdLikes) {
        this.uIdLikes = uIdLikes;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }
}
