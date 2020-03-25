import React from 'react'
import { Typography, TextField, Button } from '@material-ui/core'
import axios from 'axios'
import { baseUrl } from './utils.js'

export default function LoginForm(props) {
    const [username, setUsername] = React.useState("");
    const [password, setPassword] = React.useState("");
    const [email, setEmail] = React.useState("");

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
            .then(function (response) {
                console.log(response.data);
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
                onChange={(e) => setPassword(e.target.value)}
            />
            <br />
            <Button fullWidth
                color="primary"
                size="large"
                variant="outlined"
                onClick={() => {
                    props.setOpen(false);
                    sendRequest();
                }}
            >
                {props.isRegistering ? 'Register' : 'Login'}
            </Button>
            <Button fullWidth size="large" color="secondary" onClick={() => props.setOpen(false)}>
                Cancel
            </Button>
        </React.Fragment>
    )
}