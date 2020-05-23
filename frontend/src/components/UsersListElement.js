import React from 'react'
import { List, ListItem, ListItemAvatar, Avatar, ListItemText } from '@material-ui/core'
import PersonIcon from '@material-ui/icons/Person'

export default function UsersListElement(props) {
    return (
        <List>
            <ListItem>
                <ListItemAvatar>
                    <Avatar>
                        <PersonIcon />
                    </Avatar>
                </ListItemAvatar>
                <ListItemText
                    primary={props.user}
                />
            </ListItem>
        </List>
    )
}