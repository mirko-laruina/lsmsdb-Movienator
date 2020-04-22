import React, { useEffect } from 'react'
import { Typography } from '@material-ui/core'
import BasicPage from './BasicPage'

export default function RestrictedPage(props) {
    const [authorized, setAuthorized] = React.useState(false)
    const authorization = props.customAuthorization

    useEffect(() => {
        if(authorization){
            setAuthorized(authorization())
            return
        }
        setAuthorized(localStorage.getItem('is_admin') === 'true')
    }, [authorization])

    return (
        <BasicPage history={props.history}>
            {
                !authorized ?
                    <React.Fragment>
                        <br />
                        <Typography
                            variant="h4"
                            component="h1"
                        >
                            Error: unauthorized
                        </Typography>
                        <br />
                        <Typography variant="body1" component="p">
                            You are not authorized to access this page
                        </Typography>
                    </React.Fragment>
                    :
                    props.children
            }
        </BasicPage>
    )
}