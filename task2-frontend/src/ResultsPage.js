import React, { Component } from 'react'

import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'
import {
    List, ListItem, ListItemIcon, ListItemAvatar,
    TextField, Divider, ListItemText, Typography, Chip, Dialog, Button, Slide, Grid
} from '@material-ui/core'
import Autocomplete from '@material-ui/lab/Autocomplete';

import ListIcon from '@material-ui/icons/List';
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
        padding: '1em 3em',
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

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="down" ref={ref} {...props} />;
});

const groups = ["Country", "Year", "Director", "Actor"];
const sorts = ["Count", "Rating", "Alphabetic"];
const sortOrders = ["Ascending", "Descending"];

export default function ResultsPage() {
    const [open, setOpen] = React.useState(false);

    return (
        <BasicPage>
            <MyCard style={styles.cardRoot}>
                <br />
                <Typography align="right">
                    <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => setOpen(true)}
                        startIcon={<ListIcon />}
                    >
                        Show filters
                            </Button>
                </Typography>
                <Dialog
                    TransitionComponent={Transition}
                    open={open}
                    PaperComponent={MyCard}
                    onClose={() => setOpen(false)}
                >

                    <Typography
                        variant="h3"
                        align="center">
                        Filters
                    </Typography>
                    <br />
                    <Filters />
                    <br />
                    <Button size="large" variant="outlined" color="primary" onClick={() => setOpen(false)}>
                        Apply
                    </Button>
                    <Button size="large" color="secondary" onClick={() => setOpen(false)}>
                        Cancel
                    </Button>
                </Dialog>
                <Typography variant="h4">The best results are here for you:</Typography>
                <Grid container spacing={2}>
                    <Grid item xs={3}>
                        <Autocomplete
                            id="group-by"
                            autoHighlight
                            size="small"
                            options={groups}
                            renderInput={params => (
                                <TextField {...params} label="Group by" margin="normal" variant="outlined" />
                            )}
                        />
                    </Grid>
                    <Grid item xs={3}>
                        <Autocomplete
                            id="sort-by"
                            autoHighlight
                            size="small"
                            options={sorts}
                            renderInput={params => (
                                <TextField {...params} label="Sort by" margin="normal" variant="outlined" />
                            )}
                        />
                    </Grid>
                    <Grid item xs={3}>
                        <Autocomplete
                            id="sort-order"
                            autoHighlight
                            size="small"
                            options={sortOrders}
                            defaultValue={sortOrders[0]}
                            renderInput={params => (
                                <TextField {...params} label="Sort order" margin="normal" variant="outlined" />
                            )}
                        />
                    </Grid>
                    <Grid item xs={3}>

                    </Grid>
                </Grid>
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