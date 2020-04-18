package com.frelamape.task2;

import com.frelamape.task2.api.BaseResponse;
import com.frelamape.task2.api.LoginResponse;
import com.frelamape.task2.db.*;
import com.google.gson.Gson;
import org.bson.types.ObjectId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@EnableAutoConfiguration
public class Main {
    private static DatabaseAdapter dba;

    @CrossOrigin
    @RequestMapping(value={"/auth/login"}, method= RequestMethod.POST)
    public @ResponseBody String login(@RequestParam("username") String username,
                                      @RequestParam("password") String password){
        User u = new User(username);
        u.setPassword(password);
        u = dba.authUser(u);
        if (u != null){
            Session s = new Session(UUID.randomUUID().toString()); // TODO better session
            if (!u.isBanned()){
                if (dba.addSession(u, s))
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
        User u = dba.getUserFromSession(s);
        u.setPassword(password);

        boolean result = dba.editUserPassword(u);
        
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
    @RequestMapping(value={"/auth/logout"}, method= RequestMethod.POST)
    public @ResponseBody String logout(@RequestParam("sessionId") String sid){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);
        return new Gson().toJson(new BaseResponse(dba.removeSession(u, s), null, null));
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
            u = dba.getUserFromSession(s);
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

        QuerySubset<Movie> querySubset = dba.getMovieList(realSortBy, sortOrder, minRating, maxRating, director, actor, country,
                                              fromYear, toYear, genre, n, page);
        if (u != null){
            dba.fillUserRatings(u, querySubset.getList());
        }
        return new Gson().toJson(new BaseResponse(true, null, querySubset));
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
            u = dba.getUserFromSession(s);
        }

        QuerySubset<Movie> movies = dba.searchMovie(query, n, page);
        if (u != null){
            dba.fillUserRatings(u, movies.getList());
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
    @RequestMapping(value={"/movie/{movieId}/rating"}, method= RequestMethod.PUT)
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
        int result = dba.insertRating(rating);
        switch (result){
            case 0:
                return new Gson().toJson(new BaseResponse(true, "Rating added", null));
            case 1:
                return new Gson().toJson(new BaseResponse(true, "Rating updated", null));
            default:
                return new Gson().toJson(new BaseResponse(false, "Unknown error inserting rating", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/{movieId}/rating"}, method= RequestMethod.DELETE)
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
        // TODO
        return new Gson().toJson(new BaseResponse(true, null, null));
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}"}, method= RequestMethod.GET)
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
    @RequestMapping(value={"/user/{username}/ratings"}, method= RequestMethod.GET)
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
                QuerySubset<RatingExtended> ratings = dba.getUserRatings(u2, n, page);
                return new Gson().toJson(new BaseResponse(true, null, ratings));
            } else {
                return new Gson().toJson(new BaseResponse(false, "User not found", null));
            }
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}/rating/{movieId}"}, method= RequestMethod.GET)
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
    @RequestMapping(value={"/user/{username}/rating/{movieId}"}, method= RequestMethod.POST)
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
                int result = dba.insertRating(rating);
                switch (result) {
                    case 0:
                        return new Gson().toJson(new BaseResponse(true, "Rating added", null));
                    case 1:
                        return new Gson().toJson(new BaseResponse(true, "Rating updated", null));
                    default:
                        return new Gson().toJson(new BaseResponse(false, "Unkonwn error inserting rating", null));
                }
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
    @RequestMapping(value={"/user/{username}/ban"}, method= RequestMethod.POST)
    public @ResponseBody String banUser(@RequestParam(value = "sessionId") String sid,
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
    @RequestMapping(value={"/user/search"}, method= RequestMethod.GET)
    public @ResponseBody String searchUser(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam String query,
                                           @RequestParam(required = false, defaultValue = "10") int limit
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && u.isAdmin()){
            QuerySubset<User> users = dba.searchUser(query, limit, 1);
            List<String> result = new ArrayList<>();
            for (User user: users.getList()){
                result.add(user.getUsername());
            }
            return new Gson().toJson(new BaseResponse(true, null, result));
        } else {
            return new Gson().toJson(new BaseResponse(false, "Unauthorized", null));
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/ratings"}, method= RequestMethod.GET)
    public @ResponseBody String getAllRatings(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam(required = false, defaultValue = "10") int n,
                                           @RequestParam(required = false, defaultValue = "1") int page
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u != null && u.isAdmin()){
            QuerySubset<RatingExtended> ratings = dba.getAllRatings(n, page);
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