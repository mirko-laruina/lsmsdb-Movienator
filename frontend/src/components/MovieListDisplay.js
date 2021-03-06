import React from 'react'
import { Link } from 'react-router-dom'
import { List, ListItem, ListItemAvatar, ListItemText, Typography, Divider, Chip } from '@material-ui/core'
import { Rating } from '@material-ui/lab'
import UserRating from './UserRating'

import FilmListSkeleton from '../skeletons/MovieListSkeleton'

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
export default function MovieListDisplay(props) {

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
                    <React.Fragment key={index}>
                        <ListItem component={Link} to={"/movie/" + data.id} alignItems="flex-start">
                            <ListItemAvatar children={
                                <img alt={data.title}
                                    src={data.poster ? data.poster : require('../assets/blank_poster.png')}
                                    style={styles.img} />
                            }>
                            </ListItemAvatar>
                            <ListItemText
                                primary={
                                    data.title &&
                                    <React.Fragment>
                                        <Typography variant="h4" component="h2">
                                            {data.title} {data.year ? "(" + data.year + ")" : ""}
                                        </Typography>
                                        {data.genres && data.genres.map((gen, index) => (
                                            <Chip key={index} size="small" label={gen} variant="outlined" color="primary" style={styles.genre} />
                                        ))}
                                        <br />
                                    </React.Fragment>
                                }
                                disableTypography
                                secondary={
                                    <React.Fragment>
                                        <br />
                                        {data.description &&
                                            <React.Fragment>
                                                <Typography
                                                    component="span"
                                                    variant="body1"
                                                    color="textPrimary">
                                                    {data.description}
                                                </Typography>
                                                <br />
                                                <br />
                                            </React.Fragment>
                                        }
                                        {data.totalRating &&
                                            <React.Fragment>
                                                <Typography
                                                    component="span"
                                                    variant="body1"
                                                    color="textPrimary"
                                                >
                                                    Average rating {Math.round(data.totalRating * 10) / 10}/5
                                            </Typography>
                                                <br />
                                                <Rating name="avg-rating" value={data.totalRating} max={5} precision={0.1} readOnly />
                                                <br />
                                            </React.Fragment>
                                        }
                                        {data.userRating &&
                                            <React.Fragment>
                                                <Typography
                                                    component="span"
                                                    variant="body1"
                                                    color="textPrimary"
                                                >
                                                    Your rating {data.userRating ? data.userRating : 0}/5
                                                </Typography>
                                                <br />
                                                <UserRating
                                                    movieId={data.id}
                                                    rating={data.userRating}
                                                    readOnly />
                                            </React.Fragment>
                                        }
                                    </React.Fragment>
                                }
                            />
                        </ListItem>
                        <Divider component="li" />
                    </React.Fragment >
                ))}
        </List>
    )


}