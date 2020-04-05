import React from 'react';
import { useEffect } from 'react';
import { FormGroup, Typography, TextField, Grid, Tabs, Tab, Button } from '@material-ui/core';
import Autocomplete from '@material-ui/lab/Autocomplete';

import Rating from '@material-ui/lab/Rating';
import StarBorderIcon from '@material-ui/icons/StarBorder';
import PeopleIcon from '@material-ui/icons/People';
import TheatersIcon from '@material-ui/icons/Theaters';
import { countryToFlag, countries } from './utils.js';

const genres = ["Action", "Adult", "Adventure",
    "Animation", "Biography", "Comedy",
    "Crime", "Documentary", "Drama",
    "Family", "Fantasy", "Film-Noir",
    "History", "Horror", "Music",
    "Musical", "Mystery", "News",
    "Reality-TV", "Romance", "Sci-Fi",
    "Sport", "Thriller", "War", "Western"];
const years = Array(new Date().getFullYear() - 1800).fill().map((_, i) => (i + 1801).toString()).reverse();

const defaultFilters = {
    minRating: 0,
    maxRating: 5,
    genre: "",
    fromYear: "",
    toYear: "",
    director: "",
    actor: "",
    country: "",
}

function setIfDef(handler, value) {
    if (typeof value !== 'undefined') {
        handler(value);
    }
}

export default function Filter(props) {
    const [minRating, setminRating] = React.useState(0);
    const [minTempRat, setMinTempRat] = React.useState(-1);
    const [maxRating, setmaxRating] = React.useState(5);
    const [maxTempRat, setMaxTempRat] = React.useState(-1);
    const [currTab, setCurrTab] = React.useState(0);
    const [genreValue, setGenre] = React.useState("");
    const [fromYearValue, setFromYear] = React.useState("");
    const [toYearValue, setToYear] = React.useState("");
    const [directorValue, setDirector] = React.useState("");
    const [actorValue, setActor] = React.useState("");
    const [countryValue, setCountry] = React.useState("");

    useEffect(() => {
        setIfDef(setGenre, props.filters.genre);
        setIfDef(setFromYear, props.filters.fromYear);
        setIfDef(setToYear, props.filters.toYear);
        setIfDef(setminRating, props.filters.minRating);
        setIfDef(setmaxRating, props.filters.maxRating);
        setIfDef(setDirector, props.filters.director);
        setIfDef(setActor, props.filters.actor);
        setIfDef(setCountry, props.filters.country);
    }, [props.filters, currTab])

    return (
        <div>
            <Typography
                variant="h3"
                align="center">
                Filter Movies
            </Typography>
            <br />
            <Tabs
                value={currTab}
                onChange={(evt, v) => setCurrTab(v)}
                variant="fullWidth"
                indicatorColor="primary"
                textColor="primary"
                aria-label="scrollable force tabs example"
            >
                <Tab label="By year and genre " icon={<TheatersIcon />} />
                <Tab label="By rating" icon={<StarBorderIcon />} />
                <Tab label="By people and country" icon={<PeopleIcon />} />
            </Tabs>
            <br />
            {currTab === 0 &&
                <FormGroup>
                    <Autocomplete
                        id="genre"
                        freeSolo
                        inputValue={genreValue}
                        onInputChange={(e, v) => setGenre(v)}
                        options={genres}
                        renderInput={params => (
                            <TextField {...params} label="Genre" margin="normal" variant="outlined" />
                        )}
                    />
                    <Grid container spacing={2}>
                        <Grid item xs={6}>
                            <Autocomplete
                                id="min-year"
                                autoHighlight
                                inputValue={fromYearValue}
                                onInputChange={(e, v) => setFromYear(v)}
                                options={years}
                                renderInput={params => (
                                    <TextField {...params} label="From year" margin="normal" variant="outlined" />
                                )}
                            />
                        </Grid>
                        <Grid item xs={6}>
                            <Autocomplete
                                id="max-year"
                                autoHighlight
                                inputValue={toYearValue}
                                onInputChange={(e, v) => setToYear(v)}
                                options={years}
                                renderInput={params => (
                                    <TextField {...params} label="To year" margin="normal" variant="outlined" />
                                )}
                            />
                        </Grid>
                    </Grid>
                </FormGroup>
            }

            {currTab === 1 &&
                <React.Fragment>
                    <Grid container>
                        <Grid item xs={6}>
                            <Typography variant="body1" align="center">
                                Minimum rating: {minTempRat >= 0 ? minTempRat : minRating}/5
                        </Typography>
                            <Typography align="center">
                                <Rating name="min-rating"
                                    value={minRating}
                                    onChange={(event, value) => setminRating(value)}
                                    onChangeActive={(event, value) => setMinTempRat(value)}
                                    max={5}
                                    precision={0.1}
                                    size="large"
                                />
                            </Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <Typography variant="body1" align="center">
                                Maximum rating: {maxTempRat >= 0 ? maxTempRat : maxRating}/5
                    </Typography>
                            <Typography align="center">
                                <Rating name="max-rating"
                                    value={maxRating}
                                    onChange={(event, value) => setmaxRating(value)}
                                    onChangeActive={(event, value) => setMaxTempRat(value)}
                                    max={5}
                                    precision={0.1}
                                    size="large"
                                />
                            </Typography>
                        </Grid>
                    </Grid>
                    <br />
                </React.Fragment>
            }

            {
                currTab === 2 &&
                <FormGroup>
                    <Autocomplete
                        id="director-filter"
                        freeSolo
                        inputValue={directorValue}
                        onInputChange={(e, v) => setDirector(v)}
                        options={[]}
                        renderInput={params => (
                            <TextField {...params} label="Director" margin="normal" variant="outlined" />
                        )}
                    />
                    <Autocomplete
                        id="actor-filter"
                        freeSolo
                        inputValue={actorValue}
                        onInputChange={(e, v) => setActor(v)}
                        options={[]}
                        renderInput={params => (
                            <TextField {...params} label="Actor" margin="normal" variant="outlined" />
                        )}
                    />
                    <Autocomplete
                        id="country-filter"
                        autoHighlight
                        inputValue={countryValue}
                        onInputChange={(e, v) => setCountry(v)}
                        options={countries}
                        getOptionLabel={option => option.label}
                        renderOption={option => (
                            <React.Fragment>
                                <span style={{ marginRight: '0.3em' }}>{countryToFlag(option.code)}</span>
                                {option.label} ({option.code})
                            </React.Fragment>
                        )}
                        renderInput={params => (
                            <TextField {...params} label="Country" margin="normal" variant="outlined" />
                        )}
                    />
                </FormGroup>
            }
            <br />
            <Button fullWidth size="large" variant="outlined" color="primary" onClick={() => {
                // Ugly: to be modified
                var newFilters = {}
                var supposedFilters = {
                    genre: genreValue,
                    fromYear: fromYearValue,
                    toYear: toYearValue,
                    minRating: minRating,
                    maxRating: maxRating,
                    director: directorValue,
                    actor: actorValue,
                    country: countryValue
                }
                Object.keys(supposedFilters).map((key, i) => {
                    if (supposedFilters[key] !== defaultFilters[key]) {
                        newFilters[key] = supposedFilters[key]
                    }
                    return null;
                })
                props.handler(newFilters)
                props.setOpen(false)
            }}>
                Apply
            </Button>
            <Button fullWidth size="large" color="secondary" onClick={() => props.setOpen(false)}>
                Cancel
            </Button>


        </div >
    );
}