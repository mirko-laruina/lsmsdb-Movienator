import React from 'react'
import { useEffect } from 'react'
import BasicPage from './BasicPage.js'
import MyCard from './MyCard.js'
import {
    List, ListItem, ListItemAvatar, Divider,
    ListItemText, Typography, Chip, Grid
} from '@material-ui/core'


import Rating from '@material-ui/lab/Rating';
import Pagination from '@material-ui/lab/Pagination';
import FilterDisplay from './FilterDisplay.js';
import Sorting from './Sorting.js';
import { baseUrl } from './utils.js'
import axios from 'axios';

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
    cardRoot: {
        padding: '1em 3em',
    }
}

export default function ResultsPage(props) {
    const [filters, setFilters] = React.useState({});
    const [sortOpt, setSortOpt] = React.useState({});
    const [movies, setMovies] = React.useState([]);

    const searchRequest = () => {
        console.log(filters)
        console.log(sortOpt)
        var sorting = {
            sortBy: sortOpt.sortBy === '0' ? 'date' :
                sortOpt.sortBy === '1' ? 'rating' : 'title',
            sortOrder: sortOpt.sortOrder === '0' ? -1 : 1,
        }
        console.log(sorting)
        axios.get(baseUrl + "movie/browse", {
            params: {
                ...filters,
                ...sorting
            }
        })
            .then(function (res) {
                if (res.data.success) {
                    setMovies(res.data.response)
                    console.log(movies)
                }
            })
    }

    useEffect(() => {
        searchRequest()
    }, [props.match.params.value, filters, sortOpt]);

    return (
        <BasicPage history={props.history}>
            <MyCard style={styles.cardRoot}>
                <FilterDisplay filters={filters} setFilters={setFilters} />
                <br />
                <Typography variant="h4">Best results for "{props.match.params.value}":</Typography>
                <Sorting noGroup sortOpt={sortOpt} handler={setSortOpt} />
                <List>
                    {movies.map((data, index) => (
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
                                            <h4>{data.title} ({data.runtime})</h4>
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
                <Grid container justify="center">
                <Pagination shape="rounded"
                showFirstButton
                 showLastButton
                  color="primary"
                   size="large"
                    count={10} />
                </Grid>
            </MyCard>
        </BasicPage>
    )
}