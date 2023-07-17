import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import styled from '@emotion/styled';

import { Redirect } from 'central';

import Notes from './components/notes/Notes';

import { colors, login } from './constants';

const Root = styled.div`
  position: relative;
  display: flex;

  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;

  color: ${colors.black};
  font-family: 'Roboto', sans-serif;
  font-weight: 100;
`;

function App() {
  return (
    <Root>
      <Router>
        <Routes>
          <Route path="/" element={<Notes />} />
          <Route path="/login" element={<Redirect to={login} />} />
        </Routes>
      </Router>
    </Root>
  );
}

export default App;
