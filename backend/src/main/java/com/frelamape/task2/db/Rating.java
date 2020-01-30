package com.frelamape.task2.db;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Rating {
    private ObjectId id;
    private ObjectId userId;
    private String movieId;
    private Date date;
    private double rating;

    public Rating(ObjectId userId, String movieId, Date date, double rating) {
        this.userId = userId;
        this.movieId = movieId;
        this.date = date;
        this.rating = rating;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public static class Adapter {
        public static Rating fromDBObject (Document d){
            if (d == null)
                return null;

            Rating  rating = new Rating(
                    d.getObjectId("user_id"),
                    d.getString("movie_id"),
                    d.getDate("date"),
                    d.getDouble("rating")
            );
            rating.setId(d.getObjectId("_id"));
            return rating;
        }

        public static List<Rating> fromDBObjectIterable(Iterable<Document> documents){
            List<Rating> ratings = new ArrayList<>();
            for(Document d:documents){
                ratings.add(fromDBObject(d));
            }
            return ratings;
        }

        public static Document toDBObject(Rating rating){
            Document d = new Document();

            if (rating.getId() != null)
                d.append("_id", rating.getId());

            d.append("user_id", rating.getUserId());
            d.append("movie_id", rating.getMovieId());
            d.append("date", rating.getDate());
            d.append("rating", rating.getRating());

            return d;
        }
    }
}
