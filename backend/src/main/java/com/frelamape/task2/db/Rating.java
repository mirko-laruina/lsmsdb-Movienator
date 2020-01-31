package com.frelamape.task2.db;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Rating {
    private ObjectId userId;
    private String movieId;
    private Date date;
    private Double rating;

    public Rating(ObjectId userId, String movieId, Date date, Double rating) {
        this.userId = userId;
        this.movieId = movieId;
        this.date = date;
        this.rating = rating;
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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public static class Adapter {
        public static Rating fromDBObject (Document d){
            if (d == null)
                return null;

            Document id = (Document) d.get("_id");

            Rating rating = new Rating(
                    id.getObjectId("user_id"),
                    id.getString("movie_id"),
                    d.getDate("date"),
                    d.getDouble("rating")
            );
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
            Document id = new Document();
            id.append("user_id", rating.getUserId());
            id.append("movie_id", rating.getMovieId());

            Document d = new Document();
            d.append("_id", id);
            d.append("date", rating.getDate());
            d.append("rating", rating.getRating());

            return d;
        }
    }
}
