package com.frelamape.task2.db;

import org.bson.Document;

public class Year implements Statistics.Aggregator {
    private Integer year;

    public Year(){}

    public Year(Integer year) {
        this.year = year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public Object getId() {
        return year;
    }

    @Override
    public String getName() {
        return Integer.toString(year);
    }

    @Override
    public void fromDBObject(Document d) {
        year = d.getInteger("_id");
    }

    public static class Adapter {
        public static Year fromDBObject (Document d){
            if (d == null)
                return null;

            Year year = new Year();
            year.fromDBObject(d);
            return year;
        }
    }
}
