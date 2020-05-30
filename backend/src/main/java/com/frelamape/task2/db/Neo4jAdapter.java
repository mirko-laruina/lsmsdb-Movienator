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
    // TODO

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

    public Driver getDriver() {
        return driver;
    }

    public QuerySubset<Movie> getMovieSuggestions(User u, int n) {
        // TODO
        return new QuerySubset<>(
                new ArrayList<>(),
                false
        );    }

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

    public QuerySubset<User> getUserSuggestions(User user, int n) {
        // TODO
        return new QuerySubset<>(
                new ArrayList<>(),
                false
        );
    }

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
