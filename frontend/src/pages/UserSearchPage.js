import React, { useEffect } from 'react'
import { Typography, Grid } from '@material-ui/core'
import RestrictedPage from './RestrictedPage'
import axios from 'axios'
import UsersListDisplay from '../components/UsersListDisplay'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'

export default function UserSearchPage(props) {
    const [users, setUsers] = React.useState([])
    const [loading, setLoading] = React.useState(true)

    useEffect(() => {
        axios.get(baseUrl + 'user/search', {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                query: props.match.params.query
            }
        }).then((data) => {
            if (data.data.success) {
                setUsers(data.data.response)
                setLoading(false)
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }, [props.match.params.query])

    return (
        <RestrictedPage history={props.history}>
            <Typography
                variant="h4"
                component="h1"
            >
                Users matching "{props.match.params.query}"
            </Typography>
            <br />
            {
                users.length === 0 && !loading ?
                    <Typography variant="body1" component="p">
                        No user found
                        </Typography>
                    :
                    <UsersListDisplay users={users} />
            }
        </RestrictedPage>
    )
}