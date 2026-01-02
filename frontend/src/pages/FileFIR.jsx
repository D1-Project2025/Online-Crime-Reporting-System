import { useNavigate } from 'react-router-dom';
import { Upload } from 'lucide-react';
import PageLayout from '../components/PageLayout';
import Card from '../components/Card';
import Input from '../components/Input';
import Textarea from '../components/Textarea';
import Button from '../components/Button';

export default function FileFIR() {
  const navigate = useNavigate();

  const navLinks = [
    { path: '/', label: 'HOME' },
    { path: '/file-fir', label: 'FILE FIR', active: true },
    { path: '/missing-report', label: 'MISSING REPORT' },
    { path: '/track-status', label: 'TRACK STATUS' }
  ]; 

  const handleSubmit = (e) => {
    e.preventDefault();
    navigate('/add-witness');
  };

  return (
    <PageLayout navLinks={navLinks}>
      <div className="max-w-4xl mx-auto">
        <Card>
          <h1 className="text-3xl font-bold text-center text-[#0A2A43] mb-8">
            FILE FIRST INFORMATION REPORT
          </h1>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid md:grid-cols-2 gap-6">
              <Input label="REPORT DATE" placeholder="DD-MM-YYYY" required />
              <Input label="REPORT TIME" placeholder="HH-MM-SS" required />
            </div>

            <div className="grid md:grid-cols-2 gap-6">
              <Input label="INCIDENT DATE" placeholder="DD-MM-YYYY" required />
              <Input label="INCIDENT TIME" placeholder="HH-MM-SS" required />
            </div>

            <Input label="INCIDENT LOCATION" required />
            <Textarea label="INCIDENT DETAILS" rows="6" required />
            <Textarea label="OTHER DETAILS" rows="6" />

            <div>
              <label className="block text-sm font-medium text-[#0A2A43] mb-2">
                MEDIA UPLOAD
              </label>
              <div className="border-2 border-dashed border-[#6D7A86] rounded-lg p-12 text-center hover:border-[#FF6A3D] transition-colors cursor-pointer bg-linear-to-br from-gray-50 to-white">
                <Upload className="w-12 h-12 text-[#6D7A86] mx-auto mb-4" />
                <p className="text-[#6D7A86] font-medium">DRAG AND DROP HERE</p>
                <p className="text-sm text-[#6D7A86] mt-2">or click to browse</p>
              </div>
            </div>

            <Button type="submit" variant="primary" className="w-full">
              SUBMIT FIR REPORT
            </Button>
          </form>
        </Card>
      </div>
    </PageLayout>
  );
}