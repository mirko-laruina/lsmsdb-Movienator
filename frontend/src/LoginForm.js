import React from 'react'
import { Typography, TextField, Button } from '@material-ui/core'
import { Alert } from '@material-ui/lab'
import axios from 'axios'
import { baseUrl, errorHandler } from './utils.js'
import MyBackdrop from './MyBackdrop'

export default function LoginForm(props) {
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
                    localStorage.setItem('username', username)
                    props.setOpen(false)
                    window.location.reload()
                } else {
                    if(props.isRegistering){
                        setErrorMsg("Registration failed")
                    } else {
                        setErrorMsg("Login failed")
                    }
                }
                setLoading(false)
            }).catch((response) => errorHandler(response))
    }

    return (
        <React.Fragment>
            <Typography
                variant="h4"
                align="center">
                {props.isRegistering ? 'Registration' : 'Login'}
            </Typography>
            <br />
            <form onSubmit={(e) => {
                e.preventDefault()
                setErrorMsg("");
                setLoading(true);
                sendRequest();
            }}>
                {props.isRegistering &&
                    <TextField
                        variant="outlined"
                        label="Email"
                        value={email}
                        fullWidth
                        margin='normal'
                        onChange={(e) => setEmail(e.target.value)}
                    />
                }
                <TextField
                    variant="outlined"
                    label="Username"
                    fullWidth
                    value={username}
                    margin='normal'
                    onChange={(e) => setUsername(e.target.value)}
                />
                <TextField
                    variant="outlined"
                    label="Password"
                    value={password}
                    fullWidth
                    margin='normal'
                    type="password"
                    onChange={(e) => setPassword(e.target.value)}
                />
                {
                    errorMsg !== "" &&
                    <Alert severity="error">{errorMsg}</Alert>
                }
                <br />
                <br />
                <Button fullWidth
                    color="primary"
                    size="large"
                    variant="outlined"
                    type="submit"
                >
                    {props.isRegistering ? 'Register' : 'Login'}
                </Button>
                <Button fullWidth size="large" color="secondary" onClick={() => props.setOpen(false)}>
                    Cancel
                </Button>
            </form>
            <MyBackdrop
                open={loading}>
            </MyBackdrop>
        </React.Fragment>
    )
}