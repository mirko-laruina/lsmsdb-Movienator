import React, { Component } from 'react'

import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'
import {
    List, ListItem, ListItemIcon, ListItemAvatar,
    Avatar, Divider, ListItemText, Typography, Chip
} from '@material-ui/core'

import Rating from '@material-ui/lab/Rating';

const styles = {
    img: {
        paddingRight: '1.5em',
    },
    genre: {
        marginRight: '0.5em',
        fontSize: '1em',
        padding: '0px',
    }
}

class ResultsPage extends Component {
    constructor() {
        super();
        this.queryOut = [
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
    }

    render() {
        return (
            <BasicPage>
                <MyCard>
                    Filters:
                </MyCard>
                <MyCard>
                    <br />
                    <h1>The best results are here for you:</h1>
                    <List>
                        {this.queryOut.map((data, index) => (
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
                                                            <Rating name="user-rating" value={data.user_rating / 2} max={5} precision={0.5}/>
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
}

export default ResultsPage;