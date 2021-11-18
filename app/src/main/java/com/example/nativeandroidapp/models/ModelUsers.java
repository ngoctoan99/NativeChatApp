package com.example.nativeandroidapp.models;

public class ModelUsers {
    String name ,email, image ,phone ,cover, search, uid;

    public ModelUsers(String name, String email, String image, String phone, String cover, String search, String uid) {
        this.name = name;
        this.email = email;
        this.image = image;
        this.phone = phone;
        this.cover = cover;
        this.search = search;
        this.uid = uid;
    }
    public ModelUsers(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
