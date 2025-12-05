import { useParams } from 'react-router-dom';
import PageLayout from '../components/PageLayout';
import Card from '../components/Card';
import Button from '../components/Button';

export default function AdminCaseDetails() {
  const { id } = useParams();

  const navLinks = [
    { path: '/admin/dashboard', label: 'HOME' },
    { path: '/admin/missing-reports', label: 'MISSING REPORTS' },
    { path: '/admin/list-firs', label: "LIST FIR'S" }
  ];

  return (
    <PageLayout navLinks={navLinks}>
      <div className="max-w-4xl mx-auto">
        <Card>
          <h1 className="text-3xl font-bold text-center text-[#0A2A43] mb-8">
            CASE DETAILS - #{id}
          </h1>

          <div className="bg-gradient-to-br from-gray-50 to-white rounded-xl p-8 mb-8 min-h-[400px] border border-gray-200">
            <h2 className="text-xl font-bold text-[#0A2A43] mb-4">Case Information</h2>
            <div className="space-y-4 text-[#6D7A86]">
              <p><span className="font-semibold text-[#0A2A43]">Case ID:</span> #{id}</p>
              <p><span className="font-semibold text-[#0A2A43]">Status:</span> Under Investigation</p>
              <p><span className="font-semibold text-[#0A2A43]">Filed Date:</span> Dec 01, 2025</p>
              <p><span className="font-semibold text-[#0A2A43]">Incident Location:</span> Main Street, Downtown</p>
              <p className="pt-4">
                <span className="font-semibold text-[#0A2A43]">Description:</span><br/>
                Detailed case information and investigation notes will appear here. This section contains all relevant details about the incident, witness statements, and evidence collected.
              </p>
            </div>
          </div>

          <div className="flex flex-wrap gap-4 justify-center">
            <Button variant="primary">Update Case</Button>
            <Button variant="navy">Close Case</Button>
            <Button variant="danger">Delete Case</Button>
          </div>
        </Card>
      </div>
    </PageLayout>
  );
}
