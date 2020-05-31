package com.frelamape.task2.db;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Date;

public class AggregatedRating {
    private String source;
    private Double avgRating;
    private transient Double sum;
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
        if (avgRating != null)
            return avgRating;
        else if (count != 0)
            return sum/count;
        else
            return null;
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

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
        if (sum != null && count!= null){ // sum overrides any value found in average
            if (count != 0)
                avgRating = sum/count;
            else
                avgRating = null;
        }
    }

    public static class Adapter {
        public static AggregatedRating fromDBObject (Document d){
            if (d == null)
                return null;

            AggregatedRating rating = new AggregatedRating(
                    d.getString("source"),
                    BsonAutoCast.asDouble(d, "avgrating"),
                    BsonAutoCast.asInteger(d, "count"),
                    BsonAutoCast.asDouble(d, "weight"),
                    d.getDate("last_update")
            );

            if (d.containsKey("sum")){ // internal rating
                rating.setSum(BsonAutoCast.asDouble(d, "sum"));
            }

            return rating;
        }

        public static Document toDBObject (AggregatedRating r){
            if (r == null)
                return null;

            Document d = new Document();
            d.append("source", r.getSource());
            d.append("avgrating", r.getAvgRating());
            d.append("sum", r.getSum());
            d.append("count", r.getCount());
            d.append("weight", r.getWeight());
            d.append("last_update", r.getLastUpdate());
            return d;
        }
    }
}
