import React, { Component } from 'react'

import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'
import {
    List, ListItem, ListItemIcon, ListItemAvatar,
    Avatar, Divider, ListItemText, Typography, Chip, Dialog, Button, Grid
} from '@material-ui/core'

import Rating from '@material-ui/lab/Rating';
import Filters from './Filters.js';

const styles = {
    img: {
        paddingRight: '1.5em',
    },
    genre: {
        marginRight: '0.5em',
        fontSize: '1em',
        padding: '0px',
    },
    cardRoot: {
        padding: '3em',
    }
}

const queryOut = [
    {
        "_id": "tt7286456",
        "title": "Joker",
        "year": 2019,
        "poster": "https://picsum.photos/130/180",
        "genres": ["Crime", "Drama", "Thriller"],
        "total_rating": 8.87,
        "user_rating": 7
    },
    {
        "_id": "tt7286456",
        "title": "Joker",
        "year": 2019,
        "poster": "https://picsum.photos/130/179",
        "genres": ["Crime", "Drama", "Thriller"],
        "total_rating": 8.87,
        "user_rating": 7
    },
    {
        "_id": "tt7286456",
        "title": "Joker",
        "year": 2019,
        "poster": "https://picsum.photos/130/181",
        "genres": ["Crime", "Drama", "Thriller"],
        "total_rating": 8.87,
    },
]

export default function ResultsPage() {
    const [open, setOpen] = React.useState(false);

    return (
        <BasicPage>
            <MyCard>
                <br />
                <Grid justify="flex-start">
                    <Button variant="outlined" color="primary" align="right" onClick={() => setOpen(true)}>
                    Open simple dialog
                    </Button>
                </Grid>
                <Dialog selectedValue={0} open={open} onClose={() => setOpen(false)} >
                    <MyCard style={styles.cardRoot}>
                        <Typography variant="body1">Filters:</Typography>
                        <Filters />
                        <Button variant="outlined" color="primary" onClick={() => setOpen(false)}>
                            Close
                        </Button>
                    </MyCard>
                </Dialog>
                <h1>The best results are here for you:</h1>
                <List>
                    {queryOut.map((data, index) => (
                        <div key={index}>
                            <ListItem alignItems="flex-start">
                                <ListItemAvatar children={
                                    <img src={data.poster} style={styles.img} />
                                }>
                                </ListItemAvatar>
                                <ListItemText
                                    primary={
                                        <React.Fragment>
                                            <h4>{data.title} ({data.year})</h4>
                                            {data.genres.map((gen, index) => (
                                                <Chip key={index} size="small" label={gen} variant="outlined" color="primary" style={styles.genre} />
                                            ))}
                                        </React.Fragment>
                                    }
                                    primaryTypographyProps={{ variant: 'body2' }}
                                    secondary={
                                        <React.Fragment>
                                            <br />
                                            <Typography
                                                component="span"
                                                variant="body1"
                                                color="textPrimary"
                                            >
                                                Average rating {data.total_rating}/10
                                                </Typography>
                                            <br />
                                            <Rating name="avg-rating" value={data.total_rating / 2} max={5} precision={0.1} readOnly />
                                            <br />
                                            {
                                                !data.user_rating ? null :
                                                    <React.Fragment>
                                                        <Typography
                                                            component="span"
                                                            variant="body1"
                                                            color="textPrimary"
                                                        >
                                                            Your rating {data.user_rating}/10
                                                                                            </Typography>
                                                        <br />
                                                        <Rating name="user-rating" value={data.user_rating / 2} max={5} precision={0.5} />
                                                    </React.Fragment>
                                            }
                                        </React.Fragment>
                                    }
                                />
                            </ListItem>
                            <Divider component="li" />
                        </div >
                    ))}
                </List>
            </MyCard>
        </BasicPage>
    )
}