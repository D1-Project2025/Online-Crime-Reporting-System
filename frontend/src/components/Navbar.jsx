import { Link } from 'react-router-dom';
import { Shield } from 'lucide-react';

export default function Navbar({ links = [], showAuth = false }) {
  return (
    <nav className="bg-linear-to-r from-[#0A2A43] to-[#1a3a53] shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <Link to="/" className="flex items-center space-x-3 hover:opacity-80 transition-opacity">
            <div className="w-10 h-10 bg-linear-to-br from-[#FF6A3D] `to-[#ff8c66] rounded-full flex items-center justify-center">
              <Shield className="w-6 h-6 text-white" />
            </div>
            <span className="text-lg font-bold text-white">Crime Report System</span>
          </Link>

          <div className="flex items-center space-x-6">
            {links.map((link, index) => (
              <Link
                key={index}
                to={link.path}
                className={`text-sm font-medium transition-colors ${
                  link.active
                    ? 'text-[#FF6A3D]'
                    : 'text-gray-200 hover:text-[#FF6A3D]'
                }`}
              >
                {link.label}
              </Link>
            ))}

            {showAuth && (
              <div className="flex space-x-3">
                <Link
                  to="/signup"
                  className="px-4 py-2 text-white hover:bg-white/10 rounded-lg transition-colors"
                >
                  SIGN UP
                </Link>
                <Link
                  to="/signin"
                  className="px-4 py-2 bg-linear-to-r from-[#FF6A3D] to-[#ff8c66] text-white hover:from-[#ff5a2d] hover:to-[#ff7c56] rounded-lg transition-colors"
                >
                  SIGN IN
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
