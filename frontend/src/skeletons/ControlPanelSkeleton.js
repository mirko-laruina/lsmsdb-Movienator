import React from 'react'
import { Typography } from '@material-ui/core'
import Skeleton from '@material-ui/lab/Skeleton'

export default function ControlPanel(props) {
    return (
        <React.Fragment>
            <Typography
                component="h1"
                variant="h3" >
                Admin control panel
                </Typography>
            <br />
            <Typography
                component="h2"
                variant="h4" >
                Find user
                </Typography>
            <br />
            <Skeleton />
            <br />
            <Typography
                component="h2"
                variant="h4" >
                Rating history
                </Typography>
            <br />
            <Skeleton />
            <Skeleton />
            <Skeleton />
            <br />
        </React.Fragment>
    )
}