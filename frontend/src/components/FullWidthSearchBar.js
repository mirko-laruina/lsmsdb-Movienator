import React from 'react'
import { FormControl, OutlinedInput, InputAdornment, IconButton } from '@material-ui/core'
import SearchIcon from '@material-ui/icons/Search'

export default function FullWidthSearchBar(props) {
    const [searchValue, setSearchValue] = React.useState("")

    return (
        <form onSubmit={(e) => {
            e.preventDefault()
            props.onSubmit(searchValue)
        }}>
            <FormControl fullWidth variant="outlined">
                <OutlinedInput
                    id="search-user"
                    placeholder="Search a user"
                    value={searchValue}
                    onChange={(e) => setSearchValue(e.target.value)}
                    endAdornment={
                        <InputAdornment position="end">
                            <IconButton
                                type="submit"
                                aria-label="search"
                            >
                                <SearchIcon />
                            </IconButton>
                        </InputAdornment>
                    }
                    inputProps={{
                        'aria-label': 'weight',
                    }}
                    labelWidth={0}
                />
            </FormControl>
        </form>
    )
}