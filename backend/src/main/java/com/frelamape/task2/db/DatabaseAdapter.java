package com.frelamape.task2.db;

import com.mongodb.MongoWriteException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
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

    private boolean updateTotalRating(String movieId){
        Movie m = getMovieDetails(movieId);

        if (!m.getRatings().isEmpty()) {
            double sum = 0;
            for (AggregatedRating r : m.getRatings()) {
                sum += r.getAvgRating() * r.getWeight();
            }
            double totalRating = sum/m.getRatings().size();
            UpdateResult result = moviesCollection.updateOne(
                    eq("_id", movieId),
                    set("total_rating", totalRating)
            );
            return result.wasAcknowledged();
        } else{
            return true;
        }

    }

    private boolean updateInternalRating(String movieId){
        AggregateIterable<Document> iterable = ratingsCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(eq("_id.movie_id", movieId)),
                        Aggregates.group(
                                "$_id.movie_id",
                                Accumulators.sum("total", "$rating"),
                                Accumulators.sum("count", 1)
                        )

                )
        );

        Document doc = iterable.first();
        if (doc == null){
            return false;
        }
        Double total = BsonAutoCast.asDouble(doc, "total");
        Integer count = BsonAutoCast.asInteger(doc, "count");

        if (total == null)
            total = 0.0;
        if (count == null)
            count = 0;

        double avgrating;

        if (count != 0) {
            avgrating = total / count;
            AggregatedRating rating = new AggregatedRating(
                    "internal",
                    avgrating,
                    count,
                    1.0,
                    new Date()
            );
            BulkWriteResult result = moviesCollection.bulkWrite(Arrays.asList(
                    new UpdateOneModel<>(
                            eq("_id", movieId),
                            pull("ratings", eq("source", "internal"))
                    ),
                    new UpdateOneModel<>(
                            eq("_id", movieId),
                            push("ratings", AggregatedRating.Adapter.toDBObject(rating))
                    )
            ));

            if (result.wasAcknowledged()){
                return updateTotalRating(movieId);
            } else{
                return false;
            }
        } else {
            return true;
        }
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
            if (updateInternalRating(rating.getMovieId())){
                return result.getUpsertedId() != null ? 0 : 1;
            } else {
                return -20;
            }
        } else{
            return -10;
        }
    }

    public boolean deleteRating(Rating rating){
        DeleteResult res = ratingsCollection.deleteOne(
                and(eq("_id.movie_id", rating.getMovieId()),
                        eq("_id.user_id", rating.getUserId()))
        );
        if (res.wasAcknowledged() && res.getDeletedCount() == 1){
            return updateInternalRating(rating.getMovieId());
        } else {
            return false;
        }
    }

    /**
     * TODO: rewrite using aggregations
     */
    private List<RatingExtended> fillRatingExtended(List<Rating> ratings){
        List<RatingExtended> ratingsExtended = new ArrayList<>();
        for (Rating r: ratings){
            User u = getUserById(r.getUserId(), "username");
            Movie m = getMovieDetails(r.getMovieId(), "title", "year", "total_rating");
            if (u != null && m != null){
                ratingsExtended.add(new RatingExtended(m, u, r));
            }
        }

        return ratingsExtended;
    }

    public QuerySubset<RatingExtended> getAllRatings(int n, int page){
        List<Rating> ratings = Rating.Adapter.fromDBObjectIterable(ratingsCollection
                .find()
                .skip(n*(page-1))
                .limit(n)
        );

        return new QuerySubset<>(
                fillRatingExtended(ratings),
               ratingsCollection.countDocuments()
        );
    }

    public QuerySubset<RatingExtended> getUserRatings(User u, int n, int page){
        Bson filter = eq("_id.user_id", u.getId());
        List<Rating> ratings = Rating.Adapter.fromDBObjectIterable(ratingsCollection
                .find(filter)
                .skip(n*(page-1))
                .limit(n)
        );

        return new QuerySubset<>(
                fillRatingExtended(ratings),
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
        User u = User.Adapter.fromDBObject(
                usersCollection.find(
                        eq("username", username)
                ).first()
        );

        if (u == null){
            return null;
        }

        final int FIELD_NAME = 0;
        final int GROUPBY_ID = 1;
        final int GROUPBY_NAME = 2;

        String[] actors = new String[]{"movies.characters", "$movies.characters.actor_id", "$movies.characters.actor_name"};
        String[] directors = new String[]{"movies.directors", "$movies.directors.id", "$movies.directors.name"};
        String[] genres = new String[]{"movies.genres", "$movies.genres", "$movies.genres"};

        for (String[] field:new String[][]{actors, directors, genres}) {
            AggregateIterable<Document> iterable = ratingsCollection.aggregate(Arrays.asList(
                    Aggregates.match(
                            eq("_id.user_id", u.getId())
                    ),
                    Aggregates.lookup(
                            "movies",
                            "_id.movie_id",
                            "_id",
                            "movies"
                    ),
                    Aggregates.project(
                            include(
                                    "rating",
                                    "movies._id",
                                    field[FIELD_NAME]
                            )
                    ),
                    Aggregates.unwind("$movies"),
                    Aggregates.unwind("$" + field[FIELD_NAME]),
                    Aggregates.group(
                            field[GROUPBY_ID],
                            Accumulators.first("name", field[GROUPBY_NAME]),
                            Accumulators.avg("avg_rating", "$rating"),
                            Accumulators.sum("movie_count", 1)
                    ),
                    Aggregates.sort(descending("avg_rating")),
                    Aggregates.limit(3)
            ));

            if (field == actors){
                u.setFavouriteActors(Statistics.Adapter.fromDBObjectIterable(iterable, Person.class));
            } else if (field == directors){
                u.setFavouriteDirectors(Statistics.Adapter.fromDBObjectIterable(iterable, Person.class));
            } else if (field == genres){
                u.setFavouriteGenres(Statistics.Adapter.fromDBObjectIterable(iterable, Genre.class));
            }
        }

        return u;
    }

    public User getUserById(ObjectId userId, String... fields){
        Document document;

        if (fields.length != 0){
            document = usersCollection
                    .find(
                        eq("_id", userId)
                    ).projection(include(fields))
                    .first();
        } else {
            document = usersCollection
                    .find(
                        eq("_id", userId)
                    ).first();
        }
        User u = User.Adapter.fromDBObject(document);
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

    public Movie getMovieDetails(String id, String... fields){
        Document document;
        if (fields.length != 0) {
            document = moviesCollection
                    .find(eq("_id", id))
                    .projection(include(fields))
                    .first();
        } else{
            document = moviesCollection
                    .find(eq("_id", id))
                    .first();
        }
        return Movie.Adapter.fromDBObject(document);
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
