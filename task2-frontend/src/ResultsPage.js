import React from 'react'

import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'
import {
    List, ListItem, ListItemAvatar, Divider, Grid,
    ListItemText, Typography, Chip, Dialog, Button, Slide
} from '@material-ui/core'

import ListIcon from '@material-ui/icons/List';

import Rating from '@material-ui/lab/Rating';
import Filters from './Filters.js';
import Grouping from './Grouping.js';

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

export default function ResultsPage() {
    const [open, setOpen] = React.useState(false);
    const [filters, setFilters] = React.useState({});
    const [groupOpt, setGroupOpt] = React.useState({})

    return (
        <BasicPage>
            <MyCard style={styles.cardRoot}>
                <br />
                <Grid container>
                    <Grid item xs={9}>
                        {
                            Object.keys(filters).map((key, i) => {
                                var label = key.replace(/([A-Z])/g, ' $1').replace(/^./, function (str) { return str.toUpperCase(); })
                                return (
                                    <Chip
                                        variant="outlined"
                                        color="primary"
                                        style={{
                                            marginRight: '0.5em',
                                            marginTop: '0.5em',
                                        }}
                                        onDelete={() => {
                                            var newFilters = Object.assign({}, filters);
                                            delete newFilters[key];
                                            setFilters(newFilters)
                                        }
                                        }
                                        key={key}
                                        label={label + ": " + filters[key]}
                                    />
                                )
                            })
                        }
                    </Grid>
                    <Grid item xs={3}>
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
                            fullWidth={true}
                            maxWidth={'lg'}
                            onClose={() => setOpen(false)}
                        >

                            <Filters setOpen={setOpen} filters={filters} handler={setFilters} />
                        </Dialog>
                    </Grid>
                </Grid>
                <br />
                <Typography variant="h4">The best results are here for you:</Typography>
                <Grouping groupOpt={groupOpt} handler={setGroupOpt} />
                <List>
                    {queryOut.map((data, index) => (
                        <div key={index}>
                            <ListItem alignItems="flex-start">
                                <ListItemAvatar children={
                                    <img alt={data.title} src={data.poster} style={styles.img} />
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