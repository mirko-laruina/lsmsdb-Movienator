import React, { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Grid, Button, TextField } from '@material-ui/core'
import Alert from '@material-ui/lab/Alert'
import BasicPage from './BasicPage'
import { baseUrl, errorHandler, force_disconnect } from './utils'
import { Typography } from '@material-ui/core'
import MostLikedTable from './MostLikedTable'
import ProfilePageSkeleton from './ProfilePageSkeleton'
import axios from 'axios'

export default function ProfilePage(props) {
    const [infos, setInfos] = React.useState({})
    const [admin, setAdmin] = React.useState(false)
    const user = props.match.params.username
    const [loading, setLoading] = React.useState(true)
    const [newPassword, setNewPassword] = React.useState("")
    const [errorPw, setErrorPw] = React.useState(false)
    const [isUserSelf, setIsUserSelf] = React.useState(false)

    const changePassword = () => {
        axios.post(baseUrl + "/auth/password", null, {
            params: {
                sessionId: localStorage.getItem('sessionId'),
                password: newPassword
            }
        }).then((data) => {
            if (data.data.success) {
                window.location.reload()
            } else {
                setErrorPw(true)
            }
        }).catch((response) => errorHandler(response))
        setErrorPw(false)
    }

    useEffect(() => {
        setIsUserSelf(
            !props.match.params.username
            ||
            props.match.params.username === localStorage.getItem('username')
        )
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
        let url = baseUrl + "/user/" + localStorage.getItem('username')// + reqUser
        axios.get(url, {
            params: {
                sessionId: localStorage.getItem('sessionId')
            }
        }).then((data) => {
            if (data.data.success) {
                setInfos(data.data.response)
                setLoading(false)
            } else {
                alert(data.data.message)
                force_disconnect()
            }
        }).catch((response) => errorHandler(response))

        setLoading(true)
    }, [props.match.params.username])

    const banUser = () => {
        axios.post(baseUrl + "/user/" + user + "/ban", null, {
            params: {
                sessionId: localStorage.getItem('sessionId')
            }
        }).then((data) => {
            if (data.data.success) {
                props.history.push('/admin')
            } else {
                alert(data.data.message)
                force_disconnect()
            }
        }).catch((response) => errorHandler(response))
    }

    return (
        <BasicPage
            history={props.history}
        >
            {
                loading ?
                    <ProfilePageSkeleton showPw={isUserSelf} />
                    :
                    <React.Fragment>
                        <Grid container alignItems="center" spacing={1}>
                            <Grid item xs={isUserSelf ? 9 : 7}>
                                <Typography variant="h3" component="h1">
                                    Profile page
                        </Typography>
                            </Grid>
                            <Grid item xs={3}>
                                <Button fullWidth
                                    variant="outlined"
                                    size="large"
                                    color="primary"
                                    component={Link}
                                    to={"/social/" + (isUserSelf ? "" : user)}>
                                    Social profile
                                    </Button>
                            </Grid>
                            {!isUserSelf &&
                                <Grid item xs={2}>
                                    <Button fullWidth
                                        variant="outlined"
                                        size="large"
                                        color="primary"
                                        onClick={alert}>
                                        Follow
                                    </Button>
                                </Grid>
                            }
                        </Grid>

                        <br />
                        <Grid container spacing={1}>
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
                        {
                            (admin !== 'false'
                                ||
                                isUserSelf) &&

                            <Typography variant="h5" component='p'>
                                Email: {infos.email}
                            </Typography>
                        }
                        <br />
                        <Grid container spacing={1}>
                            <Grid item xs={9}>
                                <Typography variant="h4" component='h2'>
                                    {isUserSelf ? "The things you like" : "The things he/she likes"}
                                </Typography>
                            </Grid>
                            {
                             (isUserSelf || admin !== 'false') &&
                                <Grid item xs={3}>
                                    <Button fullWidth
                                        variant="outlined"
                                        size="large"
                                        color="primary"
                                        component={Link}
                                        to={
                                            admin === 'false' || !props.match.params.username
                                                ?
                                                "/history"
                                                :
                                                "/history/" + props.match.params.username
                                        }
                                    >
                                        Rating history
                                    </Button>
                                </Grid>
                            }
                        </Grid>
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
                        <br />
                        <br />
                        {
                            isUserSelf
                            &&
                            <React.Fragment>
                                <Typography variant="h4" component='h2'>
                                    Change password
                                </Typography>
                                {
                                    errorPw &&
                                    <Alert size="small" severity="error">
                                        Password change failed
                                    </Alert>
                                }
                                <TextField
                                    variant="outlined"
                                    label="New password"
                                    value={newPassword}
                                    type="password"
                                    margin="normal"
                                    onChange={(e) => setNewPassword(e.target.value)}
                                />
                                <br />
                                <Button
                                    variant="contained"
                                    size="large"
                                    color="primary"
                                    onClick={changePassword}
                                >
                                    Change password
                                </Button>
                            </React.Fragment>
                        }
                    </React.Fragment>
            }
        </BasicPage>
    )
}