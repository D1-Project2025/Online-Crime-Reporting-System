import { Route, Routes } from "react-router-dom";
// import './App.css'
import Home from "./pages/Home";
import UserSignIn from "./pages/UserSignIn";
import UserSignUp from './pages/UserSignUp';
import AddWitness from "./pages/AddWitness";
import AdminDashboard from "./pages/AdminDashboard"
import AdminCaseDetails from "./pages/AdminCaseDetails";
import AuthorityDashboard from './pages/AuthorityDashboard';
import AuthorityCaseDetails from "./pages/AuthorityCaseDetails";
import AdminSignIn from "./pages/AdminSignIn";
import AuthoritySignIn from "./pages/AuthoritySignIn";
import FileFIR from "./pages/FileFIR";
import TrackStatus from "./pages/TrackStatus";



function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/signup" element={<UserSignUp />} />
        <Route path="/signin" element={<UserSignIn />} />
        <Route path="/file-fir" element={<FileFIR/>} />
        <Route path="/track-status" element={<TrackStatus/>} />
        <Route path="/add-witness" element={<AddWitness />} />
        <Route path="/admin/case/:id" element={<AdminCaseDetails />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/authority/dashboard" element={<AuthorityDashboard/>} />
        <Route path="/authority/case" element={<AuthorityCaseDetails/>} />
        <Route path="/admin/signin" element={<AdminSignIn />} />
        <Route path="/authority/signin" element={<AuthoritySignIn />} />
      </Routes>
    </>
  );
}

export default App;
