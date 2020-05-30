import React, { useEffect } from 'react';
import { Grid, Typography, Button } from '@material-ui/core'
import { Link } from 'react-router-dom'

import axios from 'axios'

import Pagination from '@material-ui/lab/Pagination'
import BasicPage from './BasicPage.js'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import UsersListDisplay from '../components/UsersListDisplay'
import FollowingsRatingTable from '../components/FollowingsRatingTable.js';
import FollowButton from '../components/FollowButton.js';
import MyPagination from '../components/MyPagination.js';

export default function SocialPage(props) {
    const [isAdmin, setIsAdmin] = React.useState(false);
    const [firstLoad, setFirstLoad] = React.useState(true);
    //True if the page we are trying to show regards the user who wants to display it
    const [isTargetUser, setIsTargetUser] = React.useState(false);
    const username = props.match.params.username ? props.match.params.username : localStorage.getItem('username');
    const [suggestedUsers, setSuggestedUsers] = React.useState([]);
    const [shownFollowers, setShownFollowers] = React.useState([]);
    const [shownFollowings, setShownFollowings] = React.useState([]);
    const [followersCurrentPage, setFollowersCurrentPage] = React.useState(1);
    const [followingsCurrentPage, setFollowingsCurrentPage] = React.useState(1);
    const [followersLastPage, setFollowersLastPage] = React.useState(true);
    const [followingsLastPage, setFollowingsLastPage] = React.useState(true);

    const followingsPerPage = 5;
    const followersPerPage = 5;
    const suggestionsPerPage = 8;


    useEffect(() => {
        if (localStorage.getItem('is_admin') === 'true') {
            setIsAdmin(true);
        }
        if (!props.match.params.username
            || props.match.params.username === localStorage.getItem('username')) {
            setIsTargetUser(true);
        }

        getAllSocial()
        setFirstLoad(false);
    }, [])

    const getAllSocial = () => {
        let params = {
            sessionId: localStorage.getItem('sessionId'),
            n_followings: followingsPerPage,
            n_followers: followersPerPage,
        }
        if (isTargetUser) {
            params.n_suggestions = suggestionsPerPage
        }
        axios.get(baseUrl + "/user/" + username + "/social", {
            params: params
        }).then((data) => {
            if (data.data.success) {
                setFollowers(data.data.response.followers);
                setFollowings(data.data.response.followings);
                if (isTargetUser) {
                    setSuggested(data.data.response.suggestions);
                }
            } else {
                errorHandler(data.data.code, data.data.message)
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
            if (data.data.success) {
                setFollowers(data.data.response)
            } else {
                errorHandler(data.data.code, data.data.message)
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
            if (data.data.success) {
                setFollowings(data.data.response)
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    const setFollowers = (followers) => {
        setShownFollowers(followers.list);
        setFollowersLastPage(followers.lastPage)
    }

    const setFollowings = (followings) => {
        setShownFollowings(followings.list);
        setFollowingsLastPage(followings.lastPage)
    }

    const setSuggested = (suggested) => {
        setSuggestedUsers(suggested.list);
    }


    useEffect(() => {
        if (!firstLoad)
            getFollowers()
    }, [followersCurrentPage])

    useEffect(() => {
        if (!firstLoad)
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
                        <FollowButton
                            fullWidth
                            size="large"
                            user={username}
                            onClick={() => { getAllSocial() }}
                            following={true}
                        />
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
                    <br />
                    <UsersListDisplay
                        users={suggestedUsers}
                        horizontal
                        emptyMessage="I can't suggest any user, try following someone first."
                    />
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
                        followHandler={getAllSocial}
                        emptyMessage={(isTargetUser ? "You don't" : (username + " doesn't")) + " have any follower"} />
                    <Grid container justify="center">
                        <MyPagination 
                            currentPage={followersCurrentPage}
                            lastPage={followersLastPage}
                            noBorder
                            noText
                            onClick={(v) => setFollowersCurrentPage(v)} />
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
                        followHandler={getAllSocial}
                        emptyMessage={(isTargetUser ? "You don't" : (username + " doesn't")) + " follow anyone"} />
                    <Grid container justify="center">
                        <MyPagination 
                            currentPage={followingsCurrentPage}
                            lastPage={followingsLastPage}
                            noBorder
                            noText
                            onClick={(v) => setFollowingsCurrentPage(v)} />
                    </Grid>
                </Grid>

                <br />
                {
                    isTargetUser &&
                    <React.Fragment>
                        <Typography variant="h4" component="h2">
                            {"Hot among your followings"}
                        </Typography>
                        <br />
                        <br />
                        <br />
                        <FollowingsRatingTable />
                    </React.Fragment>
                }
            </Grid>
        </BasicPage>
    )
}