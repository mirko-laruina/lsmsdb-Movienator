import React, { useEffect } from 'react';
import { Grid, Typography, Button, Box } from '@material-ui/core'
import { Link } from 'react-router-dom'

import axios from 'axios'

import Pagination from '@material-ui/lab/Pagination'
import BasicPage from './BasicPage.js'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import UsersListDisplay from '../components/UsersListDisplay'
import FollowingsRatingTable from '../components/FollowingsRatingTable.js';
import FollowButton from '../components/FollowButton.js';
import MyPagination from '../components/MyPagination.js';
import { Skeleton } from '@material-ui/lab';

export default function SocialPage(props) {
    const isAnon = localStorage.getItem('username') ? false : true
    const [isAdmin, setIsAdmin] = React.useState(false);
    const [firstLoad, setFirstLoad] = React.useState(true);
    //True if the page we are trying to show regards the user who wants to display it
    const username = props.match.params.username ? props.match.params.username : localStorage.getItem('username');
    const isTargetUser = (!props.match.params.username || props.match.params.username === localStorage.getItem('username'))
    const [suggestedUsers, setSuggestedUsers] = React.useState([]);
    const [shownFollowers, setShownFollowers] = React.useState([]);
    const [shownFollowings, setShownFollowings] = React.useState([]);
    const [followersCurrentPage, setFollowersCurrentPage] = React.useState(1);
    const [followingsCurrentPage, setFollowingsCurrentPage] = React.useState(1);
    const [followersLastPage, setFollowersLastPage] = React.useState(true);
    const [followingsLastPage, setFollowingsLastPage] = React.useState(true);
    const [loadingFollowers, setLoadingFollowers] = React.useState(true);
    const [loadingFollowings, setLoadingFollowings] = React.useState(true);
    const [loadingPage, setLoadingPage] = React.useState(true);
    const [relationship, setRelationship] = React.useState({});

    const followingsPerPage = 5;
    const followersPerPage = 5;
    const suggestionsPerPage = 8;


    useEffect(() => {
        if (localStorage.getItem('is_admin') === 'true') {
            setIsAdmin(true);
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

        setLoadingFollowers(true)
        setLoadingFollowings(true)
        setLoadingPage(true)
        axios.get(baseUrl + "/user/" + username + "/social", {
            params: params
        }).then((data) => {
            if (data.data.success) {
                setFollowers(data.data.response.followers);
                setLoadingFollowers(false)
                setFollowings(data.data.response.followings);
                setLoadingFollowings(false)
                if (data.data.response.relationship)
                    setRelationship(data.data.response.relationship)
                if (isTargetUser) {
                    setSuggested(data.data.response.suggestions);
                }
                setLoadingPage(false)
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    const getFollowers = () => {
        setLoadingFollowers(true)
        axios.get(baseUrl + "/user/" + username + "/followers", {
            params: {
                sessionId: localStorage.getItem('sessionsId'),
                n: followersPerPage,
                page: followersCurrentPage
            }
        }).then((data) => {
            if (data.data.success) {
                setFollowers(data.data.response)
                setLoadingFollowers(false)
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    const getFollowings = () => {
        setLoadingFollowings(true)
        axios.get(baseUrl + "/user/" + username + "/followings", {
            params: {
                sessionId: localStorage.getItem('sessionsId'),
                n: followingsPerPage,
                page: followingsCurrentPage
            }
        }).then((data) => {
            if (data.data.success) {
                setFollowings(data.data.response)
                setLoadingFollowings(false)
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
                <Grid item xs={(isTargetUser || isAnon) ? 9 : 7}>
                    <Typography variant="h3" component="h1">
                        {isTargetUser ? "Your social profile" : username + " social"}
                    </Typography>
                </Grid>
                {
                    !loadingPage &&
                    <React.Fragment>
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
                        {!isTargetUser && !isAnon &&
                            <Grid item xs={2}>
                                <FollowButton
                                    fullWidth
                                    size="large"
                                    user={username}
                                    onClick={() => { getAllSocial() }}
                                    following={relationship.following}
                                />
                            </Grid>
                        }
                    </React.Fragment>
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
                    {
                        loadingPage
                            ?
                            <>
                                <Skeleton />
                                <Skeleton />
                                <Skeleton />
                            </>
                            :
                            <UsersListDisplay
                                users={suggestedUsers}
                                horizontal
                                emptyMessage="I can't suggest any user, try following someone first."
                            />
                    }
                    <br />
                </>
            }
            <Grid container spacing={4}>
                <Grid item xs={6}>
                    <Typography variant="h4" component="h2" align="center">
                        Followers
                    </Typography>
                    <br />
                    {
                        loadingFollowers ?
                            Array(5).fill().map((v, i) => {
                                return (
                                    <Skeleton key={i} height='70px' />
                                )
                            })
                            :
                            <UsersListDisplay
                                showFollow={!isAnon}
                                noFollowUser={localStorage.getItem('username')}
                                users={shownFollowers}
                                followHandler={getAllSocial}
                                emptyMessage={(isTargetUser ? "You don't" : (username + " doesn't")) + " have any follower"} />
                    }
                    <Box justify="center">
                        <MyPagination
                            currentPage={followersCurrentPage}
                            lastPage={followersLastPage}
                            noBorder
                            noText
                            onClick={(v) => setFollowersCurrentPage(v)} />
                    </Box>
                </Grid>
                <Grid item xs={6}>
                    <Typography variant="h4" component="h2" align="center">
                        Following
                    </Typography>
                    <br />
                    {
                        loadingFollowings ?
                            Array(5).fill().map((v, i) => {
                                return (
                                    <Skeleton key={i} height='70px' />
                                )
                            })
                            :
                            <UsersListDisplay
                                showFollow={!isAnon}
                                noFollowUser={localStorage.getItem('username')}
                                users={shownFollowings}
                                followHandler={getAllSocial}
                                emptyMessage={(isTargetUser ? "You don't" : (username + " doesn't")) + " follow anyone"} />
                    }
                    <Box justify="center">
                        <MyPagination
                            currentPage={followingsCurrentPage}
                            lastPage={followingsLastPage}
                            noBorder
                            noText
                            onClick={(v) => setFollowingsCurrentPage(v)} />
                    </Box>
                </Grid>

                <br />
                {
                    isTargetUser &&
                    <React.Fragment>
                        <Typography variant="h4" component="h2">
                            {"Hot among your followings"}
                        </Typography>
                        {
                            loadingPage ?
                                <Skeleton height="280px" width="100%" />
                                :
                                <Box my={2} width={1}>
                                    <FollowingsRatingTable />
                                </Box>
                        }
                    </React.Fragment>
                }
            </Grid>
        </BasicPage>
    )
}