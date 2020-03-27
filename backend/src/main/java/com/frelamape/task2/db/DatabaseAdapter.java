package com.frelamape.task2.db;

import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

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

    public DatabaseAdapter(String connectionURI, String dbName){
        mongoClient = MongoClients.create(connectionURI);
        database = mongoClient.getDatabase(dbName);
        moviesCollection = database.getCollection("movies");
        usersCollection = database.getCollection("users");
        ratingsCollection = database.getCollection("ratings");
    }

    public boolean updateInternalRating(){
        return true;
    }

    /**
     * Inserts a rating.
     *
     * @param rating the rating to be inserted
     * @return 0 rating was added
     *         1 rating was updated
     *         -10 if unknown error
     *         -20 if unknown error in rating update
     */
    public int insertRating(Rating rating){
        Document ratingBson = Rating.Adapter.toDBObject(rating);
        UpdateOptions options = new UpdateOptions();
        options.upsert(true);

        UpdateResult result = ratingsCollection.updateOne(
                 eq("_id", ratingBson.get("_id")),
                 combine(
                         set("rating", rating.getRating()),
                         set("date", rating.getDate())
                 ),
                 options
        );

        if (result.wasAcknowledged()){
            if (updateInternalRating()){
                return result.getUpsertedId() != null ? 0 : 1;
            } else {
                return -20;
            }
        } else{
            return -10;
        }
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

    public QuerySubset<RatingExtended> getAllRatings(int n, int page){
        List<Rating> ratings = Rating.Adapter.fromDBObjectIterable(ratingsCollection
                .find()
                .skip(n*(page-1))
                .limit(n)
        );
        List<RatingExtended> ratingsExtended = new ArrayList<>();
        for (Rating r: ratings){
            User u = getUserById(r.getUserId());
            Movie m = getMovieDetails(r.getMovieId());
            if (u != null && m != null){
                ratingsExtended.add(new RatingExtended(m, u, r));
            }
        }

        return new QuerySubset<>(
               ratingsExtended,
               ratingsCollection.countDocuments()
        );
    }

    public QuerySubset<Rating> getUserRatings(User u, int n, int page){
        Bson filter = eq("_id.user_id", u.getId());
        return new QuerySubset<>(
                Rating.Adapter.fromDBObjectIterable(ratingsCollection
                        .find(filter)
                        .skip(n*(page-1))
                        .limit(n)
                ),
                ratingsCollection.countDocuments(filter)
        );
    }

    public Rating getUserRating(User u, Movie m){
        return Rating.Adapter.fromDBObject(
                ratingsCollection.find(
                        and(eq("_id.user_id", u.getId()),
                            eq("_id.movie_id", m.getId()))
                ).first()
        );
    }

    public User getUserLoginInfo(String usernameORemail){
        return User.Adapter.fromDBObject(
                usersCollection.find(
                        or(
                                eq("username", usernameORemail),
                                eq("email", usernameORemail)
                        )
                ).projection(
                        include("_id", "username", "email", "password")
                ).first()
        );
    }

    public User getUserProfile(String username){
        return User.Adapter.fromDBObject(
                usersCollection.find(
                        eq("username", username)
                ).first()
        );
        // TODO user statistics
    }

    public User getUserById(ObjectId userId){
        User u = User.Adapter.fromDBObject(
                usersCollection.find(
                        eq("_id", userId)
                )
                .first());
        return u;
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

    public User getUserFromSession(Session s){
        Document userDocument = usersCollection.find(
                and(
                        eq("sessions._id", s.getId())
                ))
                .first();
        if (userDocument == null)
            return null;

        return User.Adapter.fromDBObject(userDocument);
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

    public QuerySubset<Movie> getMovieList(String sortBy, int sortOrder, double minRating,
                                    double maxRating, String director, String actor,
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

        if (director != null && !director.isEmpty()){
            List<Bson> directorConditions = new ArrayList<>();
            for (String s:director.split(" ")){
                directorConditions.add(regex("directors.name", s, "i"));
            }
            conditions.add(and(directorConditions.toArray(new Bson[]{})));
        }

        if (actor  != null && !actor.isEmpty()){
            List<Bson> actorConditions = new ArrayList<>();
            for (String s:actor.split(" ")){
                actorConditions.add(regex("actors.actor_name", s, "i"));
            }
            conditions.add(and(actorConditions.toArray(new Bson[]{})));
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

        Bson filters;

        if (!conditions.isEmpty()){
            filters = and(conditions.toArray(new Bson[]{}));

        } else {
            filters = new BsonDocument();
        }

        FindIterable<Document> movieIterable = moviesCollection
                .find(filters)
                .sort(sorting)
                .projection(include("title", "year", "poster", "genres", "total_rating", "description"))
                .skip(n*(page-1))
                .limit(n);

        return new QuerySubset<>(
                Movie.Adapter.fromDBObjectIterable(movieIterable),
                moviesCollection.countDocuments(filters)
        );
    }

    public Movie getMovieDetails(String id){
        return Movie.Adapter.fromDBObject(
                moviesCollection.find(eq("_id", id)).first()
        );
    }

    public QuerySubset<Movie> searchMovie(String query, int n, int page){
        Bson filter = text("\""+ query + "\"");
        FindIterable<Document> movieIterable = moviesCollection
                .find(filter)
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return new QuerySubset<>(
                Movie.Adapter.fromDBObjectIterable(movieIterable),
                moviesCollection.countDocuments(filter)
        );
    }

    public List<Statistics<Statistics.Aggregator>> getStatistics(String groupBy, String sortBy, int sortOrder, int n, int page){
        // TODO
        return null;
    }

    public ObjectId addUser(User u){
        // TODO name exceptions to return meaningful error message to user

        // check unique
        // NB: not unique => not banned

        Document userInDB = usersCollection.find(
                or(
                    eq("username", u.getUsername()),
                    eq("email", u.getEmail())
                )
        ).first();

        if (userInDB != null){
            return null;
        }

        InsertOneResult result = usersCollection.insertOne(User.Adapter.toDBObject(u));

        if (result.getInsertedId() != null) {
            return result.getInsertedId().asObjectId().getValue();
        } else{
            return null;
        }
    }

    public boolean banUser(User u){
        // NB: User must contain ID

        UpdateResult result = usersCollection.updateOne(
                eq("_id", u.getId()),
                set("isbanned", true)
        );

        if (result.wasAcknowledged() && result.getMatchedCount() == 1) {
            // delete user ratings
            ratingsCollection.deleteMany(eq("user_id", u.getId()));
            return true;
        } else{
            return false;
        }
    }

    public User authUser(User u){
        Document userDocument = usersCollection.find(
                and(eq("username", u.getUsername()),
                        eq("password", u.getPassword())
                ))
                .first();
        if (userDocument == null)
            return null;
        else
            return User.Adapter.fromDBObject(userDocument);
    }

    public QuerySubset<User> searchUser(String query, int n, int page){
        Bson filter = text("\""+ query + "\"");
        FindIterable<Document> userIterable = usersCollection
                .find(filter)
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return new QuerySubset<>(
                User.Adapter.fromDBObjectIterable(userIterable),
                usersCollection.countDocuments(filter));
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
}
