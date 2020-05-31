package com.frelamape.task2.db;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Movie {
    private String id;
    private String title;
    private String originalTitle;
    private String originalLanguage;
    private Integer runtime;
    private List<String> countries;
    private Integer year;
    private Date date;
    private String description;
    private String storyline;
    private String tagline;
    private String poster;
    private String mpaa;
    private Integer budget;
    private Integer gross;
    private List<Character> characters;
    private List<Person> directors;
    private List<String> genres;
    private List<AggregatedRating> ratings;
    private Double totalRating;
    private Double userRating;

    public Movie(String id) {
        this.id = id;
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

    public List<String> getCountries() {
        return countries;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public String getStoryline() {
        return storyline;
    }

    public void setStoryline(String storyline) {
        this.storyline = storyline;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getMpaa() {
        return mpaa;
    }

    public void setMpaa(String mpaa) {
        this.mpaa = mpaa;
    }

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }

    public Integer getGross() {
        return gross;
    }

    public void setGross(Integer gross) {
        this.gross = gross;
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

            Movie movie = new Movie(d.getString("_id"));
            movie.setTitle(d.getString("title"));
            movie.setOriginalTitle(d.getString("original_title"));
            movie.setOriginalLanguage(d.getString("original_language"));
            movie.setRuntime(BsonAutoCast.asInteger(d, "runtime"));
            movie.setYear(BsonAutoCast.asInteger(d, "year"));
            movie.setCountries(d.getList("countries", String.class));
            movie.setGenres(d.getList("genres", String.class));
            movie.setRuntime(BsonAutoCast.asInteger(d, "runtime"));
            movie.setDate(BsonAutoCast.asDate(d,"date"));
            movie.setDescription(d.getString("description"));
            movie.setTagline(d.getString("tagline"));
            movie.setStoryline(d.getString("storyline"));
            movie.setBudget(BsonAutoCast.asInteger(d, "budget"));
            movie.setGross(BsonAutoCast.asInteger(d, "gross"));
            movie.setPoster(d.getString("poster"));
            movie.setTotalRating(BsonAutoCast.asDouble(d, "total_rating"));

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

            Object ratingsObj = d.get("ratings");
            if (ratingsObj != null){
                List<Document> ratingsDocList = (List<Document>) ratingsObj;
                List<AggregatedRating> ratings = new ArrayList<>();
                for (Document doc:ratingsDocList){
                    AggregatedRating ar = AggregatedRating.Adapter.fromDBObject(doc);
                    if (ar.getAvgRating() != null)
                        ratings.add(ar);
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
