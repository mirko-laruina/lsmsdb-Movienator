package com.frelamape.task2.db;

import com.frelamape.task2.db.Movie;
import com.frelamape.task2.db.Rating;
import com.frelamape.task2.db.User;

public class RatingExtended extends Rating {
    private String title;
    private String username;
    private Integer year;

    public RatingExtended(Movie m, User u, Rating r) {
        super(r.getUserId(), r.getMovieId(), r.getDate(), r.getRating());
        this.title = m.getTitle();
        this.year = m.getYear();
        this.username = u.getUsername();
    }
}
