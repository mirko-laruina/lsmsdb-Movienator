package com.frelamape.task2.db;

import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Statistics<T extends Statistics.Aggregator> {
    public interface Aggregator{
        Object getId();
        String getName();
        void fromDBObject(Document d);
    }

    private T aggregator;
    private Double avgRating;
    private Integer movieCount;

    public Statistics(T aggregator, Double avgRating, Integer movieCount) {
        this.aggregator = aggregator;
        this.avgRating = avgRating;
        this.movieCount = movieCount;
    }

    public T getAggregator() {
        return aggregator;
    }

    public void setAggregator(T aggregator) {
        this.aggregator = aggregator;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public Integer getMovieCount() {
        return movieCount;
    }

    public void setMovieCount(Integer movieCount) {
        this.movieCount = movieCount;
    }

    public static class Adapter {
        public static Statistics fromDBObject (Document d, Class<? extends Aggregator> tClass){
            if (d == null)
                return null;

            Aggregator aggregator = null;
            try {
                aggregator = tClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not instantiate given class");
            }

            aggregator.fromDBObject(d);
            return new Statistics(
                    aggregator,
                    d.getDouble("avg_rating"),
                    d.getInteger("movie_count")
            );
        }

        public static List<Statistics<Aggregator>> fromDBObjectIterable(Iterable<Document> documents,
                                                                        Class<? extends Aggregator> tClass){
            List<Statistics<Aggregator>> statistics = new ArrayList<>();
            for(Document d:documents){
                statistics.add(fromDBObject(d, tClass));
            }
            return statistics;
        }
    }
}
