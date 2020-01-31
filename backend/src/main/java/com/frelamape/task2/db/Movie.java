package com.frelamape.task2.db;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Movie {
    private String id;
    private String title;
    private String originalTitle;
    private Integer runtime;
    private String country;
    private Integer year;
    private Date date;
    private String description;
    private String poster;
    private List<Character> characters;
    private List<Person> directors;
    private List<String> genres;
    private List<AggregatedRating> ratings;
    private Double totalRating;
    private Double userRating;

    public Movie(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }

    public List<Person> getDirectors() {
        return directors;
    }

    public void setDirectors(List<Person> directors) {
        this.directors = directors;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<AggregatedRating> getRatings() {
        return ratings;
    }

    public void setRatings(List<AggregatedRating> ratings) {
        this.ratings = ratings;
    }

    public Double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(Double totalRating) {
        this.totalRating = totalRating;
    }

    public Double getUserRating() {
        return userRating;
    }

    public void setUserRating(Double userRating) {
        this.userRating = userRating;
    }

    public static class Adapter {
        public static Movie fromDBObject (Document d){
            if (d == null)
                return null;

            Movie movie = new Movie(d.getString("title"));
            movie.setId(d.getString("_id"));
            movie.setOriginalTitle(d.getString("original_title"));
            movie.setRuntime(d.getInteger("runtime"));
            movie.setCountry(d.getString("country"));
            movie.setYear(d.getInteger("year"));
            movie.setDate(d.getDate("date"));
            movie.setDescription(d.getString("description"));
            movie.setPoster(d.getString("poster"));
            movie.setTotalRating(d.getDouble("total_rating"));

            Object charactersObj = d.get("characters");
            if (charactersObj != null){
                List<Document> charactersDocList = (List<Document>) charactersObj;
                List<Character> characters = new ArrayList<>();
                for (Document doc:charactersDocList){
                    characters.add(Character.Adapter.fromDBObject(doc));
                }
                movie.setCharacters(characters);
            }

            Object directorsObj = d.get("directors");
            if (directorsObj != null){
                List<Document> directorsDocList = (List<Document>) directorsObj;
                List<Person> directors = new ArrayList<>();
                for (Document doc:directorsDocList){
                    directors.add(Person.Adapter.fromDBObject(doc));
                }
                movie.setDirectors(directors);
            }


            Object genresObj = d.get("genres");
            if (genresObj != null){
                List<String> genres = (List<String>) genresObj;
                movie.setGenres(genres);
            }

            Object ratingsObj = d.get("ratings");
            if (ratingsObj != null){
                List<Document> ratingsDocList = (List<Document>) ratingsObj;
                List<AggregatedRating> ratings = new ArrayList<>();
                for (Document doc:ratingsDocList){
                    ratings.add(AggregatedRating.Adapter.fromDBObject(doc));
                }
                movie.setRatings(ratings);
            }

            return movie;
        }

        public static List<Movie> fromDBObjectIterable(Iterable<Document> documents){
            List<Movie> movies = new ArrayList<>();
            for(Document d:documents){
                movies.add(fromDBObject(d));
            }
            return movies;
        }
    }
}
