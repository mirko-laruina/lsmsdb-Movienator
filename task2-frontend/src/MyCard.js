import React from 'react';
import { Card, CardContent } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
    paperRoot: {
        color: theme.palette.primary.main,
      },
}))

export default function MyCard(props){
    const classes = useStyles();

    return (
        <Card elevation={5} classes={{ root: classes.paperRoot }}>
            <CardContent>
                {props.children}
            </CardContent>
        </Card>
    )
}