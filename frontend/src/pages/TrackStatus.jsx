import { Link } from 'react-router-dom';
import { Check } from 'lucide-react';
import PageLayout from '../components/PageLayout';
import Card from '../components/Card';
import Button from '../components/Button';

export default function TrackStatus() {
  const navLinks = [
    { path: '/', label: 'HOME' },
    { path: '/file-fir', label: 'FILE FIR' },
    { path: '/missing-report', label: 'MISSING REPORT' },
    { path: '/track-status', label: 'TRACK STATUS', active: true }
  ];

  const stages = [
    { name: 'FIR Registered', active: true },
    { name: 'Authority Assigned', active: true },
    { name: 'Investigation Initiated', active: false },
    { name: 'Investigation Completed', active: false },
    { name: 'FIR Closed', active: false }
  ];

  return (
    <PageLayout navLinks={navLinks}>
      <div className="max-w-5xl mx-auto">
        <Card>
          <h1 className="text-3xl font-bold text-center text-[#0A2A43] mb-12">
            TRACK STATUS
          </h1>

          <div className="mb-12">
            <h2 className="text-2xl font-bold text-[#0A2A43] mb-8">Case Name</h2>

            <div className="relative">
              <div className="absolute top-8 left-0 right-0 h-2 bg-gray-200 rounded-full"></div>
              <div
                className="absolute top-8 left-0 h-2 bg-linear-to-r from-[#FF6A3D] to-[#ff8c66] rounded-full transition-all duration-500"
                style={{ width: '40%' }}
              ></div>

              <div className="relative flex justify-between items-start">
                {stages.map((stage, index) => (
                  <div key={index} className="flex flex-col items-center" style={{ width: '20%' }}>
                    <div
                      className={`w-16 h-16 rounded-full flex items-center justify-center transition-all duration-300 ${
                        stage.active
                          ? 'bg-linear-to-r from-[#FF6A3D] to-[#ff8c66] shadow-lg'
                          : 'bg-gray-300'
                      }`}
                    >
                      {stage.active && (
                        <Check className="w-8 h-8 text-white" />
                      )}
                    </div>
                    <p className={`text-sm font-semibold text-center mt-4 ${
                      stage.active ? 'text-[#0A2A43]' : 'text-[#6D7A86]'
                    }`}>
                      {stage.name}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="mt-16 text-center bg-linear-to-br from-gray-50 to-white rounded-xl p-8 border border-gray-100">
            <p className="text-[#6D7A86] mb-6 text-lg">
              Your case is currently being processed. You will be notified of any updates.
            </p>
            <Link to="/">
              <Button variant="navy">Back to Home</Button>
            </Link>
          </div>
        </Card>
      </div>
    </PageLayout>
  );
}