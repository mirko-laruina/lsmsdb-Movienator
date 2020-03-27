import React from 'react'
import {List, ListItem, ListItemAvatar, ListItemText, Typography, Divider, Chip} from '@material-ui/core'
import {Rating} from '@material-ui/lab'

import FilmListSkeleton from './FilmListSkeleton'

const styles = {
    img: {
        paddingRight: '1.5em',
        width: '140px',
        maxHeight: '250px',
    },
    genre: {
        marginRight: '0.5em',
        fontSize: '1em',
        padding: '0px',
    },
}
export default function FilmListDisplay(props) {

    return (
        <List>
            {props.loading ?
                [...Array(props.numFilm)].map((v, i) =>
                    <FilmListSkeleton key={i} />
                )
                :
                (props.array.length === 0
                    &&
                    <Typography variant="body1">No movies found</Typography>)
                ||
                props.array.map((data, index) => (
                    <div key={index}>
                        <ListItem alignItems="flex-start">
                            <ListItemAvatar children={
                                <img alt={data.title}
                                    src={data.poster ? data.poster : require('./blank_poster.png')}
                                    style={styles.img} />
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
                                            color="textPrimary">
                                            {data.description}
                                        </Typography>
                                        <br />
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
    )


}