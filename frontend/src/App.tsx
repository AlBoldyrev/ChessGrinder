import React from 'react';
import './App.css';
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {BrowserRouter, HashRouter, Route, Routes} from "react-router-dom";
import MainPage from "pages/MainPage"
import TournamentPage from "pages/TournamentPage"
import Header from "components/Header";
import AdminPage from "pages/AdminPage";
import LoginPage from "pages/LoginPage";
import UserProfilePage from "./pages/UserProfilePage";

const queryClient = new QueryClient()

let ApplicationRouter = 1 > 0 /*TODO*/ ? HashRouter : BrowserRouter

function App() {
  return (
      <div className='App'>
        <QueryClientProvider client={queryClient}>
          <ApplicationRouter>
            <React.StrictMode>
              <Header/>
              <Routes>
                <Route path="/" element={<MainPage/>}/>
                <Route path="/tournament/:id" element={<TournamentPage/>}/>
                <Route path="/tournament/:id/round/:roundId" element={<TournamentPage/>}/>
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/admin" element={<AdminPage/>}/>
                <Route path="/user" element={<UserProfilePage/>}/>
                <Route path="/user/:username" element={<UserProfilePage/>}/>
              </Routes>
            </React.StrictMode>
          </ApplicationRouter>
        </QueryClientProvider>
      </div>
  );
}

export default App;