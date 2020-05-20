import React, { useEffect } from 'react';
import { Grid, Typography, Button } from '@material-ui/core'
import { Link } from 'react-router-dom'
import BasicPage from './BasicPage.js'
import UsersListDisplay from './UsersListDisplay'

export default function SocialPage(props) {
    const [isAdmin, setIsAdmin] = React.useState(false);
    //True if the page we are trying to show regards the user who wants to display it
    const [isTargetUser, setIsTargetUser] = React.useState(false);
    const username = props.match.params.username;
    const users = ["username1", "username2", "usernameN", "username2", "usernameN", "username2", "usernameN"]

    useEffect(() => {
        if (localStorage.getItem('is_admin')) {
            setIsAdmin(true);
        }
        if (!props.match.params.username
            || props.match.params.username === localStorage.getItem('username')) {
            setIsTargetUser(true);
        }

    }, [])

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
                    <UsersListDisplay showFollow users={users} />
                </Grid>
                <Grid item xs={6}>
                    <Typography variant="h4" component="h2" align="center">
                        Following
                    </Typography>
                    <br />
                    <UsersListDisplay showFollow users={users} />
                </Grid>
            </Grid>
        </BasicPage>
    )
}