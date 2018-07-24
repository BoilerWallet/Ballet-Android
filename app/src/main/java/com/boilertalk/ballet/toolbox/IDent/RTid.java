package com.boilertalk.ballet.toolbox.IDent;

//gives out runtime ids
public class RTid {
    private static long nextId = 0;
    private long id;

    public RTid() {
        id = nextId;
        nextId++;
    }
    public long getId() {
        return id;
    }

    public static long getNextId() {
        return nextId;
    }
}
