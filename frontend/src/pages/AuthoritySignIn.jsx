import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Input from '../components/Input';
import Button from '../components/Button';

export default function AuthoritySignIn() {
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate('/authority/dashboard');
  };

  return (
    <AuthLayout title="AUTHORITY SIGN IN">
      <form onSubmit={handleSubmit} className="space-y-6">
        <Input label="EMAIL" type="email" required />
        <Input label="PASSWORD" type="password" required />

        <Button type="submit" variant="navy" className="w-full">
          SIGN IN
        </Button>
      </form>

      <p className="text-center text-[#6D7A86] mt-6">
        Don't have an account?{' '}
        <Link to="/authority/signup" className="text-[#FF6A3D] font-semibold hover:text-[#ff5a2d] transition-colors">
          Sign Up
        </Link>
      </p>
    </AuthLayout>
  );
}
