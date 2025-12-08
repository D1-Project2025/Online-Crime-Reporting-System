import { useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Input from '../components/Input';
import Button from '../components/Button';

export default function AdminSignIn() {
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate('/admin/dashboard');
  };

  return (
    <AuthLayout title="ADMIN SIGN IN">
      <form onSubmit={handleSubmit} className="space-y-6">
        <Input label="EMAIL" type="email" required />
        <Input label="PASSWORD" type="password" required />

        <Button type="submit" variant="navy" className="w-full">
          SIGN IN
        </Button>
      </form>
    </AuthLayout>
  );
}
