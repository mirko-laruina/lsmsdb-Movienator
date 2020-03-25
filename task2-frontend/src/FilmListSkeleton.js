import React from 'react'
import {Grid, Divider} from '@material-ui/core'
import {Skeleton} from '@material-ui/lab'

export default function FilmListSkeleton() {
    return (
        <div>
            <Grid container>
                <Grid item xs={3}>
                    <Skeleton
                        animation='wave'
                        variant='rect'
                        width='150px'
                        height='180px' />
                    <br />

                </Grid>
                <Grid item xs={9}>
                    <Skeleton />
                    <Skeleton />
                    <Skeleton />
                    <Skeleton />
                    <Skeleton />

                </Grid>
            </Grid>
            <Divider component="li" />
            <br />
        </div>
    )
}