import React from 'react'
import { Typography, TextField, Button } from '@material-ui/core'

export default function LoginForm(props) {
    const [username, setUsername] = React.useState("");
    const [password, setPassword] = React.useState("");

    return (
        <React.Fragment>
            <Typography
                variant="h4"
                align="center">
                    {props.isRegistering ? 'Registration' : 'Login'}
            </Typography>
            <br />
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
            <Button fullWidth color="primary" size="large" variant="outlined">
                {props.isRegistering ? 'Register' : 'Login'}
            </Button>
            <Button fullWidth size="large" color="secondary">
                Cancel
            </Button>     
        </React.Fragment>
    )
}