package com.frelamape.task2;

import com.frelamape.task2.api.BaseResponse;
import com.frelamape.task2.api.LoginResponse;
import com.frelamape.task2.db.*;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@EnableAutoConfiguration
public class Main {
    private static DatabaseAdapter dba;

    @CrossOrigin
    @RequestMapping(value={"/api/v1/auth/login"}, method= RequestMethod.POST)
    public @ResponseBody String login(@RequestParam("username") String username,
                                      @RequestParam("password") String password){
        User u = new User(username);
        u.setPassword(password);
        u = dba.authUser(u);
        if (u != null){
            Session s = new Session(UUID.randomUUID().toString()); // TODO better session
            if (dba.addSession(u, s))
                return new Gson().toJson(new BaseResponse(true, null, new LoginResponse(s.getId(), u.isAdmin())));
        }
        return new Gson().toJson(new BaseResponse(false, null, null));
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/auth/register"}, method= RequestMethod.POST)
    public @ResponseBody String register(@RequestParam("username") String username,
                                         @RequestParam("password") String password,
                                         @RequestParam("email") String email){
        User u = new User(username);
        u.setPassword(password);
        u.setEmail(email);
        Session s = new Session(UUID.randomUUID().toString()); // TODO better session

        ObjectId userId = dba.addUser(u);

        if (userId == null){
            return new Gson().toJson(new BaseResponse(false, null, null));
        }

        u.setId(userId);

        if (dba.addSession(u, s)){
            return new Gson().toJson(new BaseResponse(true, null, new LoginResponse(s.getId(), u.isAdmin())));
        } else{
            return new Gson().toJson(new BaseResponse(false, null, null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/auth/logout"}, method= RequestMethod.POST)
    public @ResponseBody String logout(@RequestParam("sessionId") String sid){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);
        return new Gson().toJson(new BaseResponse(dba.removeSession(u, s), null, null));
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/movie/browse"}, method= RequestMethod.GET)
    public @ResponseBody String browseMovies(@RequestParam(value = "sessionId", required = false) String sid,
                                             @RequestParam(required = false, defaultValue = "date") String sortBy,
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
            u = dba.getUserFromSession(s);
        }

        List<Movie> movies = dba.getMovieList(sortBy, sortOrder, minRating, maxRating, director, actor, country,
                                              fromYear, toYear, genre, n, page);
        if (u != null){
            // TODO use a bulk operation
            for (Movie movie:movies){
                Rating r = dba.getUserRating(u, movie);
                if (r != null && r.getRating() != null)
                    movie.setUserRating(r.getRating());
            }
        }
        return new Gson().toJson(new BaseResponse(true, null, movies));
    }


    @CrossOrigin
    @RequestMapping(value={"/api/v1/movie/search"}, method= RequestMethod.GET)
    public @ResponseBody String searchMovie(@RequestParam(value = "sessionId", required = false) String sid,
                                             @RequestParam(required = true, defaultValue = "query") String query,
                                             @RequestParam(required = false, defaultValue = "10") int n,
                                             @RequestParam(required = false, defaultValue = "1") int page
    ){
        User u = null;
        if (sid != null){
            Session s = new Session(sid);
            u = dba.getUserFromSession(s);
        }

        List<Movie> movies = dba.searchMovie(query, n, page);
        if (u != null){
            // TODO use a bulk operation
            for (Movie movie:movies){
                Rating r = dba.getUserRating(u, movie);
                if (r != null && r.getRating() != null)
                    movie.setUserRating(r.getRating());
            }
        }
        return new Gson().toJson(new BaseResponse(true, null, movies));
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/movie/{movieId}"}, method= RequestMethod.GET)
    public @ResponseBody String getMovie(@RequestParam(value = "sessionId", required = false) String sid,
                                            @PathVariable("movieId") String movieId
    ){
        User u = null;
        if (sid != null){
            Session s = new Session(sid);
            u = dba.getUserFromSession(s);
        }

        Movie movie = dba.getMovieDetails(movieId);
        if (u != null){
            Rating r = dba.getUserRating(u, movie);
            if (r != null && r.getRating() != null)
                movie.setUserRating(r.getRating());
        }
        return new Gson().toJson(new BaseResponse(true, null, movie));
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/movie/{movieId}/rating"}, method= RequestMethod.PUT)
    public @ResponseBody String putRating(@RequestParam(value = "sessionId", required = true) String sid,
                                          @PathVariable("movieId") String movieId,
                                          @RequestParam(value = "rating", required = true) double ratingValue
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u == null){
            return new Gson().toJson(new BaseResponse(false, "Session ID does not match any active session. It may be expired.", null));
        }

        Movie movie = dba.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, ratingValue);
        boolean result = dba.insertRating(rating);
        if (result){
            return new Gson().toJson(new BaseResponse(true, null, null));
        } else {
            return new Gson().toJson(new BaseResponse(false, null, null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/movie/{movieId}/rating"}, method= RequestMethod.DELETE)
    public @ResponseBody String deleteRating(@RequestParam(value = "sessionId", required = true) String sid,
                                          @PathVariable("movieId") String movieId
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u == null){
            return new Gson().toJson(new BaseResponse(false, "Session ID does not match any active session. It may be expired.", null));
        }

        Movie movie = dba.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, 0.0);
        boolean result = dba.deleteRating(rating);
        if (result){
            return new Gson().toJson(new BaseResponse(true, null, null));
        } else {
            return new Gson().toJson(new BaseResponse(false, null, null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/movie/statistics"}, method= RequestMethod.GET)
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
        // TODO
        return new Gson().toJson(new BaseResponse(true, null, null));
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/user/{username}"}, method= RequestMethod.GET)
    public @ResponseBody String getUserProfile(@RequestParam(value = "sessionId") String sid,
                                         @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && (u.isAdmin() || u.getUsername().equals(username))){
            User u2 = dba.getUserProfile(username);
            if (u2 != null){
                return new Gson().toJson(new BaseResponse(true, null, u2));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/user/{username}/ratings"}, method= RequestMethod.GET)
    public @ResponseBody String getUserRatings(@RequestParam(value = "sessionId") String sid,
                                               @PathVariable("username") String username,
                                               @RequestParam(required = false, defaultValue = "10") int n,
                                               @RequestParam(required = false, defaultValue = "1") int page
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && (u.isAdmin() || u.getUsername().equals(username))){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                List<Rating> ratings = dba.getUserRatings(u2);
                return new Gson().toJson(new BaseResponse(true, null, ratings));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/user/{username}/rating/{movieId}"}, method= RequestMethod.GET)
    public @ResponseBody String getUserRating(@RequestParam(value = "sessionId") String sid,
                                               @PathVariable("username") String username,
                                               @PathVariable("movieId") String movieId
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && (u.isAdmin() || u.getUsername().equals(username))){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                Rating rating = dba.getUserRating(u2, new Movie(movieId));
                return new Gson().toJson(new BaseResponse(true, null, rating));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/user/{username}/rating/{movieId}"}, method= RequestMethod.POST)
    public @ResponseBody String putUserRating(@RequestParam(value = "sessionId") String sid,
                                              @PathVariable("username") String username,
                                              @PathVariable("movieId") String movieId,
                                              @RequestParam("rating") double ratingVal
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && (u.isAdmin() || u.getUsername().equals(username))){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                Rating rating = new Rating(u2, new Movie(movieId), ratingVal);
                boolean result = dba.insertRating(rating);
                if (result)
                    return new Gson().toJson(new BaseResponse(true, null, null));
                else
                    return new Gson().toJson(new BaseResponse(false, "Error inserting rating", null));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/user/{username}/rating/{movieId}"}, method= RequestMethod.DELETE)
    public @ResponseBody String deleteUserRating(@RequestParam(value = "sessionId") String sid,
                                              @PathVariable("username") String username,
                                              @PathVariable("movieId") String movieId
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && (u.isAdmin() || u.getUsername().equals(username))){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                Rating rating = new Rating(u2, new Movie(movieId), 0.0);
                boolean result = dba.deleteRating(rating);
                if (result)
                    return new Gson().toJson(new BaseResponse(true, null, null));
                else
                    return new Gson().toJson(new BaseResponse(false, "Error deleting rating", null));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/user/{username}/ban"}, method= RequestMethod.POST)
    public @ResponseBody String deleteUserRating(@RequestParam(value = "sessionId") String sid,
                                                 @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && u.isAdmin()){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                boolean result = dba.banUser(u2);
                if (result)
                    return new Gson().toJson(new BaseResponse(true, null, null));
                else
                    return new Gson().toJson(new BaseResponse(false, "Error banning user", null));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/user/search"}, method= RequestMethod.GET)
    public @ResponseBody String searchUser(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam String query,
                                           @RequestParam(required = false, defaultValue = "10") int limit
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && u.isAdmin()){
            List<User> users = dba.searchUser(query, limit, 1);
            return new Gson().toJson(new BaseResponse(true, null, users));
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/api/v1/ratings"}, method= RequestMethod.GET)
    public @ResponseBody String getAllRatings(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam(required = false, defaultValue = "10") int n,
                                           @RequestParam(required = false, defaultValue = "1") int page
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && u.isAdmin()){
            List<Rating> ratings = dba.getAllRatings(n, page);
            return new Gson().toJson(new BaseResponse(true, null, ratings));
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }


    public static void main(String[] args) {
        String connectionURI = null;
        String dbName = null;

        System.out.println(args.length);
        for (String arg:args){
            System.out.println(arg);
        }

        if (args.length == 2){
            connectionURI = args[0];
            dbName = args[1];

            dba = new DatabaseAdapter(connectionURI, dbName);
        } else{
            System.out.println("This executable takes 2 parameters: the connection URI and the database name.");
            return;
        }

        SpringApplication.run(Main.class, args);
    }

}