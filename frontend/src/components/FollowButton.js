import React from 'react'
import { errorHandler, baseUrl, httpErrorhandler } from '../utils'
import { Button } from '@material-ui/core'

import axios from 'axios'

export default function FollowButton(props) {
    let {onClick, user, following, ...childProps} = props
    const followHandler = (user, toFollow = true) => {
        axios.post(baseUrl + "/user/" + user + (toFollow ? "/follow" : "/unfollow"), null, {
            params: {
                sessionId: localStorage.getItem('sessionId'),
            }
        }).then((data) => {
            if (data.data.success) {
                onClick()
            } else {
                alert("I couldn't " + (toFollow ? "follow" : "unfollow") + " the user!");
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    return (
        <Button
            onClick={() => followHandler(props.user, !props.following)}
            variant= {props.following ? "contained" : "outlined"}
            color="primary"
            {...childProps}
            >
            { props.following ? "Unfollow" : "Follow" }
        </Button>
    )
}