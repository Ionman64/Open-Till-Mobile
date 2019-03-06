package com.opentill;

import java.util.Comparator;

public class Supplier {

    public String id = new String();
    public String name = new String();

    public Supplier(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Comparator<? super Supplier> compare = new Comparator<Supplier>() {
        @Override
        public int compare(Supplier s1, Supplier s2) {
            return s1.getName().compareTo(s2.getName());
        }
    };


    public String toString() {
        return this.name;
    }


}
