package com.frelamape.task2.db;

import org.bson.Document;

public class AggregatedRating {
    private String source;
    private Double avgRating;
    private Integer count;
    private Double weight;

    public AggregatedRating(String source, Double avgRating, Integer count, Double weight) {
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

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public static class Adapter {
        public static AggregatedRating fromDBObject (Document d){
            if (d == null)
                return null;

            return new AggregatedRating(
                    d.getString("source"),
                    BsonAutoCast.asDouble(d, "avgrating"),
                    BsonAutoCast.asInteger(d, "count"),
                    BsonAutoCast.asDouble(d, "weight")
            );
        }
    }
}
