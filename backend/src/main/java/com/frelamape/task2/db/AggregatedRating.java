package com.frelamape.task2.db;

import org.bson.Document;

import java.util.Date;

public class AggregatedRating {
    private String source;
    private Double avgRating;
    private Integer count;
    private Double weight;
    private Date lastUpdate;

    public AggregatedRating(String source, Double avgRating, Integer count, Double weight, Date lastUpdate) {
        this.source = source;
        this.avgRating = avgRating;
        this.count = count;
        this.weight = weight;
        this.lastUpdate = lastUpdate;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public static class Adapter {
        public static AggregatedRating fromDBObject (Document d){
            if (d == null)
                return null;

            return new AggregatedRating(
                    d.getString("source"),
                    BsonAutoCast.asDouble(d, "avgrating"),
                    BsonAutoCast.asInteger(d, "count"),
                    BsonAutoCast.asDouble(d, "weight"),
                    d.getDate("last_update")
            );
        }

        public static Document toDBObject (AggregatedRating r){
            if (r == null)
                return null;

            Document d = new Document();
            d.append("source", r.getSource());
            d.append("avgrating", r.getAvgRating());
            d.append("count", r.getCount());
            d.append("weight", r.getWeight());
            d.append("last_update", r.getLastUpdate());
            return d;
        }
    }
}
