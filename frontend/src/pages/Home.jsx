import { Link } from 'react-router-dom';
import { Shield, FileText, UserSearch, Activity, BookOpen, HelpCircle, AlertCircle } from 'lucide-react';
import Navbar from '../components/Navbar';
import ServiceCard from '../components/ServiceCard';
import ResourceCard from '../components/ResourceCard';
import Button from '../components/Button';
import Footer from '../components/Footer';

export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      <Navbar showAuth={true} />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="bg-gradient-to-r from-[#0A2A43] via-[#1a3a53] to-[#0A2A43] rounded-3xl p-8 md:p-16 mb-16 text-center shadow-2xl relative overflow-hidden">
          <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PHBhdHRlcm4gaWQ9ImdyaWQiIHdpZHRoPSI2MCIgaGVpZ2h0PSI2MCIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHBhdGggZD0iTSAxMCAwIEwgMCAwIDAgMTAiIGZpbGw9Im5vbmUiIHN0cm9rZT0id2hpdGUiIHN0cm9rZS1vcGFjaXR5PSIwLjAzIiBzdHJva2Utd2lkdGg9IjEiLz48L3BhdHRlcm4+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9InVybCgjZ3JpZCkiLz48L3N2Zz4=')] opacity-30"></div>

          <div className="relative z-10">
            <div className="inline-block mb-6">
              <Shield className="w-20 h-20 text-[#FF6A3D] mx-auto animate-pulse" />
            </div>
            <h1 className="text-4xl md:text-5xl font-bold text-white mb-2 leading-tight">
              YOUR SAFETY, OUR PRIORITY.<br />REPORT CRIME ONLINE.
            </h1>
            <p className="text-gray-200 text-lg mb-10 max-w-3xl mx-auto leading-relaxed">
              Empowering citizens to contribute to a safer community with easy and efficient online crime reporting
            </p>

            <div className="flex flex-wrap justify-center gap-4 mb-6">
              <Link to="/file-fir">
                <Button variant="primary">FILE FIR</Button>
              </Link>
              <Link to="/missing-report">
                <Button variant="secondary">REPORT MISSING PERSON</Button>
              </Link>
              <Link to="/track-status">
                <Button variant="secondary">TRACK STATUS</Button>
              </Link>
            </div>

            <Link to="/emergency">
              <Button variant="danger">EMERGENCY CONTACT</Button>
            </Link>
          </div>
        </div>

        <section className="mb-16">
          <h2 className="text-3xl font-bold text-center text-[#0A2A43] mb-12">KEY SERVICES</h2>
          <div className="grid md:grid-cols-2 gap-8 mb-8">
            <ServiceCard
              icon={FileText}
              title="FIR REPORT"
              description="Easily submit your FIR online for faster processing. Our streamlined procedure ensures efficient reporting from anywhere, anytime."
              trackPath="/track-status"
              registerPath="/file-fir"
            />
            <ServiceCard
              icon={UserSearch}
              title="REPORT MISSING PERSON"
              description="Quickly report and provide crucial details for missing persons. Join our collective efforts to reunite families."
              trackPath="/track-status"
              registerPath="/missing-report"
            />
          </div>

          <div className="max-w-2xl mx-auto">
            <ServiceCard
              icon={Activity}
              title="TRACK YOUR CASE STATUS"
              description="Stay informed with real-time progress updates on your filed FIRs and missing person reports. We provide detailed status tracking."
              trackPath="/track-status"
              registerPath="/signup"
            />
          </div>
        </section>

        <section>
          <h2 className="text-3xl font-bold text-center text-[#0A2A43] mb-12">CITIZEN RESOURCES</h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            <ResourceCard
              icon={Shield}
              title="SAFETY TIPS"
              description="Discover essential safety tips to safeguard yourself, your family, and your community."
            />
            <ResourceCard
              icon={BookOpen}
              title="MANUAL GUIDES"
              description="Access comprehensive guides on legal procedures, citizen rights, and community safety initiatives."
            />
            <ResourceCard
              icon={HelpCircle}
              title="FAQ(S)"
              description="Find answers to common questions about crime reporting, case tracking, and other features."
            />
            <ResourceCard
              icon={AlertCircle}
              title="EMERGENCY CONTACTS"
              description="A quick reference for emergency numbers, helplines, and important contacts in your area."
            />
          </div>
        </section>
      </main>

      <Footer />
    </div>
  );
}
