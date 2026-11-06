import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import StateMessage from './StateMessage';

export default function ProtectedRoute({ role, children }) {
  const { user, ready } = useAuth();
  const location = useLocation();

  if (!ready) {
    return <StateMessage loading title="Checking your session" />;
  }
  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }
  if (role && user.role !== role) {
    return (
      <StateMessage
        title="This page is for host accounts"
        body="Booking and wishlists use a driver account. Listing a space needs a separate host account."
        action={{ to: '/signup?role=HOST', label: 'Create a host account' }}
      />
    );
  }
  return children;
}
