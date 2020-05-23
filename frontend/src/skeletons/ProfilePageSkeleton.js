import React from 'react'
import { Grid } from '@material-ui/core'
import { Typography } from '@material-ui/core'
import Skeleton from '@material-ui/lab/Skeleton'

export default function ProfilePage(props) {

    return (
        <React.Fragment>
            <Typography variant="h3" component="h1">
                Profile page
                </Typography>
            <br />
            <Grid container>
                <Grid item xs={9}>
                    <Typography variant="h4" component='h2'>
                        Account details
                        </Typography>
                </Grid>
            </Grid>
            <br />
            <Skeleton />
            <Skeleton />
            <br />
            <Typography variant="h4" component='h2'>
                The things you like
                            </Typography>
            <br />
            <Typography variant="h5" component='h3'>
                Actors
                            </Typography>
            <br />
            <Skeleton />
            <Skeleton />
            <Skeleton />
            <br />
            <Typography variant="h5" component='h3'>
                Directors
                            </Typography>
            <br />
            <Skeleton />
            <Skeleton />
            <Skeleton />
            <br />
            <Typography variant="h5" component='h3'>
                Genres
                            </Typography>
            <br />
            <Skeleton />
            <Skeleton />
            <Skeleton />
            <br />
            {
                props.showPw &&
                <React.Fragment>
                    <Typography variant="h4" component='h2'>
                        Change password
                    </Typography>
                    <br />
                    <Skeleton />
                    <Skeleton />
                    <Skeleton />
                </React.Fragment>
            }
        </React.Fragment>
    )
}