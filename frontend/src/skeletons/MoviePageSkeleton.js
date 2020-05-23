import React from 'react'
import { Grid } from '@material-ui/core'
import { Skeleton } from '@material-ui/lab'

export default function MoviePageSkeleton() {
    return (
        <React.Fragment>
            <Skeleton variant="rect" height={60} />
            <br />
            <Grid container spacing={6}>
                <Grid item xs={4}>
                    <Skeleton variant="rect" height={300} />
                </Grid>
                <Grid item xs={8}>
                    <Skeleton />
                    <Skeleton />
                    <Skeleton />
                    <Skeleton />
                    <Skeleton />
                </Grid>
            </Grid>
            <br />
            <Skeleton />
            <Skeleton />
            <Skeleton />
        </React.Fragment>
    )
}