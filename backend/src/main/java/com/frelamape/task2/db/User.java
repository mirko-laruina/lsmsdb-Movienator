package com.frelamape.task2.db;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class User {
    private ObjectId id;
    private String username;
    private String password;
    private String email;
    private Boolean isAdmin;
    private List<Statistics<Person>> ratedActors = new ArrayList<>();
    private List<Statistics<Person>> ratedDirectors = new ArrayList<>();
    private List<Statistics<Genre>> ratedGenres = new ArrayList<>();
    private List<Session> sessions = new ArrayList<>();

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
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public List<Statistics<Person>> getRatedActors() {
        return ratedActors;
    }

    public void setRatedActors(List<Statistics<Person>> ratedActors) {
        this.ratedActors = ratedActors;
    }

    public List<Statistics<Person>> getRatedDirectors() {
        return ratedDirectors;
    }

    public void setRatedDirectors(List<Statistics<Person>> ratedDirectors) {
        this.ratedDirectors = ratedDirectors;
    }

    public List<Statistics<Genre>> getRatedGenres() {
        return ratedGenres;
    }

    public void setRatedGenres(List<Statistics<Genre>> ratedGenres) {
        this.ratedGenres = ratedGenres;
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
            user.setEmail(d.getString("email"));

            Object ratedActorObj = d.get("rated_actors");
            if (ratedActorObj != null){
                List<Document> actorDocs = (List<Document>) ratedActorObj;
                List<Statistics<Person>> actors = new ArrayList<>();
                for (Document actorDocument:actorDocs){
                    actors.add(Statistics.Adapter.fromDBObject(
                            actorDocument,
                            Person.class
                    ));
                }
                user.setRatedActors(actors);
            }

            Object ratedDirectorObj = d.get("rated_actors");
            if (ratedDirectorObj != null){
                List<Document> directorDocs = (List<Document>) ratedDirectorObj;
                List<Statistics<Person>> directors = new ArrayList<>();
                for (Document directorDocument:directorDocs){
                    directors.add(Statistics.Adapter.fromDBObject(
                            directorDocument,
                            Person.class
                    ));
                }
                user.setRatedDirectors(directors);
            }

            Object ratedGenreObj = d.get("rated_genres");
            if (ratedGenreObj != null){
                List<Document> genreDocs = (List<Document>) ratedGenreObj;
                List<Statistics<Genre>> genres = new ArrayList<>();
                for (Document genreDocument:genreDocs){
                    genres.add(Statistics.Adapter.fromDBObject(
                            genreDocument,
                            Genre.class
                    ));
                }
                user.setRatedGenres(genres);
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
    }
}
