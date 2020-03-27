package com.frelamape.task2.db;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
