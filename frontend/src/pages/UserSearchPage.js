import React, { useEffect } from 'react'
import { Typography } from '@material-ui/core'
import RestrictedPage from './RestrictedPage'
import axios from 'axios'
import UsersListDisplay from '../components/UsersListDisplay'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import FullWidthSearchBar from '../components/FullWidthSearchBar'
import { Skeleton } from '@material-ui/lab'

export default function UserSearchPage(props) {
    const [users, setUsers] = React.useState([])
    const [loading, setLoading] = React.useState(true)
    const [match, setMatch] = React.useState(props.match.params.query)

    useEffect(() => {
        if (props.match.params.query) {
            setLoading(true)
            axios.get(baseUrl + 'user/search', {
                params: {
                    sessionId: localStorage.getItem('sessionId'),
                    query: props.match.params.query
                }
            }).then((data) => {
                if (data.data.success) {
                    setUsers(data.data.response.list)
                    setLoading(false)
                    setMatch(props.match.params.query)
                } else {
                    errorHandler(data.data.code, data.data.message)
                }
            }).catch((error) => httpErrorhandler(error))
        } else {
            setLoading(false)
        }
    }, [props.match.params.query])

    const search = (query) => {
        props.history.push('/search/' + query)
    }

    return (
        <RestrictedPage history={props.history}>
            <Typography
                variant="h4"
                component="h1"
            >
                Search a user
            </Typography>
            <br />
            <FullWidthSearchBar onSubmit={search} />
            {
                match &&
                <>
                    <br />
                    <Typography
                        variant="h4"
                        component="h1"
                    >
                        Users matching "{match}"
                </Typography>
                </>
            }
            <br />
            {
                loading ?
                    <Skeleton />
                    :
                    users.length === 0 && !loading && match ?
                        <Typography variant="body1" component="p">
                            No user found
                        </Typography>
                        :
                        <UsersListDisplay users={users} />
            }
        </RestrictedPage>
    )
}