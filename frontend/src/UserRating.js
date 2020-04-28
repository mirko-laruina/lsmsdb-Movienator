import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import { Rating } from '@material-ui/lab'
import FavoriteIcon from '@material-ui/icons/Favorite'
import { Typography, Link as MaterialLink } from '@material-ui/core'
import DeleteForeverIcon from '@material-ui/icons/DeleteForever';

import MyBackdrop from './MyBackdrop'

import axios from 'axios'
import { baseUrl, errorHandler, force_disconnect } from './utils'
export default function UserRating(props) {
    const [userRatedNow, setUserRatedNow] = React.useState(null)
    const [deletedNow, setDeletedNow] = React.useState(false)
    const [loading, setLoading] = React.useState(false)

    const ratingHandler = (movie, value) => {
        setLoading(true)
        let url = baseUrl + 'movie/' + movie + '/rating'
        axios.put(url, null, {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                rating: value
            }
        }).then((pkt) => {
            if (pkt.data.success) {
                setUserRatedNow(value)
                setDeletedNow(false)
                setLoading(false)
            } else {
                alert(pkt.data.message)
                force_disconnect()
            }
        }).catch((response) => errorHandler(response))
    }


    const deleteRating = (movie) => {
        setLoading(true)
        let url
        if (props.user) {
            url = baseUrl + 'user/' + props.user + '/rating/' + movie
        } else {
            url = baseUrl + 'movie/' + movie + '/rating'
        }
        let params = {}
        if (localStorage.getItem('username')) {
            params.sessionId = localStorage.getItem('sessionId')
        }
        axios.delete(url, { params: params }).then((data) => {
            setUserRatedNow(false)
            setDeletedNow(true)
            window.location.reload()
        }).catch((response) => errorHandler(response))
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
        <React.Fragment>
            <StyledRating
                name="user-rating"
                defaultValue={0}
                value={deletedNow ? 0 :
                    userRatedNow ? userRatedNow :
                        props.rating ? props.rating : 0}
                readOnly={props.readOnly}
                onChange={(e, value) => {
                    ratingHandler(props.movieId, value)
                }}
                precision={0.5}
                icon={<FavoriteIcon fontSize="inherit" />}
            />
            {
                props.showDelete &&
                !deletedNow &&
                (userRatedNow || props.rating) &&
                <MaterialLink href="#" onClick={() => deleteRating(props.movieId)}>
                    <Typography
                        variant="body2"
                        display="inline"
                    >
                        {' '}<DeleteForeverIcon style={{
                            padding: '0.1em',
                            fontSize: '1.5em'
                        }} />
                    </Typography>
                </MaterialLink>
            }
            <MyBackdrop
                open={loading}>
            </MyBackdrop>
        </React.Fragment>
    )
}