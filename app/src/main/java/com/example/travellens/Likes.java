package com.example.travellens;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.Serializable;

@ParseClassName("Likes")
public class Likes extends ParseObject implements Serializable {
    public static final String KEY_USER = "userId";
    public static final String KEY_POST = "postId";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }
    public void setUser(ParseUser de) {
        put(KEY_USER, de);
    }

    public ParseObject getPost() {
        return getParseObject(KEY_POST);
    }
    public void setPost(ParseObject de) {
        put(KEY_POST, de);
    }
}
