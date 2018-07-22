package com.github.sumimakito.judian;

import java.util.ArrayList;
import java.util.List;

public class Card {
    public enum Type {
        Incoming, Outgoing
    }

    public boolean added = false;
    public Type type;
    public List<Attachment> attachments;
    public String title;
    public String content;
    public String username;
    public String uuid;
    public String ts;

    public double[] latlon;
    public boolean liked = false;
    public int likeCount = 0;

    public Card() {
        attachments = new ArrayList<>();
        likeCount = (int) Math.round(Math.random() * 10);
        latlon = new double[2];
    }
}
