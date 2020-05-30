package com.frelamape.task2;

import com.frelamape.task2.api.LoginResponse;
import com.frelamape.task2.api.SocialProfileResponse;
import com.frelamape.task2.db.*;
import com.frelamape.task2.api.ResponseHelper;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Main class
 *
 * Refer to docs/api.md for the description of the methods.
 */
@RestController
@RequestMapping("/api/v1")
public class Controller {
    @Autowired
    private MongoDBAdapter mongoDBAdapter;

    @Autowired
    private Neo4jAdapter neo4jAdapter;

    @Autowired
    private Neo4jTaskExecutor neo4jTaskExecutor;

    @CrossOrigin
    @RequestMapping(value={"/auth/login"}, method= RequestMethod.POST)
    public @ResponseBody String login(@RequestParam("username") String username,
                                      @RequestParam("password") String password){
        User u = new User(username);
        u.setPassword(password);
        u = mongoDBAdapter.authUser(u);

        if (u == null)
            return ResponseHelper.wrongCredentials();

        Session s = new Session(UUID.randomUUID().toString()); // TODO better session
        if (!u.isBanned()){
            if (mongoDBAdapter.addSession(u, s))
                return ResponseHelper.success(new LoginResponse(s.getId(), u.isAdmin()));
            else
                return ResponseHelper.mongoError("Error creating new user session");
        } else {
            return ResponseHelper.userBanned();
        }

    }

    @CrossOrigin
    @RequestMapping(value={"/auth/password"}, method= RequestMethod.POST)
    public @ResponseBody String changePassword(@RequestParam(value = "sessionId") String sid,
                                      @RequestParam("password") String password){
        password = password.trim();
        if(password.equals("")){
            return ResponseHelper.genericError("Password cannot be empty");
        }

        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        u.setPassword(password);

        boolean result = mongoDBAdapter.editUserPassword(u);
        if (result){
            return ResponseHelper.success(null);
        } else{
            return ResponseHelper.mongoError("Unknown error");
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/auth/register"}, method= RequestMethod.POST)
    public @ResponseBody String register(@RequestParam("username") String username,
                                         @RequestParam("password") String password,
                                         @RequestParam("email") String email){
        email = email.trim();
        username = username.trim();
        password = password.trim();

        if(password.equals("") || username.equals("") || email.equals("")){
            return ResponseHelper.genericError("One of the field is empty");
        }

        User u = new User(username);
        u.setPassword(password);
        u.setEmail(email);
        Session s = new Session(UUID.randomUUID().toString()); // TODO better session

        ObjectId userId = mongoDBAdapter.addUser(u);

        if (userId == null){
            return ResponseHelper.genericError("User already exists.");
        }

        u.setId(userId);

        neo4jTaskExecutor.insertUser(u);
        if (mongoDBAdapter.addSession(u, s)){
            return ResponseHelper.success(new LoginResponse(s.getId(), u.isAdmin()));
        } else{
            return ResponseHelper.mongoError("Unknown error");
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/auth/logout"}, method= RequestMethod.POST)
    public @ResponseBody String logout(@RequestParam("sessionId") String sid){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (mongoDBAdapter.removeSession(u, s)) {
            return ResponseHelper.success(null);
        } else {
            return ResponseHelper.mongoError("Unknown error");
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/browse"}, method= RequestMethod.GET)
    public @ResponseBody String browseMovies(@RequestParam(value = "sessionId", required = false) String sid,
                                             @RequestParam(required = false, defaultValue = "release") String sortBy,
                                             @RequestParam(required = false, defaultValue = "-1") int sortOrder,
                                             @RequestParam(required = false, defaultValue = "-1") double minRating,
                                             @RequestParam(required = false, defaultValue = "-1") double maxRating,
                                             @RequestParam(required = false) String director,
                                             @RequestParam(required = false) String actor,
                                             @RequestParam(required = false) String country,
                                             @RequestParam(required = false, defaultValue = "-1") int fromYear,
                                             @RequestParam(required = false, defaultValue = "-1") int toYear,
                                             @RequestParam(required = false) String genre,
                                             @RequestParam(required = false, defaultValue = "10") int n,
                                             @RequestParam(required = false, defaultValue = "1") int page
                                             ){
        User u = null;
        if (sid != null){
            Session s = new Session(sid);
            u = mongoDBAdapter.getUserFromSession(s);
        }

        String realSortBy;
        switch (sortBy){
            case  "release":
                realSortBy = "date";
                break;
            case  "rating":
                realSortBy = "total_rating";
                break;
            case  "title":
                realSortBy = "title";
                break;
            default:
                return ResponseHelper.genericError("Unrecognized sortBy value: " + sortBy);
        }

        QuerySubset<Movie> querySubset = mongoDBAdapter.getMovieList(realSortBy, sortOrder, minRating, maxRating, director, actor, country,
                                              fromYear, toYear, genre, n, page);
        if (u != null){
            mongoDBAdapter.fillUserRatings(u, querySubset.getList());
        }
        return ResponseHelper.success(querySubset);
    }


    @CrossOrigin
    @RequestMapping(value={"/movie/suggestion"}, method= RequestMethod.GET)
    public @ResponseBody String suggestMovies(@RequestParam(value = "sessionId") String sid,
                                              @RequestParam int n
    ){
        User u = null;
        Session s = new Session(sid);
        u = mongoDBAdapter.getUserFromSession(s);

        if (u == null){
            return ResponseHelper.invalidSession();
        }

        QuerySubset<Movie> movies = neo4jAdapter.getMovieSuggestions(u, n);
        return ResponseHelper.success(movies);
    }



    @CrossOrigin
    @RequestMapping(value={"/movie/search"}, method= RequestMethod.GET)
    public @ResponseBody String searchMovie(@RequestParam(value = "sessionId", required = false) String sid,
                                             @RequestParam(required = true, defaultValue = "query") String query,
                                             @RequestParam(required = false, defaultValue = "10") int n,
                                             @RequestParam(required = false, defaultValue = "1") int page
    ){
        User u = null;
        if (sid != null){
            Session s = new Session(sid);
            u = mongoDBAdapter.getUserFromSession(s);
        }

        QuerySubset<Movie> movies = mongoDBAdapter.searchMovie(query, n, page);
        if (u != null){
            mongoDBAdapter.fillUserRatings(u, movies.getList());
        }
        return ResponseHelper.success(movies);
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/{movieId}"}, method= RequestMethod.GET)
    public @ResponseBody String getMovie(@RequestParam(value = "sessionId", required = false) String sid,
                                            @PathVariable("movieId") String movieId
    ){
        User u = null;
        if (sid != null){
            Session s = new Session(sid);
            u = mongoDBAdapter.getUserFromSession(s);
        }

        Movie movie = mongoDBAdapter.getMovieDetails(movieId);
        if (movie != null) {
            if (u != null) {
                Rating r = mongoDBAdapter.getUserRating(u, movie);
                if (r != null && r.getRating() != null)
                    movie.setUserRating(r.getRating());
            }
            return ResponseHelper.success(movie);
        } else {
            return ResponseHelper.notFound();

        }
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/{movieId}/rating"}, method= RequestMethod.PUT)
    public @ResponseBody String putRating(@RequestParam(value = "sessionId", required = true) String sid,
                                          @PathVariable("movieId") String movieId,
                                          @RequestParam(value = "rating", required = true) double ratingValue
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null){
            return ResponseHelper.invalidSession();
        }

        Movie movie = mongoDBAdapter.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, ratingValue);

        mongoDBAdapter.insertRating(rating);
        neo4jTaskExecutor.insertRating(rating);

        return ResponseHelper.success(null);
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/{movieId}/rating"}, method= RequestMethod.DELETE)
    public @ResponseBody String deleteRating(@RequestParam(value = "sessionId", required = true) String sid,
                                          @PathVariable("movieId") String movieId
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null){
            return ResponseHelper.invalidSession();
        }

        Movie movie = mongoDBAdapter.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, 0.0);
        boolean result = mongoDBAdapter.deleteRating(rating);
        if (result){
            neo4jTaskExecutor.deleteRating(rating);
            return ResponseHelper.success(null);
        } else {
            return ResponseHelper.mongoError("Unknown error");
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/statistics"}, method= RequestMethod.GET)
    public @ResponseBody String movieStatistics(@RequestParam(value = "sessionId", required = false) String sid,
                                             @RequestParam(required = true) String groupBy,
                                             @RequestParam(required = false, defaultValue = "rating") String sortBy,
                                             @RequestParam(required = false, defaultValue = "-1") int sortOrder,
                                             @RequestParam(required = false, defaultValue = "-1") double minRating,
                                             @RequestParam(required = false, defaultValue = "-1") double maxRating,
                                             @RequestParam(required = false) String director,
                                             @RequestParam(required = false) String actor,
                                             @RequestParam(required = false) String country,
                                             @RequestParam(required = false, defaultValue = "-1") int fromYear,
                                             @RequestParam(required = false, defaultValue = "-1") int toYear,
                                             @RequestParam(required = false) String genre,
                                             @RequestParam(required = false, defaultValue = "10") int n,
                                             @RequestParam(required = false, defaultValue = "1") int page
    ){
        //Identificazione campo per il sorting
        String realSortBy;
        switch (sortBy){
            case "count":
                realSortBy = "movie_count";
                break;
            case "rating":
                realSortBy = "avg_rating";
                break;
            case "alphabetic":
                realSortBy = "name";
                break;
            default:
                return ResponseHelper.genericError("Unrecognized sortBy value: " + sortBy);
        }

        QuerySubset<Statistics<Statistics.Aggregator>> statistics = mongoDBAdapter.getStatistics(groupBy, realSortBy, sortOrder,
                minRating, maxRating, director, actor, country, fromYear, toYear, genre, n, page);

        return ResponseHelper.success(statistics);
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}"}, method= RequestMethod.GET)
    public @ResponseBody String getUserProfile(@RequestParam(value = "sessionId", required=false) String sid,
                                         @PathVariable("username") String username
    ){
        User u = null;
        if (sid != null){
            Session s = new Session(sid);
            u = mongoDBAdapter.getUserFromSession(s);
        }

        // Asynchronously fetch relationship from Neo4j
        Future<User.Relationship> relationshipFuture = null;
        if (u != null && !u.getUsername().equals(username))
            relationshipFuture = neo4jTaskExecutor.getUserRelationship(u, new User(username));

        User u2 = mongoDBAdapter.getUserProfile(username);

        if (u2 != null){
            User.Relationship relationship = null;

            if (relationshipFuture != null) {
                try{
                    relationship = relationshipFuture.get(); // Wait for result
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if (relationship != null){
                u2.setFollower(relationship.follower);
                u2.setFollowing(relationship.following);
            }

            return ResponseHelper.success(u2);
        } else {
            return ResponseHelper.notFound();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/ratings"}, method= RequestMethod.GET)
    public @ResponseBody String getUserRatings(@RequestParam(value = "sessionId", required=false) String sid,
                                               @PathVariable("username") String username,
                                               @RequestParam(required = false, defaultValue = "10") int n,
                                               @RequestParam(required = false, defaultValue = "1") int page
    ){
        User u2 = mongoDBAdapter.getUserLoginInfo(username);
        if (u2 != null){
            QuerySubset<RatingExtended> ratings = mongoDBAdapter.getUserRatings(u2, n, page);
            return ResponseHelper.success(ratings);
        } else {
            return ResponseHelper.notFound();
        }

    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/followers"}, method= RequestMethod.GET)
    public @ResponseBody String getUserFollowers(@RequestParam(value = "sessionId", required=false) String sid,
                                               @PathVariable("username") String username,
                                               @RequestParam(required = false, defaultValue = "10") int n,
                                               @RequestParam(required = false, defaultValue = "1") int page
    ){
        QuerySubset<User> followers = neo4jAdapter.getFollowers(new User(username), n, page);

        if (followers != null){
            return ResponseHelper.success(followers);
        } else {
            return ResponseHelper.notFound();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/followings"}, method= RequestMethod.GET)
    public @ResponseBody String getUserFollowings(@RequestParam(value = "sessionId", required=false) String sid,
                                               @PathVariable("username") String username,
                                               @RequestParam(required = false, defaultValue = "10") int n,
                                               @RequestParam(required = false, defaultValue = "1") int page
    ){
        QuerySubset<User> followings = neo4jAdapter.getFollowings(new User(username), n, page);

        if (followings != null){
            return ResponseHelper.success(followings);
        } else {
            return ResponseHelper.notFound();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/users/suggested"}, method= RequestMethod.GET)
    public @ResponseBody String getSuggestedUsers(@RequestParam(value = "sessionId") String sid,
                                               @RequestParam(required = false, defaultValue = "10") int n
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        QuerySubset<User> suggestions = neo4jAdapter.getUserSuggestions(new User(u.getUsername()), n);

        if (suggestions != null){
            return ResponseHelper.success(suggestions);
        } else {
            return ResponseHelper.notFound();
        }
    }



    @CrossOrigin
    @RequestMapping(value={"/user/{username}/social"}, method= RequestMethod.GET)
    public @ResponseBody String getUserSocialProfile(@RequestParam(value = "sessionId", required=false) String sid,
                                                     @PathVariable("username") String username,
                                                     @RequestParam(required = false, defaultValue = "10") int n_followers,
                                                     @RequestParam(required = false, defaultValue = "10") int n_followings,
                                                     @RequestParam(required = false, defaultValue = "10") int n_suggestions
    ){
        User u = new User(username);
        Future<QuerySubset<User>> followersFuture = neo4jTaskExecutor.getFollowers(u, n_followers, 1);
        Future<QuerySubset<User>> followingsFuture = neo4jTaskExecutor.getFollowings(u, n_followings, 1);
        Future<QuerySubset<User>> suggestionsFuture = null;

        User su = null;
        if (sid != null){
            Session s = new Session(sid);
            su = mongoDBAdapter.getUserFromSession(s);
        }

        if (su != null && su.getUsername().equals(username)) {
            suggestionsFuture = neo4jTaskExecutor.getUserSuggestions(u, n_suggestions);
        }

        QuerySubset<User> followers = null;
        QuerySubset<User> followings = null;
        QuerySubset<User> suggestions = null;

        try {
            followers = followersFuture.get();
            followings = followingsFuture.get();
            if (suggestionsFuture != null)
                suggestions = suggestionsFuture.get();
            else {
                suggestions = new QuerySubset<>(new ArrayList<>(), true);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (!(followers == null && followings == null && suggestions == null)){
            SocialProfileResponse response = new SocialProfileResponse(followers, followings, suggestions);
            return ResponseHelper.success(response);
        } else{
            return ResponseHelper.notFound();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/rating/{movieId}"}, method= RequestMethod.GET)
    public @ResponseBody String getUserRating(@RequestParam(value = "sessionId", required=false) String sid,
                                               @PathVariable("username") String username,
                                               @PathVariable("movieId") String movieId
    ){
        User u2 = mongoDBAdapter.getUserLoginInfo(username);
        if (u2 != null){
            Rating rating = mongoDBAdapter.getUserRating(u2, new Movie(movieId));
            return ResponseHelper.success(rating);
        } else {
            return ResponseHelper.notFound();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/rating/{movieId}"}, method= RequestMethod.POST)
    public @ResponseBody String putUserRating(@RequestParam(value = "sessionId") String sid,
                                              @PathVariable("username") String username,
                                              @PathVariable("movieId") String movieId,
                                              @RequestParam("rating") double ratingVal
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin() || u.getUsername().equals(username)){
            User u2 = mongoDBAdapter.getUserLoginInfo(username);
            if (u2 != null){
                Movie movie = new Movie(movieId);
                Rating rating = new Rating(u2, movie, ratingVal);
                mongoDBAdapter.insertRating(rating);
                neo4jTaskExecutor.insertRating(rating);
                return ResponseHelper.success(null);
            } else {
                return ResponseHelper.notFound();
            }
        } else {
            return ResponseHelper.unauthorized();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/rating/{movieId}"}, method= RequestMethod.DELETE)
    public @ResponseBody String deleteUserRating(@RequestParam(value = "sessionId") String sid,
                                              @PathVariable("username") String username,
                                              @PathVariable("movieId") String movieId
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin() || u.getUsername().equals(username)){
            User u2 = mongoDBAdapter.getUserLoginInfo(username);
            if (u2 != null){
                Rating rating = new Rating(u2, new Movie(movieId), 0.0);
                boolean result = mongoDBAdapter.deleteRating(rating);
                if (result)
                    return ResponseHelper.success(null);
                else
                    return ResponseHelper.mongoError("Unknown erorr");
            } else {
                return ResponseHelper.notFound();
            }
        } else {
            return ResponseHelper.unauthorized();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/follow"}, method= RequestMethod.POST)
    public @ResponseBody String followUser(@RequestParam(value = "sessionId") String sid,
                                                 @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        User u2 = new User(username);
        boolean result = neo4jAdapter.follow(u, u2);
        if (result)  {
            return ResponseHelper.success(null);
        } else
            return ResponseHelper.notFound();
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/unfollow"}, method= RequestMethod.POST)
    public @ResponseBody String unfollowUser(@RequestParam(value = "sessionId") String sid,
                                                 @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        User u2 = new User(username);
        boolean result = neo4jAdapter.unfollow(u, u2);
        if (result)  {
            return ResponseHelper.success(null);
        } else
            return ResponseHelper.notFound();
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/ban"}, method= RequestMethod.POST)
    public @ResponseBody String banUser(@RequestParam(value = "sessionId") String sid,
                                                 @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin()){
            User u2 = mongoDBAdapter.getUserLoginInfo(username);
            if (u2 != null){
                boolean result = mongoDBAdapter.banUser(u2);
                if (result)  {
                    return ResponseHelper.success(null);
                } else
                    return ResponseHelper.mongoError("Unknown error");
            } else {
                return ResponseHelper.notFound();
            }
        } else {
            return ResponseHelper.unauthorized();
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/search"}, method= RequestMethod.GET)
    public @ResponseBody String searchUser(@RequestParam(value = "sessionId", required = false) String sid,
                                           @RequestParam String query,
                                           @RequestParam(required = false, defaultValue = "10") int n,
                                           @RequestParam(required = false, defaultValue = "1") int page
    ){
        QuerySubset<User> users = mongoDBAdapter.searchUser(query, n, page);

        User u = null;
        if (sid != null){
            Session s = new Session(sid);
            u = mongoDBAdapter.getUserFromSession(s);
        }
        
        if (u != null) {
            // Find relationships asynchronously
            List<Future<User.Relationship>> relationshipFutureList = new ArrayList<>();
            for (User user : users.getList()) {
                relationshipFutureList.add(neo4jTaskExecutor.getUserRelationship(u, user));
            }

            for (int i = 0; i< relationshipFutureList.size(); i++){
                Future<User.Relationship> future = relationshipFutureList.get(i);
                User.Relationship rel = null;
                try {
                    rel = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (rel != null){
                    users.getList().get(i).setFollower(rel.follower);
                    users.getList().get(i).setFollowing(rel.following);
                }
            }
        }
        return ResponseHelper.success(users);
    }

    @CrossOrigin
    @RequestMapping(value={"/ratings"}, method= RequestMethod.GET)
    public @ResponseBody String getAllRatings(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam(required = false, defaultValue = "10") int n,
                                           @RequestParam(required = false, defaultValue = "1") int page
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin()){
            QuerySubset<RatingExtended> ratings = mongoDBAdapter.getAllRatings(n, page);
            return ResponseHelper.success(ratings);
        } else {
            return ResponseHelper.unauthorized();
        }
    }


    @CrossOrigin
    @RequestMapping(value={"/ratings/following"}, method= RequestMethod.GET)
    public @ResponseBody String getFollowingRatings(@RequestParam(value = "sessionId") String sid,
                                              @RequestParam(required = false, defaultValue = "10") int n,
                                              @RequestParam(required = false, defaultValue = "1") int page
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        QuerySubset<RatingExtended> ratings = neo4jAdapter.getFollowingRatings(u, n, page);
        return ResponseHelper.success(ratings);
    }
}