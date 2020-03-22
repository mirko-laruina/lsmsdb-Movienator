import React, { Component } from 'react'

import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'
import { List, ListItem, ListItemIcon, ListItemAvatar, Avatar, Divider, ListItemText, Typography } from '@material-ui/core'

const styles = {
    img: {
        paddingRight: '10px',
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
                <br />
                <MyCard>
                    The best results are here for you:
                    <List>
                        {this.queryOut.map((data, index) => (
                            <div key={index}>
                                <ListItem alignItems="flex-start">
                                    <ListItemAvatar children={
                                        <img src={data.poster} style={styles.img} />
                                    }>
                                    </ListItemAvatar>
                                    <ListItemText
                                        className={styles.itemText}
                                        primary={data.title}
                                        secondary={
                                            <React.Fragment>
                                                <Typography
                                                    component="span"
                                                    variant="body2"
                                                    color="textPrimary"
                                                >
                                                    Ali Connors  {data.year}
                                                </Typography>
                                                {" — I'll be in your neighborhood doing errands this…"}
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