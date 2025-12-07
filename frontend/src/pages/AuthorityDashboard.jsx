import PageLayout from '../components/PageLayout';
import CaseCard from '../components/CaseCard';

export default function AuthorityDashboard() {
  const navLinks = [
    { path: '/authority/dashboard', label: 'HOME', active: true },
    { path: '/authority/missing-reports', label: 'MISSING REPORTS' },
    { path: '/authority/list-firs', label: "LIST FIR'S" }
  ];

  const cases = [
    { id: 1, name: 'Theft Case - Downtown Area' },
    { id: 2, name: 'Assault Report - Main Street' },
    { id: 3, name: 'Missing Person - Sarah Johnson' },
    { id: 4, name: 'Fraud Investigation - Tech Company' },
    { id: 5, name: 'Vandalism - City Park' },
    { id: 6, name: 'Burglary - Residential Complex' }
  ];

  return (
    <PageLayout navLinks={navLinks}>
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-[#0A2A43] mb-4">Authority Dashboard</h1>
          <p className="text-[#6D7A86] text-lg">Manage and track all assigned cases</p>
        </div>

        <div className="space-y-4">
          {cases.map((caseItem) => (
            <CaseCard
              key={caseItem.id}
              id={caseItem.id}
              name={caseItem.name}
              basePath="/authority"
            />
          ))}
        </div>
      </div>
    </PageLayout>
  );
}
