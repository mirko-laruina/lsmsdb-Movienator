import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import './App.css';
import HomePage from './HomePage.js'
import 'bootstrap/dist/css/bootstrap.min.css';
import { CssBaseline, ThemeProvider } from '@material-ui/core';
import { fade, makeStyles, createMuiTheme } from '@material-ui/core/styles';

const theme = createMuiTheme({
  palette: {
    background: {
      default: "#717078"
    },
    primary: {
      main: "#8e1d34",
      contrastText: '#fff',
    },
    info: {
      main: "#8e1d34",
    },
  },
  typography: {
    fontFamily: [
      'Ubuntu',
      'Roboto',
      'Arial'
    ].join(','),
  }
});

function App() {
  //NOTICE: root route has to be the last route, otherwise it will match all the paths
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Switch>
          <Route path="/test">
            <p>test</p>
          </Route>
          <Route path="/results">
            <p>test</p>
          </Route>
          <Route path="/">
            <HomePage />
          </Route>
        </Switch>
      </Router>
    </ThemeProvider>
  );
}

export default App;
