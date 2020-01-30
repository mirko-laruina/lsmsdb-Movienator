package com.frelamape.task2.db;

import org.bson.Document;

public class Genre implements Statistics.Aggregator {
    private String name;

    public Genre(){}

    public Genre(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void fromDBObject(Document d) {
        name = d.getString("_id");
    }

    public static class Adapter {
        public static Genre fromDBObject (Document d){
            if (d == null)
                return null;

            Genre genre = new Genre();
            genre.fromDBObject(d);
            return genre;
        }
    }
}
