package com.frelamape.task2.api;

import com.frelamape.task2.db.QuerySubset;
import com.frelamape.task2.db.User;

public class SocialProfileResponse {
    private QuerySubset<User> followers;
    private QuerySubset<User> followings;
    private QuerySubset<User> suggestions;
    private User.Relationship relationship;

    public SocialProfileResponse(QuerySubset<User> followers, QuerySubset<User> followings,
            QuerySubset<User> suggestions, User.Relationship relationship) {
        this.followers = followers;
        this.followings = followings;
        this.suggestions = suggestions;
        this.relationship = relationship;
    }

    public User.Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(User.Relationship relationship) {
        this.relationship = relationship;
    }

    public QuerySubset<User> getFollowers() {
        return followers;
    }

    public void setFollowers(QuerySubset<User> followers) {
        this.followers = followers;
    }

    public QuerySubset<User> getFollowings() {
        return followings;
    }

    public void setFollowings(QuerySubset<User> followings) {
        this.followings = followings;
    }

    public QuerySubset<User> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(QuerySubset<User> suggestions) {
        this.suggestions = suggestions;
    }
}
