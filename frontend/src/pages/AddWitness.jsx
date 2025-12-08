import { useNavigate } from 'react-router-dom';
import PageLayout from '../components/PageLayout';
import Card from '../components/Card';
import Input from '../components/Input';
import Button from '../components/Button';

export default function AddWitness() {
  const navigate = useNavigate();

  const navLinks = [
    { path: '/', label: 'HOME' },
    { path: '/file-fir', label: 'FILE FIR' },
    { path: '/missing-report', label: 'MISSING REPORT' },
    { path: '/track-status', label: 'TRACK STATUS' }
  ];

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate('/track-status');
  };

  return (
    <PageLayout navLinks={navLinks}>
      <div className="max-w-3xl mx-auto">
        <Card>
          <h1 className="text-3xl font-bold text-center text-[#0A2A43] mb-8">
            ADD WITNESS
          </h1>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid md:grid-cols-2 gap-6">
              <Input label="FIRST NAME" required />
              <Input label="LAST NAME" required />
            </div>

            <Input label="PHONE NO" type="tel" required />
            <Input label="EMAIL" type="email" required />

            <Button type="submit" variant="primary" className="w-full mt-8">
              SUBMIT WITNESS
            </Button>
          </form>
        </Card>
      </div>
    </PageLayout>
  );
}
