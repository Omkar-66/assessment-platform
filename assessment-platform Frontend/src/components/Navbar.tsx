import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LogOut, BookOpen } from 'lucide-react';

export function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isTeacher = user?.role === 'TEACHER';
  const initials = user?.username?.slice(0, 2).toUpperCase() ?? '??';

  return (
    <nav className="navbar">
      <NavLink to={isTeacher ? '/teacher' : '/student'} className="navbar-brand">
        <span className="navbar-logo">
          <BookOpen size={16} color="#fff" />
        </span>
        <span className="navbar-brand-text">AssessIQ</span>
      </NavLink>

      <div className="navbar-nav">
        {isTeacher ? (
          <>
            <NavLink to="/teacher" end className="navbar-link">
              Dashboard
            </NavLink>
            <NavLink to="/teacher/assessments" className="navbar-link">
              Assessments
            </NavLink>
          </>
        ) : (
          <>
            <NavLink to="/student" end className="navbar-link">
              Dashboard
            </NavLink>
            <NavLink to="/student/assessments" className="navbar-link">
              Browse
            </NavLink>
            <NavLink to="/student/history" className="navbar-link">
              History
            </NavLink>
          </>
        )}
      </div>

      <div className="navbar-user">
        <div className="user-avatar">{initials}</div>
        <span className="navbar-username">
          {user?.username}
        </span>
        <button className="btn btn-ghost btn-sm" onClick={handleLogout} title="Logout">
          <LogOut size={15} />
        </button>
      </div>
    </nav>
  );
}
