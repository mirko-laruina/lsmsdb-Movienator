package com.frelamape.task2.db;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.WriteConcern;
import com.mongodb.ReadPreference;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;

@Component
public class DatabaseAdapter {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> moviesCollection;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> usersCollectionMajorityWrite;
    private MongoCollection<Document> usersCollectionPrimaryRead;
    private MongoCollection<Document> ratingsCollection;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAdapter.class);

    @Value("${com.frelamape.task2.db.DatabaseAdapter.connectionURI}")
    private String connectionURI;
    @Value("${com.frelamape.task2.db.DatabaseAdapter.dbName}")
    private String dbName;

    @Autowired
    private DatabaseTaskExecutor executor;

    @Autowired
    private ApplicationArguments args;

    @PostConstruct
    public void init() {
        if (args.getSourceArgs().length >= 2) {
            connectionURI = args.getSourceArgs()[1];
            dbName = args.getSourceArgs()[0];
        }

        logger.info("Connecting to Mongo at " + connectionURI + " " + dbName);

        mongoClient = MongoClients.create(connectionURI);
        database = mongoClient.getDatabase(dbName);
        moviesCollection = database.getCollection("movies").withReadPreference(ReadPreference.nearest());
        usersCollection = database.getCollection("users").withReadPreference(ReadPreference.nearest());
        usersCollectionMajorityWrite = database.getCollection("users").withWriteConcern(WriteConcern.MAJORITY);
        usersCollectionPrimaryRead = database.getCollection("users").withReadPreference(ReadPreference.primary());
        ratingsCollection = database.getCollection("ratings").withReadPreference(ReadPreference.nearest());
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
    public void insertRating(Rating rating){
        Document ratingBson = Rating.Adapter.toDBObject(rating);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
        options.upsert(true);

        Document oldRatingDoc = ratingsCollection.findOneAndUpdate(
                 eq("_id", ratingBson.get("_id")),
                 combine(
                         set("rating", rating.getRating()),
                         set("date", rating.getDate())
                 ),
                 options
        );

        Rating oldRating = Rating.Adapter.fromDBObject(oldRatingDoc);
        executor.updateInternalRating(oldRating, rating);
    }

    /**
     * Deletes the given rating from the database.
     *
     * @param rating the rating to be removed. Only the movieId and userId
     *               fields are used
     * @return True if the update was successful, false otherwise.
     */
    public boolean deleteRating(Rating rating){
        Document ratingDoc = ratingsCollection.findOneAndDelete(
                eq("_id", rating.getId())
        );
        rating = Rating.Adapter.fromDBObject(ratingDoc);
        executor.updateInternalRating(rating, null);
        return rating != null;
    }

    /**
     * Deletes multiple ratings from the database.
     *
     * @param ratings the list of ratings to be removed. Only the movieId and userId
     *                fields are used-
     * @return True if the update was successful, false otherwise.
     */
    public boolean deleteRatings(List<Rating> ratings){
        List<Document> ratingIds = new ArrayList<>();

        for (Rating r:ratings){
            ratingIds.add(r.getId());
        }

        DeleteResult res = ratingsCollection.deleteMany(
                in("_id", ratingIds)
        );

        for (Rating r:ratings){
            executor.updateInternalRating(r, null);
        }

        return res.getDeletedCount() == ratings.size();
    }

    /**
     * Adds information about user and movie to the given ratings.
     *
     * @param ratings the list of ratings
     * @return a list of RatingExtended that contains the supplementary information.
     */
    private List<RatingExtended> fillRatingExtended(List<Rating> ratings){
        List<RatingExtended> ratingsExtended = new ArrayList<>();
        Set<ObjectId> userIds = new HashSet<>();
        Set<String> movieIds = new HashSet<>();

        for (Rating r:ratings){
            userIds.add(r.getUserId());
            movieIds.add(r.getMovieId());
        }

        FindIterable<Document> userIterable = usersCollection.find(
            in("_id", userIds)
        ).projection(include("username"));
        List<User> users = User.Adapter.fromDBObjectIterable(userIterable);

        FindIterable<Document> movieIterable = moviesCollection.find(
            in("_id", movieIds)
        ).projection(include("title", "year", "total_rating"));
        List<Movie> movies = Movie.Adapter.fromDBObjectIterable(movieIterable);

        for (Rating r: ratings){
            User u = null;
            for (User user:users){
                if (user.getId().equals(r.getUserId())){
                    u = user;
                    break;
                }
            }

            Movie m = null;
            for (Movie movie:movies){
                if (movie.getId().equals(r.getMovieId())){
                    m = movie;
                    break;
                }
            }

            if (u != null && m != null){
                ratingsExtended.add(new RatingExtended(m, u, r));
            }
        }

        return ratingsExtended;
    }

    /**
     * Gets all ratings in the database sorted by descending date.
     *
     * Result can be paged using n and page. The returned list contains
     * the n elements of the `page`th page.
     *
     * @param n items per page
     * @param page page number
     * @return the subset of all ratings.
     */
    public QuerySubset<RatingExtended> getAllRatings(int n, int page){
        List<Rating> ratings = Rating.Adapter.fromDBObjectIterable(ratingsCollection
                .find()
                .sort(descending("date"))
                .skip(n*(page-1))
                .limit(n)
        );

        return new QuerySubset<>(
                fillRatingExtended(ratings),
               -1
        );
    }

    /**
     * Gets all ratings of a user in the database sorted by descending date.
     *
     * @param u the user
     * @param n items per page
     * @param page page number
     * @return the subset of all user ratings.
     */
    public QuerySubset<RatingExtended> getUserRatings(User u, int n, int page){
        Bson filter = eq("_id.user_id", u.getId());
        List<Rating> ratings = Rating.Adapter.fromDBObjectIterable(ratingsCollection
                .find(filter)
                .sort(descending("date"))
                .skip(n*(page-1))
                .limit(n)
        );

        return new QuerySubset<>(
                fillRatingExtended(ratings),
                -1
        );
    }

    /**
     * Get the rating (if it exists) whose user is u and movie is m.
     *
     * @param u the user
     * @param m the movie
     * @return the rating or null if none is found
     */
    public Rating getUserRating(User u, Movie m){
        return Rating.Adapter.fromDBObject(
                ratingsCollection.find(
                        and(eq("_id.user_id", u.getId()),
                            eq("_id.movie_id", m.getId()))
                ).first()
        );
    }

    /**
     * Returns an User instance containing her login information.
     *
     * @param usernameORemail query string. Can be either username or email.
     * @return an User instance with _id, username, email and password.
     */
    public User getUserLoginInfo(String usernameORemail){
        return User.Adapter.fromDBObject(
                usersCollectionPrimaryRead.find(
                        or(
                                eq("username", usernameORemail),
                                eq("email", usernameORemail)
                        )
                ).projection(
                        include("_id", "username", "email", "password")
                ).first()
        );
    }

    /**
     * Returns an User instance containing all his login information.
     *
     * This function also calculates the user statistics using
     * aggregation pipelines.
     *
     * @param username the user's username
     * @return a User with all information (including statistics)
     */
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
                    Aggregates.sort(descending("avg_rating", "movie_count")),
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

    /**
     * Get a user by his id, returning only a chosen subset of field.
     *
     * @param userId the _id of the User
     * @param fields the list of fields to return
     * @return a User instance with the chosen fields or null if user
     *         is not found
     */
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

    /**
     * Adds the new login session to the user.
     *
     * @param u the user
     * @param s the new session
     * @return true if addition was successful, false otherwise
     */
    public boolean addSession(User u, Session s){
        UpdateResult result = usersCollectionMajorityWrite.updateOne(
                eq("_id", u.getId()),
                push("sessions", Session.Adapter.toDBObject(s))
        );
        return result.getModifiedCount() == 1;
    }

    /**
     * Checks whether the given session of a user exists.
     *
     * @param u the user
     * @param s the session
     * @return true if the user has a matching session, false otherwise
     */
    public boolean existsSession(User u, Session s){
        Document userDocument = usersCollectionPrimaryRead.find(
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

    /**
     * Returns the user whose session is s.
     *
     * @param s the session
     * @return the user or null if none is found
     */
    public User getUserFromSession(Session s){
        Document userDocument = usersCollectionPrimaryRead.find(
                and(
                        eq("sessions._id", s.getId())
                ))
                .first();
        if (userDocument == null)
            return null;

        return User.Adapter.fromDBObject(userDocument);
    }

    /**
     * Removes the given session from the user.
     *
     * @param u the user
     * @param s the session
     * @return true if deletion was successful, false otherwise
     */
    public boolean removeSession(User u, Session s){
        UpdateResult result = usersCollectionMajorityWrite.updateOne(
                eq("_id", u.getId()),
                pull("sessions", eq("_id", s.getId()))
        );
        return result.getModifiedCount() == 1;
    }

    /**
     * Renew the given session of the given user.
     *
     * @param u the user
     * @param s the session
     * @return true if renewal was successful, false otherwise
     */
    public boolean updateSession(User u, Session s){
        UpdateResult result = usersCollectionMajorityWrite.updateOne(
                and(eq("_id", u.getId()),
                    eq("sessions._id", s.getId())),
                set("sessions.$.expiry", s.getExpiry())
        );
        return result.getModifiedCount() == 1;
    }

    /**
     * Returns a list of filtered movies with the given sorting. The result is paged
     *
     * @param sortBy field to sortBy (must match a DB field)
     * @param sortOrder 1 for ascending, -1 for descending
     * @param minRating return only movies with a higher rating than this
     * @param maxRating return only movies with a lower rating than this
     * @param director return only movies from a director whose name contains all words in this string
     * @param actor return only movies with an actor whose name contains all words in this string
     * @param country return only movies that contain this country
     * @param fromYear return only movies that were released in this or a following year
     * @param toYear return only movies that were released in this or a previous year
     * @param genre return only movies that contain this genre
     * @param n items per page
     * @param page page number
     * @return a paged subset of all movies that match all filters
     */
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
                actorConditions.add(regex("characters.actor_name", s, "i"));
            }
            conditions.add(and(actorConditions.toArray(new Bson[]{})));
        }

        if (country != null && !country.isEmpty()){
            conditions.add(eq("countries", country));
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
                -1
        );
    }

    /**
     * Finds a movie given its id and returns some chosen fields.
     *
     * @param id the id of the movie
     * @param fields the list of fields to be removed
     * @return a Movie instance with the given fields or null if none is found
     */
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

    /**
     * Finds a movie by name. All matching movies are returned.
     *
     * This function uses a Mongo text index to fuzzy match movies.
     *
     * @param query the string to query
     * @param n items per page
     * @param page page number
     * @return a paged subset of the search results
     */
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
                -1
        );
    }

    /**
     *
     * @param groupBy one of (genre, year, contry, acotr, director)
     * @param realSortBy one of (name, avg_rating, movie_count)
     * @param sortOrder 1 for ascending, -1 for descending
     * @param minRating return only movies with a higher rating than this
     * @param maxRating return only movies with a lower rating than this
     * @param director return only movies from a director whose name contains all words in this string
     * @param actor return only movies with an actor whose name contains all words in this string
     * @param country return only movies that contain this country
     * @param fromYear return only movies that were released in this or a following year
     * @param toYear return only movies that were released in this or a previous year
     * @param genre return only movies that contain this genre
     * @param n items per page
     * @param page page number
     * @return a paged subset of the aggregated statistics
     */
    public QuerySubset<Statistics<Statistics.Aggregator>> getStatistics(String groupBy, String realSortBy, int sortOrder, double minRating,
        double maxRating, String director, String actor, String country, int fromYear, int toYear, String genre,
        int n, int page) {

        String realUnwindBy;
        String realGroupBy;
        String realGroupName;
        Class<? extends Statistics.Aggregator> groupClass;

        switch (groupBy){
            case  "genre":
                realUnwindBy = "genres";
                realGroupBy = "genres";
                realGroupName = "genres";
                groupClass = Genre.class;
                break;
            case  "year":
                realUnwindBy = "year";
                realGroupBy = "year";
                realGroupName = "year";
                groupClass = Year.class;
                break;
            case  "country":
                realUnwindBy = "countries";
                realGroupBy = "countries";
                realGroupName = "countries";
                groupClass = Country.class;
                break;
            case  "director":
                realUnwindBy = "directors";
                realGroupBy = "directors.id";
                realGroupName = "directors.name";
                groupClass = Person.class;
                break;
            case  "actor":
                realUnwindBy = "characters";
                realGroupBy = "characters.actor_id";
                realGroupName = "characters.actor_name";
                groupClass = Person.class;
                break;
            default:
                return null;
        }

        //determinare i parametri per il filtraggio
        Bson sorting;
        if (sortOrder == 1) {
            sorting = ascending(realSortBy);
        } else if (sortOrder == -1) {
            sorting = descending(realSortBy);
        } else {
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
                actorConditions.add(regex("characters.actor_name", s, "i"));
            }
            conditions.add(and(actorConditions.toArray(new Bson[]{})));
        }

        if (country != null && !country.isEmpty()){
            conditions.add(eq("countries", country));
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

        //aggregazione
        AggregateIterable<Document> iterable = moviesCollection.aggregate(
                Arrays.asList(
                        Aggregates.match(filters),
                        Aggregates.unwind("$" + realUnwindBy),
                        Aggregates.group(
                                "$" + realGroupBy,
                                Accumulators.first("name", "$" + realGroupName),
                                Accumulators.avg("avg_rating", "$total_rating"),
                                Accumulators.sum("movie_count", 1)
                        ),
                        Aggregates.sort(sorting),
                        Aggregates.skip(n*(page-1)),
                        Aggregates.limit(n)
                )
        );//.iterator;

        return new QuerySubset<>(
                Statistics.Adapter.fromDBObjectIterable(iterable, groupClass),
                -1
        );

    }

    /**
     * Adds a new user to the database.
     *
     * @param u the user to be added
     * @return the id of the new user or null in case of error
     */
    public ObjectId addUser(User u){
        // check unique
        // NB: unique => not banned
        Document userInDB = usersCollectionPrimaryRead.find(
                or(
                    eq("username", u.getUsername()),
                    eq("email", u.getEmail())
                )
        ).first();

        if (userInDB != null){
            return null;
        }

        InsertOneResult result = usersCollectionMajorityWrite.insertOne(User.Adapter.toDBObject(u));

        if (result.getInsertedId() != null) {
            return result.getInsertedId().asObjectId().getValue();
        } else{
            return null;
        }
    }

    /**
     * Changes the user password.
     *
     * @param u the User instance with the modified password.
     * @return true if the password was changed successfully, false otherwise
     */
    public boolean editUserPassword(User u){
        UpdateResult result = usersCollectionMajorityWrite.updateOne(
                eq("username", u.getUsername()),
                set("password", u.getPassword())
        );

        return result.getModifiedCount() != 0;
    }

    /**
     * Bans a user. This consists in setting a flag in his document
     *
     * NB: User u must contain ID
     * NB: all user ratings will be deleted
     *
     * @param u the user to be banned (only the _id is used)
     * @return the list of movies whose rating should be updated
     */
    public boolean banUser(User u){
        UpdateResult result = usersCollectionMajorityWrite.updateOne(
                eq("_id", u.getId()),
                combine(
                    set("isBanned", true),
                    unset("sessions")
                )
        );

        if (result.getModifiedCount() == 1) {
            // delete user ratings
            List<Rating> ratings = Rating.Adapter.fromDBObjectIterable(ratingsCollection.find(eq("_id.user_id", u.getId())));
            return deleteRatings(ratings);
        } else{
            return true;
        }
    }

    /**
     * Searches a user by name.
     *
     * This uses a text index in the User collection.
     *
     * @param query the string to match
     * @param n items per page
     * @param page page number
     * @return a paged subset of the users that match the given query
     */
    public QuerySubset<User> searchUser(String query, int n, int page){
        Bson filter = text(query);
        FindIterable<Document> userIterable = usersCollection
                .find(filter)
                .projection(Projections.metaTextScore("score"))
                .sort(Sorts.metaTextScore("score"))
                .skip(n*(page-1))
                .limit(n);

        return new QuerySubset<>(
                User.Adapter.fromDBObjectIterable(userIterable),
                -1);
    }

    /**
     * Adds the user ratings to the given movies.
     *
     * @param u the User whose ratings are to be found
     * @param movies the list of movies to be updated with the user ratings.
     * @return true if the movies were updated successfully, false otherwise
     */
    public boolean fillUserRatings(User u, List<Movie> movies){
        List<String> ids = new ArrayList<>();
        for (Movie m:movies){
            ids.add(m.getId());
        }

        FindIterable<Document> ratingIterable = ratingsCollection.find(
            and(
                eq("_id.user_id", u.getId()),
                in("_id.movie_id", ids)
            )
        );

        List<Rating> ratings = Rating.Adapter.fromDBObjectIterable(ratingIterable);

        for (Movie m:movies){
            for (Rating r:ratings){
                if (m.getId().equals(r.getMovieId())){
                    m.setUserRating(r.getRating());
                    break;
                }
            }
        }

        return true;
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
