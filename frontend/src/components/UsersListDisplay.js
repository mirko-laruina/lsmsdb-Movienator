import React from 'react'
import { Grid, Button, Typography } from '@material-ui/core'
import { Link } from 'react-router-dom'
import UsersListElement from './UsersListElement'

export default function UsersListDisplay(props) {
    return (
        props.users && props.users.length !== 0 ?
            <Grid container>
                {
                    props.users.map((user, i) => {
                        return (
                            <Grid key={i} container item xs={props.horizontal ? 3 : 12} alignItems='center'>
                                {
                                    !props.showFollow ?
                                        <Link to={"/profile/" + user.username}>
                                            <UsersListElement user={user.username} />
                                        </Link>
                                        :
                                        <>
                                            <Grid item xs={8}>
                                                <Link to={"/profile/" + user.username}>
                                                    <UsersListElement user={user.username} />
                                                </Link>
                                            </Grid>
                                            <Grid item xs={3}>
                                                {
                                                    !user.following ?
                                                        <Button
                                                            fullWidth
                                                            variant="outlined"
                                                            color="primary"
                                                            size="small"
                                                            onClick={() => {
                                                                props.followHandler(user.username)
                                                            }}>
                                                            Follow
                                                        </Button>
                                                        :
                                                        <Button
                                                            fullWidth
                                                            variant="contained"
                                                            color="primary"
                                                            size="small"
                                                            onClick={() => {
                                                                props.followHandler(user.username, false)
                                                            }}>
                                                            Unfollow
                                                        </Button>
                                                }
                                            </Grid>
                                        </>
                                }
                            </Grid>
                        )
                    })
                }
            </Grid>
            :
            <Typography variant="body1" component="p">
                {props.emptyMessage}
            </Typography>
    )
}