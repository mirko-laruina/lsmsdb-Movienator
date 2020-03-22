import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import { FormGroup, Typography, TextField, FormControlLabel } from '@material-ui/core';
import Rating from '@material-ui/lab/Rating';

const useStyles = makeStyles(theme => ({

}));

export default function SimpleSelect() {
    const classes = useStyles();
    const [minRat, setMinRat] = React.useState(0);
    const [minTempRat, setMinTempRat] = React.useState(0);
    const [maxRat, setMaxRat] = React.useState(0);
    const [maxTempRat, setMaxTempRat] = React.useState(0);

    return (
        <div>
            <FormGroup>
                <FormControl>
                    <Typography variant="body1" align="left">Minimum rating: {minTempRat >= 0 ? minTempRat : minRat}/10</Typography>
                    <Rating name="min-rating"
                        value={minRat / 2}
                        onChange={(event, value) => setMinRat(value * 2)}
                        onChangeActive={(event, value) => setMinTempRat(value * 2)}
                        max={5}
                        precision={0.05}
                    />
                </FormControl>
            </FormGroup>
            <FormGroup>
                <FormControl>

                    <Typography variant="body1" align="left">Maximum rating: {maxTempRat >= 0 ? maxTempRat : maxRat}/10</Typography>
                    <Rating name="max-rating"
                        value={maxRat / 2}
                        onChange={(event, value) => setMaxRat(value * 2)}
                        onChangeActive={(event, value) => setMaxTempRat(value * 2)}
                        max={5}
                        precision={0.05}
                    />
                    <br />
                </FormControl>
            </FormGroup>
            <FormGroup>
                <FormControlLabel
                    control={<TextField id="director" label="Director" variant="outlined" />}
                >
                </FormControlLabel>
                <FormControlLabel
                    control={<TextField id="actor" label="Actor" variant="outlined" />}
                >
                </FormControlLabel>
                <FormControlLabel
                    control={<TextField id="country" label="Country" variant="outlined" />}
                >
                </FormControlLabel>
            </FormGroup>


        </div>
    );
}
