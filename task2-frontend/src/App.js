import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link
} from "react-router-dom";
import './App.css';
import HomePage from './HomePage.js'
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
  //NOTICE: root route has to be the last route, otherwise it will match all the paths
  return (
    <Router>
      <Switch>
        <Route path="/test">
          <p>test</p>
        </Route>
        <Route path="/">
          <HomePage />
        </Route>
      </Switch>
    </Router>
  );
}

export default App;
