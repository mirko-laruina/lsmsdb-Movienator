import React from 'react'
import { Grid, Button } from '@material-ui/core'
import { Link } from 'react-router-dom'
import UsersListElement from './UsersListElement'

export default function UsersListDisplay(props) {
    return (
        <Grid container>
            {
                props.users.map((user, i) => {
                    return (
                        <Grid key={i} container item xs={props.horizontal ? 3 : 12} alignItems='center'>
                            {
                                !props.showFollow ?
                                    <Link to={"/profile/" + user}>
                                        <UsersListElement user={user} />
                                    </Link>
                                    :
                                    <>
                                        <Grid item xs={8}>
                                            <Link to={"/profile/" + user}>
                                                <UsersListElement user={user} />
                                            </Link>
                                        </Grid>
                                        <Grid item xs={3}>
                                            {
                                                Math.random() < 0.5 ?
                                                    <Button
                                                        fullWidth
                                                        variant="outlined"
                                                        color="primary"
                                                        size="small">
                                                        Follow
                                                </Button>
                                                    :
                                                    <Button
                                                        fullWidth
                                                        variant="contained"
                                                        color="primary"
                                                        size="small">
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
    )
}