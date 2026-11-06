import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Explore from './pages/Explore';
import ParkingDetails from './pages/ParkingDetails';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Bookings from './pages/Bookings';
import Wishlist from './pages/Wishlist';
import Profile from './pages/Profile';
import HostDashboard from './pages/HostDashboard';
import ListSpace from './pages/ListSpace';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<Layout />}>
              <Route path="/" element={<Home />} />
              <Route path="/explore" element={<Explore />} />
              <Route path="/spaces/:id" element={<ParkingDetails />} />
              <Route path="/login" element={<Login />} />
              <Route path="/signup" element={<Signup />} />
              <Route path="/bookings" element={<ProtectedRoute role="DRIVER"><Bookings /></ProtectedRoute>} />
              <Route path="/wishlist" element={<ProtectedRoute><Wishlist /></ProtectedRoute>} />
              <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
              <Route path="/host" element={<ProtectedRoute role="HOST"><HostDashboard /></ProtectedRoute>} />
              <Route path="/host/list" element={<ProtectedRoute role="HOST"><ListSpace /></ProtectedRoute>} />
              <Route path="/host/spaces/:id/edit" element={<ProtectedRoute role="HOST"><ListSpace /></ProtectedRoute>} />
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  );
}
