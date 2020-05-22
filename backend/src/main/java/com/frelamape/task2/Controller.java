package com.frelamape.task2;

import com.frelamape.task2.api.BaseResponse;
import com.frelamape.task2.api.LoginResponse;
import com.frelamape.task2.api.SocialProfileResponse;
import com.frelamape.task2.db.*;
import com.google.gson.Gson;
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
        if (u != null){
            Session s = new Session(UUID.randomUUID().toString()); // TODO better session
            if (!u.isBanned()){
                if (mongoDBAdapter.addSession(u, s))
                    return new Gson().toJson(new BaseResponse(true, null, new LoginResponse(s.getId(), u.isAdmin())));
                else
                    return new Gson().toJson(new BaseResponse(true, "Error creating new user session", null));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User is banned.", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Wrong username or password.", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/auth/password"}, method= RequestMethod.POST)
    public @ResponseBody String changePassword(@RequestParam(value = "sessionId") String sid,
                                      @RequestParam("password") String password){
        password = password.trim();
        if(password.equals("")){
            return new Gson().toJson(new BaseResponse(false, null, null));
        }
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);
        u.setPassword(password);

        boolean result = mongoDBAdapter.editUserPassword(u);
        
        return new Gson().toJson(new BaseResponse(result, null, null));
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
            return new Gson().toJson(new BaseResponse(false, null, null));
        }
        User u = new User(username);
        u.setPassword(password);
        u.setEmail(email);
        Session s = new Session(UUID.randomUUID().toString()); // TODO better session

        ObjectId userId = mongoDBAdapter.addUser(u);

        if (userId == null){
            return new Gson().toJson(new BaseResponse(false, null, null));
        }

        u.setId(userId);

        neo4jTaskExecutor.insertUser(u);

        if (mongoDBAdapter.addSession(u, s)){
            return new Gson().toJson(new BaseResponse(true, null, new LoginResponse(s.getId(), u.isAdmin())));
        } else{
            return new Gson().toJson(new BaseResponse(false, null, null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/auth/logout"}, method= RequestMethod.POST)
    public @ResponseBody String logout(@RequestParam("sessionId") String sid){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);
        return new Gson().toJson(new BaseResponse(mongoDBAdapter.removeSession(u, s), null, null));
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
                return new Gson().toJson(new BaseResponse(false, "Unrecognized sortBy value: " + sortBy, null));
        }

        QuerySubset<Movie> querySubset = mongoDBAdapter.getMovieList(realSortBy, sortOrder, minRating, maxRating, director, actor, country,
                                              fromYear, toYear, genre, n, page);
        if (u != null){
            mongoDBAdapter.fillUserRatings(u, querySubset.getList());
        }
        return new Gson().toJson(new BaseResponse(true, null, querySubset));
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
            return new Gson().toJson(new BaseResponse(false, "User is not logged in", null));
        }

        QuerySubset<Movie> movies = neo4jAdapter.getMovieSuggestions(u, n);
        return new Gson().toJson(new BaseResponse(true, null, movies));
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
        return new Gson().toJson(new BaseResponse(true, null, movies));
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
            return new Gson().toJson(new BaseResponse(true, null, movie));
        } else {
            return new Gson().toJson(new BaseResponse(false, "Not found", null));

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
            return new Gson().toJson(new BaseResponse(false, "Session ID does not match any active session. It may be expired.", null));
        }

        Movie movie = mongoDBAdapter.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, ratingValue);
        mongoDBAdapter.insertRating(rating);

        neo4jTaskExecutor.insertRating(rating);

        return new Gson().toJson(new BaseResponse(true, "Rating inserted successfully", null));
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/{movieId}/rating"}, method= RequestMethod.DELETE)
    public @ResponseBody String deleteRating(@RequestParam(value = "sessionId", required = true) String sid,
                                          @PathVariable("movieId") String movieId
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u == null){
            return new Gson().toJson(new BaseResponse(false, "Session ID does not match any active session. It may be expired.", null));
        }

        Movie movie = mongoDBAdapter.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, 0.0);
        boolean result = mongoDBAdapter.deleteRating(rating);
        if (result){
            neo4jTaskExecutor.deleteRating(rating);
            return new Gson().toJson(new BaseResponse(true, null, null));
        } else {
            return new Gson().toJson(new BaseResponse(false, null, null));
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
                return new Gson().toJson(new BaseResponse(false, "Unrecognized sortBy value: " + sortBy, null));
        }

        QuerySubset<Statistics<Statistics.Aggregator>> statistics = mongoDBAdapter.getStatistics(groupBy, realSortBy, sortOrder,
                minRating, maxRating, director, actor, country, fromYear, toYear, genre, n, page);

        return new Gson().toJson(new BaseResponse(true, null, statistics));
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}"}, method= RequestMethod.GET)
    public @ResponseBody String getUserProfile(@RequestParam(value = "sessionId") String sid,
                                         @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        Future<User.Relationship> relationshipFuture = null;
        if (!u.getUsername().equals(username))
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
            return new Gson().toJson(new BaseResponse(true, null, u2));
        } else {
            return new Gson().toJson(new BaseResponse(false, "User not found", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/ratings"}, method= RequestMethod.GET)
    public @ResponseBody String getUserRatings(@RequestParam(value = "sessionId") String sid,
                                               @PathVariable("username") String username,
                                               @RequestParam(required = false, defaultValue = "10") int n,
                                               @RequestParam(required = false, defaultValue = "1") int page
    ){
        User u2 = mongoDBAdapter.getUserLoginInfo(username);
        if (u2 != null){
            QuerySubset<RatingExtended> ratings = mongoDBAdapter.getUserRatings(u2, n, page);
            return new Gson().toJson(new BaseResponse(true, null, ratings));
        } else {
            return new Gson().toJson(new BaseResponse(false, "User not found", null));
        }

    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/followers"}, method= RequestMethod.GET)
    public @ResponseBody String getUserFollowers(@RequestParam(value = "sessionId") String sid,
                                               @PathVariable("username") String username,
                                               @RequestParam(required = false, defaultValue = "10") int n,
                                               @RequestParam(required = false, defaultValue = "1") int page
    ){
        QuerySubset<User> followers = neo4jAdapter.getFollowers(new User(username), n, page);

        if (followers != null){
            return new Gson().toJson(new BaseResponse(true, null, followers));
        } else {
            return new Gson().toJson(new BaseResponse(false, "User not found", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/followings"}, method= RequestMethod.GET)
    public @ResponseBody String getUserFollowings(@RequestParam(value = "sessionId") String sid,
                                               @PathVariable("username") String username,
                                               @RequestParam(required = false, defaultValue = "10") int n,
                                               @RequestParam(required = false, defaultValue = "1") int page
    ){
        QuerySubset<User> followings = neo4jAdapter.getFollowings(new User(username), n, page);

        if (followings != null){
            return new Gson().toJson(new BaseResponse(true, null, followings));
        } else {
            return new Gson().toJson(new BaseResponse(false, "User not found", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/users/suggested"}, method= RequestMethod.GET)
    public @ResponseBody String getSuggestedUsers(@RequestParam(value = "sessionId") String sid,
                                               @RequestParam(required = false, defaultValue = "10") int n
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        QuerySubset<User> suggestions = neo4jAdapter.getUserSuggestions(new User(u.getUsername()), n);

        if (suggestions != null){
            return new Gson().toJson(new BaseResponse(true, null, suggestions));
        } else {
            return new Gson().toJson(new BaseResponse(false, "Error", null));
        }
    }



    @CrossOrigin
    @RequestMapping(value={"/user/{username}/social"}, method= RequestMethod.GET)
    public @ResponseBody String getUserSocialProfile(@RequestParam(value = "sessionId") String sid,
                                                     @PathVariable("username") String username,
                                                     @RequestParam(required = false, defaultValue = "10") int n_followers,
                                                     @RequestParam(required = false, defaultValue = "10") int n_followings,
                                                     @RequestParam(required = false, defaultValue = "10") int n_suggestions
    ){
        User u = new User(username);
        Future<QuerySubset<User>> followersFuture = neo4jTaskExecutor.getFollowers(u, n_followers, 1);
        Future<QuerySubset<User>> followingsFuture = neo4jTaskExecutor.getFollowings(u, n_followings, 1);
        Future<QuerySubset<User>> suggestionsFuture = null;

        Session s = new Session(sid);
        User su = mongoDBAdapter.getUserFromSession(s);

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
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (!(followers == null && followings == null && suggestions == null)){
            SocialProfileResponse response = new SocialProfileResponse(followers, followings, suggestions);
            return new Gson().toJson(new BaseResponse(true, null, response));
        } else{
            return new Gson().toJson(new BaseResponse(false, "User not found", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/rating/{movieId}"}, method= RequestMethod.GET)
    public @ResponseBody String getUserRating(@RequestParam(value = "sessionId") String sid,
                                               @PathVariable("username") String username,
                                               @PathVariable("movieId") String movieId
    ){
        User u2 = mongoDBAdapter.getUserLoginInfo(username);
        if (u2 != null){
            Rating rating = mongoDBAdapter.getUserRating(u2, new Movie(movieId));
            return new Gson().toJson(new BaseResponse(true, null, rating));
        } else {
            return new Gson().toJson(new BaseResponse(false, "User not found", null));
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

        if (u != null && (u.isAdmin() || u.getUsername().equals(username))){
            User u2 = mongoDBAdapter.getUserLoginInfo(username);
            if (u2 != null){
                Movie movie = new Movie(movieId);
                Rating rating = new Rating(u2, movie, ratingVal);
                mongoDBAdapter.insertRating(rating);
                neo4jTaskExecutor.insertRating(rating);
                return new Gson().toJson(new BaseResponse(true, "Rating added", null));

            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
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

        if (u != null && (u.isAdmin() || u.getUsername().equals(username))){
            User u2 = mongoDBAdapter.getUserLoginInfo(username);
            if (u2 != null){
                Rating rating = new Rating(u2, new Movie(movieId), 0.0);
                boolean result = mongoDBAdapter.deleteRating(rating);
                if (result) {
                    neo4jTaskExecutor.deleteRating(rating);
                    return new Gson().toJson(new BaseResponse(true, null, null));
                }else
                    return new Gson().toJson(new BaseResponse(false, "Error deleting rating", null));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/ban"}, method= RequestMethod.POST)
    public @ResponseBody String banUser(@RequestParam(value = "sessionId") String sid,
                                                 @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u != null && u.isAdmin()){
            User u2 = mongoDBAdapter.getUserLoginInfo(username);
            if (u2 != null){
                boolean result = mongoDBAdapter.banUser(u2);
                if (result)  {
                    return new Gson().toJson(new BaseResponse(true, null, null));
                } else
                    return new Gson().toJson(new BaseResponse(false, "Error banning user", null));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/search"}, method= RequestMethod.GET)
    public @ResponseBody String searchUser(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam String query,
                                           @RequestParam(required = false, defaultValue = "10") int n,
                                           @RequestParam(required = false, defaultValue = "10") int page
    ){
        QuerySubset<User> users = mongoDBAdapter.searchUser(query, n, page);

        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);
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
        return new Gson().toJson(new BaseResponse(true, null, users));
    }

    @CrossOrigin
    @RequestMapping(value={"/ratings"}, method= RequestMethod.GET)
    public @ResponseBody String getAllRatings(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam(required = false, defaultValue = "10") int n,
                                           @RequestParam(required = false, defaultValue = "1") int page
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u != null && u.isAdmin()){
            QuerySubset<RatingExtended> ratings = mongoDBAdapter.getAllRatings(n, page);
            return new Gson().toJson(new BaseResponse(true, null, ratings));
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }


    @CrossOrigin
    @RequestMapping(value={"/ratings/friends"}, method= RequestMethod.GET)
    public @ResponseBody String getFriendsRatings(@RequestParam(value = "sessionId") String sid,
                                              @RequestParam(required = false, defaultValue = "10") int n,
                                              @RequestParam(required = false, defaultValue = "1") int page
    ){
        Session s = new Session(sid);
        User u = mongoDBAdapter.getUserFromSession(s);

        if (u != null){
            QuerySubset<RatingExtended> ratings = neo4jAdapter.getFriendsRatings(u, n, page);
            return new Gson().toJson(new BaseResponse(true, null, ratings));
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }
}