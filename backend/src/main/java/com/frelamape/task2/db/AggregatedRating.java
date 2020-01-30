package com.frelamape.task2.db;

import org.bson.Document;

public class AggregatedRating {
    private String source;
    private double avgRating;
    private long count;
    private double weight;

    public AggregatedRating(String source, double avgRating, long count, double weight) {
        this.source = source;
        this.avgRating = avgRating;
        this.count = count;
        this.weight = weight;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public static class Adapter {
        public static AggregatedRating fromDBObject (Document d){
            if (d == null)
                return null;

            return new AggregatedRating(
                    d.getString("source"),
                    d.getDouble("avg_rating"),
                    d.getInteger("count"),
                    d.getDouble("weight")
            );
        }
    }
}
