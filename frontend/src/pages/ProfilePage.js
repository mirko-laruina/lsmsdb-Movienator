import React, { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Grid, Button, TextField } from '@material-ui/core'
import Alert from '@material-ui/lab/Alert'
import BasicPage from './BasicPage'
import { baseUrl, errorHandler, httpErrorhandler } from '../utils'
import { Typography } from '@material-ui/core'
import MostLikedTable from '../components/MostLikedTable'
import ProfilePageSkeleton from '../skeletons/ProfilePageSkeleton'
import axios from 'axios'
import FollowButton from '../components/FollowButton'

export default function ProfilePage(props) {
    const [infos, setInfos] = React.useState({})
    const [admin, setAdmin] = React.useState(false)
    const user = props.match.params.username ? props.match.params.username : localStorage.getItem('username')
    const [loading, setLoading] = React.useState(true)
    const [newPassword, setNewPassword] = React.useState("")
    const [errorPw, setErrorPw] = React.useState(false)
    const [isTargetUser, setIsTargetUser] = React.useState(false)

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
        }).catch((error) => httpErrorhandler(error))
        setErrorPw(false)
    }

    const getProfile = () => {
        setLoading(true)
        let url = baseUrl + "/user/" + user
        axios.get(url, {
            params: {
                sessionId: localStorage.getItem('sessionId')
            }
        }).then((data) => {
            if (data.data.success) {
                setInfos(data.data.response)
                setLoading(false)
            } else {
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    useEffect(() => {
        setIsTargetUser(
            !props.match.params.username
            ||
            props.match.params.username === localStorage.getItem('username')
        )
        setAdmin(localStorage.getItem('is_admin') === 'true')

        getProfile()
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
                errorHandler(data.data.code, data.data.message)
            }
        }).catch((error) => httpErrorhandler(error))
    }

    return (
        <BasicPage
            history={props.history}
        >
            {
                loading ?
                    <ProfilePageSkeleton showPw={isTargetUser} />
                    :
                    <React.Fragment>
                        <Grid container alignItems="center" spacing={1}>
                            <Grid item xs={isTargetUser ? 9 : 7}>
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
                                    to={"/social/" + (isTargetUser ? "" : user)}>
                                    Social profile
                                    </Button>
                            </Grid>
                            {!isTargetUser &&
                                <Grid item xs={2}>
                                    <FollowButton
                                        fullWidth
                                        size="large"
                                        user={infos.username}
                                        onClick={() => { getProfile() }}
                                        following={infos.following}
                                    />
                                </Grid>
                            }
                        </Grid>
                        <br />
                        {
                            !isTargetUser && infos.following &&
                            <>
                                <Typography variant="h5" component="h2">
                                    {"You are following " + user}
                                </Typography>
                            </>
                        }
                        {
                            !isTargetUser && infos.follower &&
                            <>
                                <Typography variant="h5" component="h2">
                                    {user + " is following you."}
                                </Typography>
                            </>
                        }
                        <br />
                        <Grid container spacing={1}>
                            <Grid item xs={9}>
                                <Typography variant="h4" component='h2'>
                                    Account details
                                        </Typography>
                            </Grid>
                            <Grid item xs={3}>
                                {
                                    admin &&
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
                            (admin
                                ||
                                isTargetUser) &&

                            <Typography variant="h5" component='p'>
                                Email: {infos.email}
                            </Typography>
                        }
                        <br />
                        <Grid container spacing={1}>
                            <Grid item xs={9}>
                                <Typography variant="h4" component='h2'>
                                    {isTargetUser ? "The things you like" : ("The things " + user + " likes")}
                                </Typography>
                            </Grid>
                            {
                                <Grid item xs={3}>
                                    <Button fullWidth
                                        variant="outlined"
                                        size="large"
                                        color="primary"
                                        component={Link}
                                        to={
                                            isTargetUser
                                                ?
                                                "/history"
                                                :
                                                "/history/" + user
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
                            isTargetUser
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