package com.frelamape.task2.db;

import org.bson.types.ObjectId;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RatingExtended extends Rating {
    private String title;
    private String username;
    private Integer year;

    public RatingExtended(Movie m, User u, Rating r) {
        super(r.getUserId(), r.getMovieId(), r.getDate(), r.getRating());
        this.title = m.getTitle();
        this.year = m.getYear();
        this.username = u.getUsername();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public static class Adapter{
        public static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        public static RatingExtended fromNeo4jRecord(Record record){
            Movie m = new Movie(
                    record.get("movie_id").asString()
            );
            ;
            m.setTitle(record.get("title").asString());
            m.setYear(record.get("year").asInt());
            m.setPoster(record.get("poster").asString());
            User u = new User(record.get("username").asString());
            u.setId(new ObjectId(record.get("user_id").asString()));
            Rating r = new Rating(u, m, record.get("rating").asDouble());
            try{
                r.setDate(ISO8601.parse(record.get("date").asString()));
            } catch (Exception e){
                e.printStackTrace();
            }
            return new RatingExtended(m, u, r);
        }

        public static List<RatingExtended> fromNeo4jResult(Result result){
            List<RatingExtended> list = new ArrayList<>();
            while(result.hasNext()){
                Record record = result.next();
                list.add(fromNeo4jRecord(record));
            }
            return list;
        }
    }
}
