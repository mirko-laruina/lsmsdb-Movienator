import React from 'react';
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Link
} from "react-router-dom";
import './App.css';


function App() {
  //NOTICE: root route has to be the last route, otherwise it will match all the paths
  return (
    <Router>
      <Switch>
        <Route path="/test">
          <p>test</p>
        </Route>
        <Route path="/">
          <p>Home</p>
          <Link to="/test">Test</Link>
        </Route>
      </Switch>
    </Router>
  );
}

export default App;
