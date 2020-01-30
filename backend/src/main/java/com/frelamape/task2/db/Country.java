package com.frelamape.task2.db;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Country implements Statistics.Aggregator {
    private String name;

    public Country(){}

    public Country(String name) {
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
        public static Country fromDBObject (Document d){
            if (d == null)
                return null;

            Country country = new Country();
            country.fromDBObject(d);
            return country;
        }

        public static List<Country> fromDBObjectIterable(Iterable<Document> documents){
            List<Country> countries = new ArrayList<>();
            for(Document d:documents){
                countries.add(fromDBObject(d));
            }
            return countries;
        }
    }
}
