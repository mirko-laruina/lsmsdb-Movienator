import React, {useEffect} from 'react'
import {Grid, Chip, Typography, Button, Dialog, Slide} from '@material-ui/core'
import ListIcon from '@material-ui/icons/List';
import Filters from './Filters'
import MyCard from './MyCard'

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="down" ref={ref} {...props} />;
});

export default function ResultPage(props) {
    const [open, setOpen] = React.useState(false);
    var filters = props.filters
    var setFilters = props.setFilters

    const updateFilters = (filters) => {
        localStorage.setItem('filters', JSON.stringify(filters))
        setFilters(filters);
    }

    useEffect(()=>{
        window.onpopstate = (e) => {
            if(localStorage.getItem('filters'))
                setFilters(JSON.parse(localStorage.getItem('filters')))
        }
    }, [])

    return (
        <Grid container>
            <Grid item xs={9}>
                {
                    Object.keys(filters).map((key, i) => {
                        var label = key.replace(/([A-Z])/g, ' $1').replace(/^./, function (str) { return str.toUpperCase(); })
                        return (
                            <Chip
                                variant="outlined"
                                color="primary"
                                style={{
                                    marginRight: '0.5em',
                                    marginTop: '0.5em',
                                }}
                                onDelete={() => {
                                    var newFilters = Object.assign({}, filters);
                                    delete newFilters[key];
                                    setFilters(newFilters)
                                }
                                }
                                key={key}
                                label={label + ": " + filters[key]}
                            />
                        )
                    })
                }
            </Grid>
            <Grid item xs={3}>
                <Typography align="right">
                    <Button
                        variant="outlined"
                        color="primary"
                        onClick={() => setOpen(true)}
                        startIcon={<ListIcon />}
                    >
                        Show filters
                            </Button>
                </Typography>
                <Dialog
                    TransitionComponent={Transition}
                    open={open}
                    PaperComponent={MyCard}
                    fullWidth={true}
                    maxWidth={'lg'}
                    onClose={() => setOpen(false)}
                >

                    <Filters setOpen={setOpen} filters={filters} handler={updateFilters} />
                </Dialog>
            </Grid>
        </Grid>
    )
}