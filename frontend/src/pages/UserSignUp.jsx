import { Link } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import Input from '../components/Input';
import Button from '../components/Button';

export default function UserSignUp() {
  return (
    <AuthLayout title="USER SIGN UP">
      <form className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Input label="FIRST NAME" required />
          <Input label="LAST NAME" required />
        </div>

        <Input label="PHONE NO" type="tel" required />
        <Input label="EMAIL" type="email" required />
        <Input label="PASSWORD" type="password" required />
        <Input label="ADDRESS" required />

        <Button type="submit" variant="primary" className="w-full">
          SIGN UP
        </Button>
      </form>

      <p className="text-center text-[#6D7A86] mt-6">
        Already have an account?{' '}
        <Link to="/signin" className="text-[#FF6A3D] font-semibold hover:text-[#ff5a2d] transition-colors">
          Sign In
        </Link>
      </p>
    </AuthLayout>
  );
}
