import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Rating } from '@material-ui/lab'
import FavoriteIcon from '@material-ui/icons/Favorite'
import axios from 'axios'
import { baseUrl } from './utils'
export default function UserRating(props) {
    const [userRatedNow, setUserRatedNow] = React.useState(null)

    const ratingHandler = (movie, value) => {
        let url = baseUrl + 'movie/' + movie + '/rating'
        axios.put(url, null, {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                rating: value
            }
        }).then((pkt) => {
            if (pkt.data.success) {
                setUserRatedNow(value)
            }
        })
    }

    const StyledRating = withStyles({
        iconFilled: {
            color: '#ff6d75',
        },
        iconHover: {
            color: '#ff3d47',
        },
    })(Rating);

    return (
        <StyledRating
            name="user-rating"
            defaultValue={0}
            value={userRatedNow ? userRatedNow :
                props.rating ? props.rating : 0}
            readOnly={props.readOnly}
            onChange={(e, value) => {
                ratingHandler(props.movieId, value)
            }}
            precision={0.5}
            icon={<FavoriteIcon fontSize="inherit" />}
        />
    )
}