import { Route, Routes } from "react-router-dom";
// import './App.css'
import Home from "./pages/Home";
import UserSignIn from "./pages/UserSignIn";
import UserSignUp from './pages/UserSignUp';

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/signup" element={<UserSignUp />} />
        <Route path="/signin" element={<UserSignIn />} />
      </Routes>
    </>
  );
}

export default App;
