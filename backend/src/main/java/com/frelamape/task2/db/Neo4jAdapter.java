package com.frelamape.task2.db;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

@Component
public class Neo4jAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jAdapter.class);

    @Value("${com.frelamape.task2.db.Neo4jAdapter.connectionURI}")
    private String connectionURI;

    @Value("${com.frelamape.task2.db.Neo4jAdapter.username}")
    private String username;

    @Value("${com.frelamape.task2.db.Neo4jAdapter.password}")
    private String password;

    @Autowired
    private Neo4jTaskExecutor executor;

    @Autowired
    private ApplicationArguments args;

    private Driver driver;

    private static final int MIN_RATING = 3;
    private static final int MAX_DAYS = 7;
    private static final int MAX_PATHS = 1000;

    @PostConstruct
    public void init() {
        if (args.getSourceArgs().length >= 3) {
            connectionURI = args.getSourceArgs()[3];
        }

        logger.info("Connecting to Neo4J at " + connectionURI);

        driver = GraphDatabase.driver(connectionURI, AuthTokens.basic( username, password ) );
    }

    @PreDestroy
    public void teardown(){
        driver.close();
    }

    /**
     * Returns the Neo4j driver associated to this adapter.
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Retuns a list of suggested movies.
     * 
     * @param u the recipient of the suggestions
     * @param n the number of suggestions
     * @return a QuerySubset of n suggested movies.
     */
    public QuerySubset<Movie> getMovieSuggestions(User u, int n) {
        try (org.neo4j.driver.Session session = driver.session()) {
            Result result = session.run(
                        "CALL{ " +
                            "MATCH (u:User {username:$username}) " +
                            "MATCH (u)-[rum1:RATED]->(m1:Movie)<-[ru2m1:RATED]-(u2:User)-[ru2m2:RATED]->(m2:Movie) " +
                            "WHERE rum1.rating>="+MIN_RATING+" AND ru2m1.rating>="+MIN_RATING+" AND ru2m2.rating >=" + MIN_RATING + " " +
                                "AND duration.between(datetime(ru2m1.date), date()).days<=" + MAX_DAYS + " " +
                                "AND duration.between(datetime(ru2m2.date), date()).days<=" + MAX_DAYS + " " +
                                "AND u<>u2 AND m1<>m2 " +
                            "RETURN m2 " +
                            "LIMIT " + MAX_PATHS +
                        "} " +
                        "RETURN m2._id AS _id, m2.title AS title, m2.poster AS poster, count(*) as score " +
                        "ORDER BY score DESC " +
                        "LIMIT $limit",
                    parameters(
                            "username", u.getUsername(),
                            "limit", n));
            
            List<Movie> movies = Movie.Adapter.fromNeo4jResult(result);
            return new QuerySubset<>(
                movies,
                true
            );
        }
    }

    /**
     * Returns the relationships between user 1 and user 2.
     * 
     * This function will return 
     * 
     * @param u1 the first user
     * @param u2 the second user
     * @return a User.Relationship instance that indicates whether user 1 
     *         follows user 2 (following) and whether user 2 follows user 1
     *         (followed).
     */
    public User.Relationship getUserRelationship(User u1, User u2) {
        try (org.neo4j.driver.Session session = driver.session()) {
            Result result = session.run(
                        "MATCH (u1:User {username: $username1}) " +
                        "MATCH (u2:User {username: $username2}) " +
                        "RETURN EXISTS((u1)-[:FOLLOWS]->(u2)), " +
                                "EXISTS((u2)-[:FOLLOWS]->(u1))",
                    parameters("username1", u1.getUsername(),
                                "username2", u2.getUsername()));
            // Each Cypher execution returns a stream of records.
            if (result.hasNext()) {
                Record record = result.next();
                User.Relationship relationship = new User.Relationship();
                relationship.following = record.get(0).asBoolean();
                relationship.follower  = record.get(1).asBoolean();
                return relationship;
            }
        }
        return null;
    }

    /**
     * Returns the (paged) list of followers of user.
     * 
     * Each user will also contain a User.Relationship instance from the point
     * of view (PoV) of the user making the query (relationshipPoV).
     * 
     * @param user the user whose followers are to be returned
     * @param relationshipPoV the user whose relationships are to be returned
     * @param n the number of users to return
     * @param page the number of the current page (assuming all pages are of n 
     *             elements).
     * @return the list of followers of user.
     */
    public QuerySubset<User> getFollowers(User user, User relationshipPoV, int n, int page) {
        try (org.neo4j.driver.Session session = driver.session()) {
            Result result = session.run(
                        "MATCH (me:User {username: $username}) " +
                        "MATCH (pov:User {username: $username_pov}) " +
                        "MATCH (user:User)-[:FOLLOWS]->(me) " +
                        "RETURN user.username as username, user._id as _id, " +
                            "EXISTS((pov)-[:FOLLOWS]->(user)) as following, " +
                            "EXISTS((user)-[:FOLLOWS]->(pov)) as follower " +
                        "ORDER BY user.username " +
                        "SKIP $skip " +
                        "LIMIT $limit",
                    parameters(
                            "username", user.getUsername(),
                            "username_pov", relationshipPoV.getUsername(),
                            "skip", n*(page-1),
                            "limit", n+1));
            
            List<User> users = User.Adapter.fromNeo4jResult(result);
            boolean lastPage = users.size() <= n;
            if (!lastPage)
                users.remove(n);
    
            return new QuerySubset<>(
                users,
                lastPage
            );
        }
    }

    /**
     * Returns the (paged) list of users that 'user' is following.
     * 
     * Each user will also contain a User.Relationship instance from the point
     * of view (PoV) of the user making the query (relationshipPoV).
     * 
     * @param user the user whose followed are to be returned
     * @param relationshipPoV the user whose relationships are to be returned
     * @param n the number of users to return
     * @param page the number of the current page (assuming all pages are of n 
     *             elements).
     * @return the list of users that 'user' is following.
     */
    public QuerySubset<User> getFollowings(User user, User relationshipPoV, int n, int page) {
        try (org.neo4j.driver.Session session = driver.session()) {
            Result result = session.run(
                        "MATCH (me:User {username: $username}) " +
                        "MATCH (me)-[:FOLLOWS]->(user:User) " +
                        "MATCH (pov:User {username: $username_pov}) " +
                        "RETURN user.username as username, user._id as _id, " +
                            "EXISTS((pov)-[:FOLLOWS]->(user)) as following, " +
                            "EXISTS((user)-[:FOLLOWS]->(pov)) as follower " +
                        "ORDER BY user.username " +
                        "SKIP $skip " +
                        "LIMIT $limit",
                    parameters(
                            "username", user.getUsername(),
                            "username_pov", relationshipPoV.getUsername(),
                            "skip", n*(page-1),
                            "limit", n+1));
                            
            List<User> users = User.Adapter.fromNeo4jResult(result);
            boolean lastPage = users.size() <= n;
            if (!lastPage)
                users.remove(n);
    
            return new QuerySubset<>(
                users,
                lastPage
            );
        }
    }

    /**
     * Returns a list of suggestions for new users to follow
     * 
     * @param user the user whom to receive suggestions for
     * @param n the number of suggestions to be returned
     * @return the list of suggestions
     */
    public QuerySubset<User> getUserSuggestions(User user, int n) {
        // TODO
        return new QuerySubset<>(
                new ArrayList<>(),
                false
        );
    }

    /**
     * Returns the list of ratings of the users followed by u.
     * 
     * Ratings will be returned in chronological order (most recent first).
     * 
     * @param u the users whose follower's ratings are to be returned
     * @param n the number of ratings to return 
     * @param page the number of the current page (assuming all pages are of n 
     *             elements).
     * @return the list of (extended) ratings.
     */
    public QuerySubset<RatingExtended> getFollowingRatings(User u, int n, int page) {
        try (org.neo4j.driver.Session session = driver.session()) {
            Result result = session.run(
                        "MATCH (:User {username: $username})-[:FOLLOWS]->(u:User)-[r:RATED]->(m:Movie) " +
                        "RETURN r.date as date, r.rating as rating, " +
                            "m._id as movie_id, m.title as title, m.year as year, m.poster as poster, " +
                            "u._id as user_id, u.username as username " +
                        "ORDER BY r.date " +
                        "SKIP $skip " +
                        "LIMIT $limit",
                    parameters("username", u.getUsername(),
                            "skip", n*(page-1),
                            "limit", n+1));
            
            List<RatingExtended> ratings = RatingExtended.Adapter.fromNeo4jResult(result);
            boolean lastPage = ratings.size() <= n;
            if (!lastPage)
                ratings.remove(n);
    
            return new QuerySubset<>(
                ratings,
                lastPage
            );
        }
    }

    /**
     * Creates a new user in Neo4j.
     * 
     * NB: Neo4j stores only username and id.
     * 
     * @param u the user to be created
     * @return true if the transaction was successful, false otherwise
     */
    public boolean insertUser(User u){
        try (org.neo4j.driver.Session session = driver.session()) {
            session.writeTransaction( tx -> tx.run(
                    "CREATE (:User {_id: $id, username: $username})",
                        parameters("username", u.getUsername(),
                                "id", u.getId().toHexString()))
            );
            return true;
        }
    }

    /**
     * Creates or updates a RATED relationship in Neo4j.
     * 
     * @param r the rating to be added/updated
     * @return true if the transaction was successful, false otherwise 
     */
    public boolean insertRating(Rating r){
        try (org.neo4j.driver.Session session = driver.session()) {
            session.writeTransaction( tx -> tx.run(
                        "MATCH (m:Movie {_id: $movie_id}) " +
                        "MATCH (u:User {_id: $user_id}) " +
                        "MERGE (u)-[r:RATED]->(m) " +
                        "SET r.rating=$rating, r.date=$date",
                    parameters("movie_id", r.getMovieId(),
                            "user_id", r.getUserId().toHexString(),
                            "rating", r.getRating(),
                            "date", RatingExtended.Adapter.ISO8601.format(r.getDate())))
            );
            return true;
        }
    }

    /**
     * Deletes a rating from Neo4j.
     * 
     * @param r the rating to be deleted
     * @return true if the transaction was successful, false otherwise
     */
    public boolean deleteRating(Rating r){
        try (org.neo4j.driver.Session session = driver.session()) {
            session.writeTransaction( tx -> tx.run(
                    "MATCH (m:Movie {_id: $movie_id}) " +
                            "MATCH (u:User {_id: $user_id}) " +
                            "MATCH (u)-[r:RATED]->(m) " +
                            "DELETE r",
                    parameters("movie_id", r.getMovieId(),
                            "user_id", r.getUserId().toHexString()))
            );
            return true;
        }
    }

    /**
     * Add a new FOLLOWS relationship from user A to user B.
     * 
     * @param a the user that is following
     * @param b the user that is being followed
     * @return true if the transaction was successful, false otherwise
     */
    public boolean follow(User a, User b){
        try (org.neo4j.driver.Session session = driver.session()) {
            session.writeTransaction( tx -> tx.run(
                    "MATCH (a:User {username: $username_a}) " +
                            "MATCH (b:User {username: $username_b}) " +
                            "MERGE (a)-[:FOLLOWS]->(b)",
                    parameters("username_a", a.getUsername(),
                            "username_b", b.getUsername()))
            );
            return true;
        }
    }

    /**
     * Removes the FOLLOWS relationship from user A to user B.
     * 
     * @param a the user that stopped following
     * @param b the user that is no longer being followed
     * @return true if the transaction was successful, false otherwise
     */
    public boolean unfollow(User a, User b){
        try (org.neo4j.driver.Session session = driver.session()) {
            session.writeTransaction( tx -> tx.run(
                    "MATCH (a:User {username: $username_a}) " +
                            "MATCH (b:User {username: $username_b}) " +
                            "MATCH (a)-[r:FOLLOWS]->(b) " +
                            "DELETE r",
                    parameters("username_a", a.getUsername(),
                            "username_b", b.getUsername()))
            );
            return true;
        }
    }
}
