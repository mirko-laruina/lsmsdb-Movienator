import React from 'react'
import { Grid, Button, Typography } from '@material-ui/core'
import { Link } from 'react-router-dom'
import UsersListElement from './UsersListElement'
import FollowButton from './FollowButton'

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
                                                <FollowButton
                                                    fullWidth
                                                    size="small"
                                                    user={user.username}
                                                    following={user.following}
                                                    onClick={() => props.followHandler()}
                                                />
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