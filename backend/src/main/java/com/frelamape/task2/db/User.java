package com.frelamape.task2.db;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;

public class User {
    private transient ObjectId id;
    private String username;
    private transient String password;
    private String email;
    private Boolean isAdmin;
    private Boolean isBanned;
    private Boolean follower;
    private Boolean following;
    private List<Statistics<Statistics.Aggregator>> favouriteActors = null;
    private List<Statistics<Statistics.Aggregator>> favouriteDirectors = null;
    private List<Statistics<Statistics.Aggregator>> favouriteGenres = null;
    private transient List<Session> sessions = new ArrayList<>();

    public User(String username) {
        this.username = username;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isAdmin() {
        if (isAdmin == null)
            return false;
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
    public Boolean isBanned() {
        if (isBanned == null)
            return false;
        return isBanned;
    }

    public void setBanned(Boolean banned) {
        isBanned = banned;
    }

    public List<Statistics<Statistics.Aggregator>> getFavouriteActors() {
        return favouriteActors;
    }

    public void setFavouriteActors(List<Statistics<Statistics.Aggregator>> favouriteActors) {
        this.favouriteActors = favouriteActors;
    }

    public List<Statistics<Statistics.Aggregator>> getFavouriteDirectors() {
        return favouriteDirectors;
    }

    public void setFavouriteDirectors(List<Statistics<Statistics.Aggregator>> favouriteDirectors) {
        this.favouriteDirectors = favouriteDirectors;
    }

    public List<Statistics<Statistics.Aggregator>> getFavouriteGenres() {
        return favouriteGenres;
    }

    public void setFavouriteGenres(List<Statistics<Statistics.Aggregator>> favouriteGenres) {
        this.favouriteGenres = favouriteGenres;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public void addSession(Session session){
        sessions.add(session);
    }

    public void removeSession(Session session){
        sessions.remove(session);
    }

    public Boolean isFollower() {
        return follower;
    }

    public void setFollower(Boolean follower) {
        this.follower = follower;
    }

    public Boolean isFollowing() {
        return following;
    }

    public void setFollowing(Boolean following) {
        this.following = following;
    }

    public static class Adapter {
        public static User fromDBObject (Document d){
            if (d == null)
                return null;

            User user = new User(
                    d.getString("username")
            );
            user.setId(d.getObjectId("_id"));
            user.setPassword(d.getString("password"));
            user.setEmail(d.getString("email"));
            user.setAdmin(d.getBoolean("isAdmin", false));
            user.setBanned(d.getBoolean("isBanned", false));
            user.setEmail(d.getString("email"));

            Object ratedActorObj = d.get("rated_actors");
            if (ratedActorObj != null){
                List<Document> actorDocs = (List<Document>) ratedActorObj;
                List<Statistics<Statistics.Aggregator>> actors = new ArrayList<>();
                for (Document actorDocument:actorDocs){
                    actors.add(Statistics.Adapter.fromDBObject(
                            actorDocument,
                            Person.class
                    ));
                }
                user.setFavouriteActors(actors);
            }

            Object ratedDirectorObj = d.get("rated_actors");
            if (ratedDirectorObj != null){
                List<Document> directorDocs = (List<Document>) ratedDirectorObj;
                List<Statistics<Statistics.Aggregator>> directors = new ArrayList<>();
                for (Document directorDocument:directorDocs){
                    directors.add(Statistics.Adapter.fromDBObject(
                            directorDocument,
                            Person.class
                    ));
                }
                user.setFavouriteDirectors(directors);
            }

            Object ratedGenreObj = d.get("rated_genres");
            if (ratedGenreObj != null){
                List<Document> genreDocs = (List<Document>) ratedGenreObj;
                List<Statistics<Statistics.Aggregator>> genres = new ArrayList<>();
                for (Document genreDocument:genreDocs){
                    genres.add(Statistics.Adapter.fromDBObject(
                            genreDocument,
                            Genre.class
                    ));
                }
                user.setFavouriteGenres(genres);
            }

            Object sessionsObj = d.get("sessions");
            if (sessionsObj != null){
                List<Document> sessionDocs = (List<Document>) sessionsObj;
                List<Session> sessions = new ArrayList<>();
                for (Document sessionDocument:sessionDocs){
                    sessions.add(Session.Adapter.fromDBObject(sessionDocument));
                }
                user.setSessions(sessions);
            }

            return user;
        }

        public static List<User> fromDBObjectIterable(Iterable<Document> documents){
            List<User> users = new ArrayList<>();
            for(Document d:documents){
                users.add(fromDBObject(d));
            }
            return users;
        }

        public static Document toDBObject(User u){
            Document d = new Document();

            if (u.getId() != null)
                d.append("_id", u.getId());

            d.append("username", u.getUsername());
            d.append("email", u.getEmail());
            d.append("password", u.getPassword());

            return d;
        }

        public static User fromNeo4jRecord(Record record){
            User u = new User(record.get("username").asString());
            u.setId(new ObjectId(record.get("_id").asString()));

            if (record.containsKey("following"))
                u.setFollowing(record.get("following").asBoolean());

            if (record.containsKey("follower"))
                u.setFollower(record.get("follower").asBoolean());

            return u;
        }

        public static List<User> fromNeo4jResult(Result result){
            List<User> list = new ArrayList<>();
            while(result.hasNext()){
                Record record = result.next();
                list.add(fromNeo4jRecord(record));
            }
            return list;
        }
    }

    public static class Relationship{
        public boolean following;
        public boolean follower;
    }
}
