import { Route, Routes } from "react-router-dom";
// import './App.css'
import Home from "./pages/Home";
import UserSignIn from "./pages/UserSignIn";
import UserSignUp from './pages/UserSignUp';
import AddWitness from "./pages/AddWitness";
import AdminDashboard from "./pages/AdminDashboard"
import AdminCaseDetails from "./pages/AdminCaseDetails";


function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/signup" element={<UserSignUp />} />
        <Route path="/signin" element={<UserSignIn />} />
        <Route path="/add-witness" element={<AddWitness />} />
        <Route path="/admin/case/:id" element={<AdminCaseDetails />} />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
      </Routes>
    </>
  );
}

export default App;
