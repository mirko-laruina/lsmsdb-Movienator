import React from 'react'
import { Link } from 'react-router-dom'
import { Tab, Tabs, Typography } from '@material-ui/core'
import { withStyles, makeStyles } from '@material-ui/core/styles';

const MyTab = withStyles({
    textColorInherit: {
        opacity: '1',
    },
})(Tab)

const useStyles = makeStyles(theme => (
    {
        tabsIndicator: {
            visibility: 'hidden',
        },
        tabRoot: {
            width: '25%',
        }
    }
))


export default function MovieCarousel(props) {
    const classes = useStyles()

    return (
        (props.movies && props.movies.length !== 0) ?
        <Tabs
            value={0}
            variant="scrollable"
            aria-label="Movie carousel"
            classes={{
                indicator: classes.tabsIndicator,
            }}>

            {
                props.movies.map((movie, i) => {
                    return <MyTab
                        key={i}
                        classes={{
                            root: classes.tabRoot
                        }}
                        component={Link}
                        to={"/movie/" + movie.id}
                        label={movie.title}
                        icon={
                            <img
                                src={(movie.poster && movie.poster !== "null") ? movie.poster : require('../assets/blank_poster.png')}
                                alt={movie.title + " poster"}
                                style={{ width: '95%' }} />
                        }
                    />
                })
            }
        </Tabs>
        :
        <Typography variant="body1" component="p">
            No suggestions for you. Try rating some movies first!
        </Typography>
    )
}