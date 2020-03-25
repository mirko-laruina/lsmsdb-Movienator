import React from 'react'
import { Typography, TextField, Button, Backdrop, CircularProgress } from '@material-ui/core'
import { Alert } from '@material-ui/lab'
import axios from 'axios'
import { baseUrl } from './utils.js'
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
    backdrop: {
        zIndex: theme.zIndex.drawer + 1,
    },
}));

export default function LoginForm(props) {
    const classes = useStyles();
    const [username, setUsername] = React.useState("");
    const [password, setPassword] = React.useState("");
    const [email, setEmail] = React.useState("");
    const [errorMsg, setErrorMsg] = React.useState("");
    const [loading, setLoading] = React.useState(false);

    const sendRequest = () => {
        var requestUrl = baseUrl
        var postParams = {
            username: username,
            password: password
        }
        if (props.isRegistering) {
            requestUrl += "auth/register"
            postParams.email = email
        } else {
            requestUrl += "auth/login"
        }

        axios.post(requestUrl, null, {
            params: postParams
        })
            .then(function (pkt) {
                console.log(pkt.data)
                if (pkt.data.success) {
                    localStorage.setItem('sessionId', pkt.data.response.sessionId)
                    localStorage.setItem('is_admin', pkt.data.response.is_admin)
                    props.setOpen(false)
                } else {
                    setErrorMsg("Failed login")
                }
                setLoading(false)
            })
    }

    return (
        <React.Fragment>
            <Typography
                variant="h4"
                align="center">
                {props.isRegistering ? 'Registration' : 'Login'}
            </Typography>
            <br />
            {props.isRegistering &&
                <TextField
                    variant="outlined"
                    label="Email"
                    value={email}
                    margin='normal'
                    onChange={(e) => setEmail(e.target.value)}
                />
            }
            <TextField
                variant="outlined"
                label="Username"
                value={username}
                margin='normal'
                onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
                variant="outlined"
                label="Password"
                value={password}
                margin='normal'
                type="password"
                onChange={(e) => setPassword(e.target.value)}
            />
            {
                errorMsg !== "" &&
                <Alert severity="error">{errorMsg}</Alert>
            }
            <br />
            <Button fullWidth
                color="primary"
                size="large"
                variant="outlined"
                onClick={() => {
                    setErrorMsg("");
                    setLoading(true);
                    sendRequest();
                }}
            >
                {props.isRegistering ? 'Register' : 'Login'}
            </Button>
            <Button fullWidth size="large" color="secondary" onClick={() => props.setOpen(false)}>
                Cancel
            </Button>
            <Backdrop
                className={classes.backdrop}
                open={loading}>
                <CircularProgress color="inherit" />
            </Backdrop>
        </React.Fragment>
    )
}