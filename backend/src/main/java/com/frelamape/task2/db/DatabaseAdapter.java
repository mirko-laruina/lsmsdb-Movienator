package com.frelamape.task2.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;

public class DatabaseAdapter {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> moviesCollection;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> ratingsCollection;
    private MongoCollection<Document> actorsCollection;
    private MongoCollection<Document> directorsCollection;
    private MongoCollection<Document> yearsCollection;
    private MongoCollection<Document> genresCollection;
    private MongoCollection<Document> countriesCollection;
    private MongoCollection<Document> bannedUsersCollection;

    public DatabaseAdapter(String connectionURI, String dbName){
        mongoClient = new MongoClient(new MongoClientURI(connectionURI));
        database = mongoClient.getDatabase(dbName);
        moviesCollection = database.getCollection("movies");
        usersCollection = database.getCollection("users");
        ratingsCollection = database.getCollection("ratings");
        actorsCollection = database.getCollection("actors");
        directorsCollection = database.getCollection("directors");
        yearsCollection = database.getCollection("years");
        genresCollection = database.getCollection("genres");
        countriesCollection = database.getCollection("countries");
        bannedUsersCollection = database.getCollection("banned_users");
    }

    public void insertRating(Rating rating){
        ratingsCollection.insertOne(Rating.Adapter.toDBObject(rating));
    }

    public boolean updateRating(Rating rating){
        UpdateResult res = ratingsCollection.updateOne(
                and(eq("_id.movie_id", rating.getMovieId()),
                        eq("_id.user_id", rating.getUserId())),
                combine(set("date", rating.getDate()),
                        set("rating", rating.getRating())
                )
        );
        return res.getMatchedCount() == 1;
    }

    public boolean deleteRating(Rating rating){
        DeleteResult res = ratingsCollection.deleteOne(
                and(eq("_id.movie_id", rating.getMovieId()),
                        eq("_id.user_id", rating.getUserId()))
        );
        return res.getDeletedCount() == 1;
    }

    public List<Rating> getAllRatings(int n, int page){
        return Rating.Adapter.fromDBObjectIterable(
                ratingsCollection
                        .find()
                        .skip(n*(page-1))
                        .limit(n)
        );
    }

    public List<Rating> getUserRatings(User u){
        return Rating.Adapter.fromDBObjectIterable(
                ratingsCollection.find(
                        eq("_id.user_id", u.getId())
                )
        );
    }

    public User getUserLoginInfo(String username){
        return User.Adapter.fromDBObject(
                usersCollection.find(
                        eq("username", username)
                ).projection(
                        include("username", "email", "password")
                ).first()
        );
    }

    public User getUserProfile(String username){
        return User.Adapter.fromDBObject(
                usersCollection.find(
                        eq("username", username)
                ).first()
        );
    }

    public boolean addSession(User u, Session s){
        UpdateResult result = usersCollection.updateOne(
                eq("_id", u.getId()),
                push("sessions", Session.Adapter.toDBObject(s))
        );
        return result.wasAcknowledged();
    }

    public boolean existsSession(User u, Session s){
        Document userDocument = usersCollection.find(
                and(eq("_id", u.getId()),
                    eq("sessions._id", s.getId())
                ))
                .projection(
                        include("sessions.$")
                )
                .first();
        if (userDocument == null)
            return false;

        Object sessionsObj = userDocument.get("sessions");
        if (sessionsObj == null)
            return false;

        List<Document> sessions = (List<Document>) sessionsObj;
        if (sessions.isEmpty())
            return false;

        Session dbSession = Session.Adapter.fromDBObject(sessions.get(0));
        if (dbSession.getExpiry().before(new Date())) {
            removeSession(u, s);
            return false;
        }
        return true;
    }

    public boolean removeSession(User u, Session s){
        UpdateResult result = usersCollection.updateOne(
                eq("_id", u.getId()),
                pull("sessions", eq("_id", s.getId()))
        );
        return result.wasAcknowledged();
    }

    public boolean updateSession(User u, Session s){
        UpdateResult result = usersCollection.updateOne(
                and(eq("_id", u.getId()),
                    eq("sessions._id", s.getId())),
                set("sessions.$.expiry", s.getExpiry())
        );
        return result.wasAcknowledged();
    }

    public List<Movie> getMovieList(String sortBy, int sortOrder, double minRating,
                                    double maxRating, String directorId, String actorId,
                                    String country, int fromYear, int toYear, String genre,
                                    int n, int page){

        Bson sorting;
        if (sortOrder == 1){
            sorting = ascending(sortBy);
        } else if (sortOrder == -1){
            sorting = descending(sortBy);
        } else{
            throw new RuntimeException("sortOrder must be 1 or -1.");
        }

        List<Bson> conditions = new ArrayList<>();
        if (minRating != -1){
            conditions.add(gte("total_rating", minRating));
        }

        if (maxRating != -1){
            conditions.add(lte("total_rating", maxRating));
        }

        if (directorId != null){
            conditions.add(eq("directors.id", directorId));
        }

        if (actorId  != null){
            conditions.add(eq("actors.actor_id", actorId));
        }

        if (country != null && !country.isEmpty()){
            conditions.add(eq("country", country));
        }

        if (fromYear != -1){
            conditions.add(gte("year", fromYear));
        }

        if (toYear != -1){
            conditions.add(lte("year", toYear));
        }

        if (genre != null && !genre.isEmpty()){
            conditions.add(eq("genres", genre));
        }

        FindIterable<Document> movieIterable = moviesCollection
                .find(and(conditions.toArray(new Bson[]{})))
                .sort(sorting)
                .projection(include("title", "year", "poster", "genres", "total_rating"))
                .skip(n*(page-1))
                .limit(n);

        return Movie.Adapter.fromDBObjectIterable(movieIterable);
    }

    public Movie getMovieDetails(String id){
        return Movie.Adapter.fromDBObject(
                moviesCollection.find(eq("_id", id)).first()
        );
    }

    public List<Movie> searchMovie(String query, int n, int page){
        FindIterable<Document> movieIterable = moviesCollection
                .find(text("\""+ query + "\""))
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return Movie.Adapter.fromDBObjectIterable(movieIterable);
    }

    public List<Statistics<Statistics.Aggregator>> getStatistics(String groupBy, String sortBy, int sortOrder, int n, int page){
        MongoCollection<Document> collection;
        Class<? extends Statistics.Aggregator> aggregatorClass;

        switch (groupBy){
            case "country":
                collection = countriesCollection;
                aggregatorClass = Country.class;
                break;
            case "year":
                collection = yearsCollection;
                aggregatorClass = Year.class;
                break;
            case "director":
                collection = directorsCollection;
                aggregatorClass = Person.class;
                break;
            case "actor":
                collection = actorsCollection;
                aggregatorClass = Person.class;
                break;
            default:
                throw new RuntimeException("Not recognized groupBy: " + groupBy);
        }

        Bson sorting;
        if (sortOrder == 1){
            sorting = ascending(sortBy);
        } else if (sortOrder == -1){
            sorting = descending(sortBy);
        } else{
            throw new RuntimeException("sortOrder must be 1 or -1.");
        }

        FindIterable<Document> statisticsIterable = collection
                .find()
                .sort(sorting)
                .skip(n*(page-1))
                .limit(n);

        return Statistics.Adapter.fromDBObjectIterable(statisticsIterable, aggregatorClass);
    }

    public List<Person> searchActor(String query, int n, int page){
        FindIterable<Document> actorIterable = actorsCollection
                .find(text("\""+ query + "\""))
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return Person.Adapter.fromDBObjectIterable(actorIterable);
    }

    public List<Person> searchDirector(String query, int n, int page){
        FindIterable<Document> actorIterable = directorsCollection
                .find(text("\""+ query + "\""))
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return Person.Adapter.fromDBObjectIterable(actorIterable);
    }

    public List<Person> searchCountry(String query, int n, int page){
        FindIterable<Document> actorIterable = countriesCollection
                .find(text("\""+ query + "\""))
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return Person.Adapter.fromDBObjectIterable(actorIterable);
    }

    public void addUser(User u){
        usersCollection.insertOne(User.Adapter.toDBObject(u));
    }

    public boolean banUser(User u){
        User dbUser = User.Adapter.fromDBObject(
                usersCollection.findOneAndDelete(eq("username", u.getUsername()))
        );
        if (dbUser == null)
            return false;

        bannedUsersCollection.insertOne(User.Adapter.toDBObject(dbUser));

        ratingsCollection.deleteMany(eq("user_id", dbUser.getId()));

        return true;
    }

    public List<User> searchUser(String query, int n, int page){
        FindIterable<Document> userIterable = usersCollection
                .find(text("\""+ query + "\""))
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return User.Adapter.fromDBObjectIterable(userIterable);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getMoviesCollection() {
        return moviesCollection;
    }

    public MongoCollection<Document> getUsersCollection() {
        return usersCollection;
    }

    public MongoCollection<Document> getRatingsCollection() {
        return ratingsCollection;
    }

    public MongoCollection<Document> getActorsCollection() {
        return actorsCollection;
    }

    public MongoCollection<Document> getDirectorsCollection() {
        return directorsCollection;
    }

    public MongoCollection<Document> getYearsCollection() {
        return yearsCollection;
    }

    public MongoCollection<Document> getGenresCollection() {
        return genresCollection;
    }

    public MongoCollection<Document> getCountriesCollection() {
        return countriesCollection;
    }

    public MongoCollection<Document> getBannedUsersCollection() {
        return bannedUsersCollection;
    }
}
