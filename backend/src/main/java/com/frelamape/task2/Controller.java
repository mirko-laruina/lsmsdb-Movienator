package com.frelamape.task2;

import com.frelamape.task2.api.LoginResponse;
import com.frelamape.task2.api.ResponseHelper;
import com.frelamape.task2.db.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main class
 *
 * Refer to docs/api.md for the description of the methods.
 */
@RestController
@RequestMapping("/api/v1")
public class Controller {
    @Autowired
    private DatabaseAdapter dba;

    @CrossOrigin
    @RequestMapping(value={"/auth/login"}, method= RequestMethod.POST)
    public @ResponseBody String login(@RequestParam("username") String username,
                                      @RequestParam("password") String password){
        User u = new User(username);
        u.setPassword(password);
        u = dba.authUser(u);

        if (u == null)
            return ResponseHelper.wrongCredentials();

        Session s = new Session(UUID.randomUUID().toString()); // TODO better session
        if (!u.isBanned()){
            if (dba.addSession(u, s))
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
        User u = dba.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        u.setPassword(password);

        boolean result = dba.editUserPassword(u);
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

        ObjectId userId = dba.addUser(u);

        if (userId == null){
            return ResponseHelper.genericError("User already exists.");
        }

        u.setId(userId);

        if (dba.addSession(u, s)){
            return ResponseHelper.success(new LoginResponse(s.getId(), u.isAdmin()));
        } else{
            return ResponseHelper.mongoError("Unknown error");
        }
    }

    @CrossOrigin
    @RequestMapping(value={"/auth/logout"}, method= RequestMethod.POST)
    public @ResponseBody String logout(@RequestParam("sessionId") String sid){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (dba.removeSession(u, s)) {
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
                return ResponseHelper.genericError("Unrecognized sortBy value: " + sortBy);
        }

        QuerySubset<Movie> querySubset = dba.getMovieList(realSortBy, sortOrder, minRating, maxRating, director, actor, country,
                                              fromYear, toYear, genre, n, page);
        if (u != null){
            dba.fillUserRatings(u, querySubset.getList());
        }
        return ResponseHelper.success(querySubset);
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
            u = dba.getUserFromSession(s);
        }

        Movie movie = dba.getMovieDetails(movieId);
        if (movie != null) {
            if (u != null) {
                Rating r = dba.getUserRating(u, movie);
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
        User u = dba.getUserFromSession(s);

        if (u == null){
            return ResponseHelper.invalidSession();
        }

        Movie movie = dba.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, ratingValue);
        dba.insertRating(rating);

        return ResponseHelper.success(null);
    }

    @CrossOrigin
    @RequestMapping(value={"/movie/{movieId}/rating"}, method= RequestMethod.DELETE)
    public @ResponseBody String deleteRating(@RequestParam(value = "sessionId", required = true) String sid,
                                          @PathVariable("movieId") String movieId
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u == null){
            return ResponseHelper.invalidSession();
        }

        Movie movie = dba.getMovieDetails(movieId);
        Rating rating = new Rating(u, movie, 0.0);
        boolean result = dba.deleteRating(rating);
        if (result){
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

        QuerySubset<Statistics<Statistics.Aggregator>> statistics = dba.getStatistics(groupBy, realSortBy, sortOrder,
                minRating, maxRating, director, actor, country, fromYear, toYear, genre, n, page);

        return ResponseHelper.success(statistics);
    }

    @CrossOrigin
    @RequestMapping(value={"/user/{username}"}, method= RequestMethod.GET)
    public @ResponseBody String getUserProfile(@RequestParam(value = "sessionId") String sid,
                                         @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin() || u.getUsername().equals(username)){
            User u2 = dba.getUserProfile(username);
            if (u2 != null){
                return ResponseHelper.success(u2);
            } else {
                return ResponseHelper.notFound();
            }
        } else {
            return ResponseHelper.unauthorized();
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

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin() || u.getUsername().equals(username)){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                QuerySubset<RatingExtended> ratings = dba.getUserRatings(u2, n, page);
                return ResponseHelper.success(ratings);
            } else {
                return ResponseHelper.notFound();
            }
        } else {
            return ResponseHelper.unauthorized();
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

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin() || u.getUsername().equals(username)){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                Rating rating = dba.getUserRating(u2, new Movie(movieId));
                return ResponseHelper.success(rating);
            } else {
                return ResponseHelper.notFound();
            }
        } else {
            return ResponseHelper.unauthorized();
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

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin() || u.getUsername().equals(username)){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                Movie movie = new Movie(movieId);
                Rating rating = new Rating(u2, movie, ratingVal);
                dba.insertRating(rating);
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
        User u = dba.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin() || u.getUsername().equals(username)){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                Rating rating = new Rating(u2, new Movie(movieId), 0.0);
                boolean result = dba.deleteRating(rating);
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
    @RequestMapping(value={"/user/{username}/ban"}, method= RequestMethod.POST)
    public @ResponseBody String banUser(@RequestParam(value = "sessionId") String sid,
                                                 @PathVariable("username") String username
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin()){
            User u2 = dba.getUserLoginInfo(username);
            if (u2 != null){
                boolean result = dba.banUser(u2);
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
    public @ResponseBody String searchUser(@RequestParam(value = "sessionId") String sid,
                                           @RequestParam String query,
                                           @RequestParam(required = false, defaultValue = "10") int limit
    ){
        Session s = new Session(sid);
        User u = dba.getUserFromSession(s);

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin()){
            QuerySubset<User> users = dba.searchUser(query, limit, 1);
            List<String> result = new ArrayList<>();
            for (User user: users.getList()){
                result.add(user.getUsername());
            }
            return ResponseHelper.success(result);
        } else {
            return ResponseHelper.unauthorized();
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

        if (u == null)
            return ResponseHelper.invalidSession();

        if (u.isAdmin()){
            QuerySubset<RatingExtended> ratings = dba.getAllRatings(n, page);
            return ResponseHelper.success(ratings);
        } else {
            return ResponseHelper.unauthorized();
        }
    }
}