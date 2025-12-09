import { Link } from 'react-router-dom';
import { Shield, Phone, FileText, Mail } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-linear-to-r from-[#0A2A43] to-[#1a3a53] text-white mt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid md:grid-cols-3 gap-8 mb-8">
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <Shield className="w-8 h-8 text-[#FF6A3D]" />
              <span className="text-xl font-bold">Crime Report</span>
            </div>
            <p className="text-gray-300 leading-relaxed">
              Empowering citizens to contribute to a safer community with easy and efficient online crime reporting.
            </p>
          </div>

          <div>
            <h3 className="text-lg font-bold mb-4 text-[#FF6A3D]">Quick Links</h3>
            <div className="space-y-2">
              <Link to="/resources" className="block text-gray-300 hover:text-[#FF6A3D] transition-colors">
                Resources
              </Link>
              <Link to="/legal" className="block text-gray-300 hover:text-[#FF6A3D] transition-colors">
                Legal Information
              </Link>
              <Link to="/contact" className="block text-gray-300 hover:text-[#FF6A3D] transition-colors">
                Contact Us
              </Link>
              <Link to="/faq" className="block text-gray-300 hover:text-[#FF6A3D] transition-colors">
                FAQ
              </Link>
            </div>
          </div>

          <div>
            <h3 className="text-lg font-bold mb-4 text-[#FF6A3D]">Contact</h3>
            <div className="space-y-3">
              <div className="flex items-center space-x-3">
                <Phone className="w-5 h-5 text-[#FF6A3D]" />
                <span className="text-gray-300">Emergency: 911</span>
              </div>
              <div className="flex items-center space-x-3">
                <Mail className="w-5 h-5 text-[#FF6A3D]" />
                <span className="text-gray-300">support@crimereport.com</span>
              </div>
            </div>
          </div>
        </div>

        <div className="border-t border-white/10 pt-8 flex flex-wrap justify-between items-center">
          <p className="text-gray-400 text-sm">
            © 2025 Online Crime Reporting System. All rights reserved.
          </p>
          <div className="flex space-x-4 mt-4 md:mt-0">
            <Shield className="w-5 h-5 text-gray-400 hover:text-[#FF6A3D] cursor-pointer transition-colors" />
            <Phone className="w-5 h-5 text-gray-400 hover:text-[#FF6A3D] cursor-pointer transition-colors" />
            <FileText className="w-5 h-5 text-gray-400 hover:text-[#FF6A3D] cursor-pointer transition-colors" />
          </div>
        </div>
      </div>
    </footer>
  );
}
