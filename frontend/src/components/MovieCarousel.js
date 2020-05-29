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
    }
))


export default function MovieCarousel(props) {
    const classes = useStyles()

    return (
        (props.movies && props.movies.length != 0) ?
        <Tabs
            value={0}
            variant="standard"
            aria-label="Movie carousel"
            classes={{
                indicator: classes.tabsIndicator,
            }}>

            {
                props.movies.map((movie, i) => {
                    return <MyTab
                        key={i}
                        component={Link}
                        to={"/movie/" + movie.id}
                        label={movie.title}
                        icon={
                            <img
                                src={movie.poster ? movie.poster : require('../assets/blank_poster.png')}
                                style={{ width: '140px' }} />
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