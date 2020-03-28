import React, { useEffect } from 'react'
import { makeStyles } from '@material-ui/core/styles';
import { Grid, Button } from '@material-ui/core'
import BasicPage from './BasicPage'
import { baseUrl } from './utils'
import { Typography } from '@material-ui/core'
import MostLikedTable from './MostLikedTable'
import axios from 'axios'

export default function ProfilePage(props) {
    const [infos, setInfos] = React.useState({})
    const [authorized, setAuthorized] = React.useState(false)
    const [admin, setAdmin] = React.useState(false)
    const [user, setUser] = React.useState(props.match.params.username)

    useEffect(() => {
        let reqUser = localStorage.getItem('username')
        if (!reqUser) {
            //not logged in
            return;
        }
        let urlUser = props.match.params.username
        setAdmin(localStorage.getItem('is_admin'))
        // Note: setAdmin is async, so in the if
        // we can't check the admin variable directly
        if (urlUser
            && reqUser !== urlUser
            && !localStorage.getItem('is_admin')) {
            //logged in but requested another user profile page
            return
        }

        if (urlUser) {
            // Admin requesting another user profile page 
            reqUser = urlUser
        }
        setAuthorized(true)
        let url = baseUrl + "/user/" + reqUser
        axios.get(url, {
            params: {
                sessionId: localStorage.getItem('sessionId')
            }
        }).then((data) => {
            if (data.data.success) {
                console.log(data.data.response)
                setInfos(data.data.response)
            }
        })
    }, [props.match.params.username])

    const banUser = () => {
        axios.post(baseUrl+"/user/"+user+"/ban", null, {
            params: {
                sessionId: localStorage.getItem('sessionId')
            }
        }).then((data) => {
            if(data.data.success){
                props.history.push('/admin')
            }
        })
    }

    return (
        <BasicPage history={props.history}>
            <Typography variant="h3" component="h1">
                Profile page
                </Typography>
            <br />
            {
                !authorized ?
                    <Typography variant="body1" component='p'>
                        You are not authorized to access this page
                        </Typography>
                    :
                    <React.Fragment>
                        <Grid container>
                            <Grid item xs={9}>
                                <Typography variant="h4" component='h2'>
                                    Account details
                                </Typography>
                            </Grid>
                            <Grid item xs={3}>
                                {
                                    admin !== 'false' &&
                                    <Button fullWidth
                                        variant="outlined"
                                        size="large"
                                        color="primary"
                                        onClick={banUser}>
                                        Ban user
                                    </Button>
                                }
                            </Grid>
                        </Grid>
                        <br />
                        <Typography variant="h5" component='p'>
                            Username: {infos.username}
                        </Typography>
                        <Typography variant="h5" component='p'>
                            Email: {infos.email}
                        </Typography>
                        <br />
                        <Typography variant="h4" component='h2'>
                            The things you like
                            </Typography>
                        <br />
                        <Typography variant="h5" component='h3'>
                            Actors
                            </Typography>
                        <br />
                        <MostLikedTable
                            subject='Actor'
                            data={infos.favouriteActors}
                        />
                        <br />
                        <Typography variant="h5" component='h3'>
                            Directors
                            </Typography>
                        <br />
                        <MostLikedTable
                            subject='Director'
                            data={infos.favouriteDirectors}
                        />
                        <br />
                        <Typography variant="h5" component='h3'>
                            Genres
                            </Typography>
                        <br />
                        <MostLikedTable
                            subject='Genre'
                            data={infos.favouriteGenres}
                        />
                    </React.Fragment>
            }
        </BasicPage>
    )
}