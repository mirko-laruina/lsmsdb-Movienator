import React, { useEffect } from 'react';
import { Grid, Typography, Button } from '@material-ui/core'
import { Link } from 'react-router-dom'

import Pagination from '@material-ui/lab/Pagination'
import BasicPage from './BasicPage.js'
import UsersListDisplay from '../components/UsersListDisplay'

export default function SocialPage(props) {
    const [isAdmin, setIsAdmin] = React.useState(false);
    //True if the page we are trying to show regards the user who wants to display it
    const [isTargetUser, setIsTargetUser] = React.useState(false);
    const username = props.match.params.username;
    const users = ["username1", "username2", "usernameN", "username2", "usernameN", "username2", "usernameN"]
    const [shownFollowers, setShownFollowers] = React.useState(users.slice(1, 6));
    const [shownFollowings, setShownFollowings] = React.useState(users.slice(1, 6));
    const [followersCurrentPage, setFollowersCurrentPage] = React.useState(1);
    const [followingsCurrentPage, setFollowingsCurrentPage] = React.useState(1);
    const [followersPageCount, setFollowersPageCount] = React.useState(Math.ceil(users.length/5));
    const [followingsPageCount, setFollowingsPageCount] = React.useState(Math.ceil(users.length/5));


    useEffect(() => {
        if (localStorage.getItem('is_admin')) {
            setIsAdmin(true);
        }
        if (!props.match.params.username
            || props.match.params.username === localStorage.getItem('username')) {
            setIsTargetUser(true);
        }

    }, [])

    useEffect(() => {
        setShownFollowers(users.slice((followersCurrentPage-1)*5, followersCurrentPage*5))
    }, [followersCurrentPage])

    useEffect(() => {
        setShownFollowings(users.slice((followingsCurrentPage-1)*5, followingsCurrentPage*5))
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
                    <UsersListDisplay users={users} horizontal />
                    <br />
                </>
            }
            <Grid container spacing={1}>
                <Grid item xs={6}>
                    <Typography variant="h4" component="h2" align="center">
                        Followers
                    </Typography>
                    <br />
                    <UsersListDisplay showFollow users={shownFollowers} />
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
                    <UsersListDisplay showFollow users={shownFollowings} />
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