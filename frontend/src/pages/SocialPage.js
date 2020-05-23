import React, { useEffect } from 'react';
import { Grid, Typography, Button } from '@material-ui/core'
import { Link } from 'react-router-dom'

import axios from 'axios'

import Pagination from '@material-ui/lab/Pagination'
import BasicPage from './BasicPage.js'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import UsersListDisplay from '../components/UsersListDisplay'

export default function SocialPage(props) {
    const [isAdmin, setIsAdmin] = React.useState(false);
    //True if the page we are trying to show regards the user who wants to display it
    const [isTargetUser, setIsTargetUser] = React.useState(false);
    const username = props.match.params.username;
    const [suggestedUsers, setSuggestedUsers] = React.useState([]);
    const [shownFollowers, setShownFollowers] = React.useState([]);
    const [shownFollowings, setShownFollowings] = React.useState([]);
    const [followersCurrentPage, setFollowersCurrentPage] = React.useState(1);
    const [followingsCurrentPage, setFollowingsCurrentPage] = React.useState(1);
    const [followersPageCount, setFollowersPageCount] = React.useState(0);
    const [followingsPageCount, setFollowingsPageCount] = React.useState(0);

    const followingsPerPage = 5;
    const followersPerPage = 5;
    const suggestionsPerPage = (isTargetUser ? 8 : null);


    useEffect(() => {
        if (localStorage.getItem('is_admin')) {
            setIsAdmin(true);
        }
        if (!props.match.params.username
            || props.match.params.username === localStorage.getItem('username')) {
            setIsTargetUser(true);
        }

        getAllSocial()
    }, [])

    const getAllSocial = () => {
        axios.get(baseUrl + "/user/" + username + "/social", {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                n_followings: followingsPerPage,
                n_followers: followersPerPage,
                n_suggestions: suggestionsPerPage
            }
        }).then((data) => {
            if (data.data.success) {
                setFollowers(data.data.followers);
                setFollowings(data.data.followings);
                if(isTargetUser){
                    setSuggested(data.data.suggested);
                }
            } else {
                errorHandler(data.data.success, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    const getFollowers = () => {
        axios.get(baseUrl + "/user/" + username + "/followers", {
            params: {
                sessionId: localStorage.getItem('sessionsId'),
                n: followersPerPage,
                page: followersCurrentPage
            }
        }).then((data) => {
            if(data.data.success){
                setFollowers(data.data)
            } else {
                errorHandler(data.data.success, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    const getFollowings = () => {
        axios.get(baseUrl + "/user/" + username + "/followings", {
            params: {
                sessionId: localStorage.getItem('sessionsId'),
                n: followingsPerPage,
                page: followingsCurrentPage
            }
        }).then((data) => {
            if(data.data.success){
                setFollowings(data.data)
            } else {
                errorHandler(data.data.success, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    const followHandler = (user, toFollow = true) => {
        axios.post(baseUrl + "/user/" + user + (toFollow ? "/follow" : "/unfollow"), null, {
            params: {
                sessionId: localStorage.getItem('sessionId'),
            }
        }).then((data) => {
            if(!data.data.success){
                alert("I couldn't " +(toFollow ? "follow" : "unfollow") + " the user!");
            } else {
                errorHandler(data.data.success, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    const setFollowers = (followers) => {
        setShownFollowers(followers.list);
        setFollowersPageCount(Math.ceil(followers.totalCount/followersPerPage))
    }

    const setFollowings = (followings) => {
        setShownFollowings(followings.list);
        setFollowingsPageCount(Math.ceil(followings.totalCount/followingsPerPage))
    }

    const setSuggested = (suggested) => {
        setSuggestedUsers(suggested.list);
    }


    useEffect(() => {
        getFollowers()
    }, [followersCurrentPage])

    useEffect(() => {
        getFollowings()
    }, [followingsCurrentPage])
    

    return (
        <BasicPage history={props.history}>
            <Grid container alignItems="center" spacing={1}>
                <Grid item xs={isTargetUser ? 9 : 7}>
                    <Typography variant="h3" component="h1">
                        {isTargetUser ? "Your social profile" : username + " social"}
                    </Typography>
                </Grid>
                <Grid item xs={3}>
                    <Button
                        component={Link}
                        to={"/profile/" + (isTargetUser ? "" : username)}
                        variant="outlined"
                        color="primary"
                        size="large"
                        fullWidth
                    >
                        Profile
                    </Button>
                </Grid>
                {!isTargetUser &&
                    <Grid item xs={2}>
                        <Button
                            component={Link}
                            to={"/profile/" + (isTargetUser ? "" : username)}
                            variant="outlined"
                            color="primary"
                            size="large"
                            fullWidth
                        >
                            Follow
                    </Button>
                    </Grid>
                }
            </Grid>
            <br />
            {
                (isTargetUser || isAdmin) &&
                <>
                    <Typography variant="h4" component="h2">
                        {"Share interests with " + (isTargetUser ? "you" : username) + ":"}
                    </Typography>
                    <UsersListDisplay users={suggestedUsers} horizontal />
                    <br />
                </>
            }
            <Grid container spacing={1}>
                <Grid item xs={6}>
                    <Typography variant="h4" component="h2" align="center">
                        Followers
                    </Typography>
                    <br />
                    <UsersListDisplay
                        showFollow
                        users={shownFollowers}
                        followHandler />
                    <Grid container justify="center">
                            <Pagination shape="rounded"
                                showFirstButton
                                showLastButton
                                color="primary"
                                size="large"
                                count={followersPageCount}
                                page={followersCurrentPage}
                                onChange={(e, v) => setFollowersCurrentPage(v)} />
                    </Grid>
                </Grid>
                <Grid item xs={6}>
                    <Typography variant="h4" component="h2" align="center">
                        Following
                    </Typography>
                    <br />
                    <UsersListDisplay
                        showFollow
                        users={shownFollowings}
                        followHandler/>
                    <Grid container justify="center">
                            <Pagination shape="rounded"
                                showFirstButton
                                showLastButton
                                color="primary"
                                size="large"
                                count={followingsPageCount}
                                page={followingsCurrentPage}
                                onChange={(e, v) => setFollowingsCurrentPage(v)} />
                    </Grid>
                </Grid>
            </Grid>
        </BasicPage>
    )
}