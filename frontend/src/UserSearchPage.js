import React, { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Typography, List, ListItem, ListItemAvatar, ListItemText, Avatar, Grid } from '@material-ui/core'
import PersonIcon from '@material-ui/icons/Person'
import RestrictedPage from './RestrictedPage'
import axios from 'axios'
import { baseUrl, errorHandler } from './utils'

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
        })
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
                    users.map((user, i) => {
                        return (
                            <Grid key={i} container>
                                <Link to={"/profile/" + user}>
                                    <Grid item xs={12}>
                                        <div>
                                            <List>
                                                <ListItem>
                                                    <ListItemAvatar>
                                                        <Avatar>
                                                            <PersonIcon />
                                                        </Avatar>
                                                    </ListItemAvatar>
                                                    <ListItemText
                                                        primary={user}
                                                    />
                                                </ListItem>
                                            </List>
                                        </div>
                                    </Grid>
                                </Link>
                            </Grid>
                        )
                    })
            }
        </RestrictedPage>
    )
}