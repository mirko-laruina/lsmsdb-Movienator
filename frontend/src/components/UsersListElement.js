import React from 'react'
import Gravatar from 'react-gravatar'
import { List, ListItem, ListItemAvatar, Avatar, ListItemText } from '@material-ui/core'

export default function UsersListElement(props) {
    return (
        <List>
            <ListItem>
                <ListItemAvatar>
                    <Avatar>
                        <Gravatar default="identicon" email={props.user} />
                    </Avatar>
                </ListItemAvatar>
                <ListItemText
                    primary={props.user}
                />
            </ListItem>
        </List>
    )
}