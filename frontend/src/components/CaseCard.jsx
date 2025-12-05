import { Link } from 'react-router-dom';
import { FileText, ChevronRight } from 'lucide-react';

export default function CaseCard({ id, name, basePath }) {
  return (
    <Link
      to={`${basePath}/case/${id}`}
      className="block bg-gradient-to-r from-white to-gray-50 rounded-xl p-6 transition-all transform hover:scale-[1.02] shadow-md hover:shadow-xl border border-gray-100"
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <div className="p-3 bg-gradient-to-br from-[#FF6A3D]/10 to-[#ff8c66]/10 rounded-lg">
            <FileText className="w-6 h-6 text-[#FF6A3D]" />
          </div>
          <h2 className="text-xl font-bold text-[#0A2A43]">{name}</h2>
        </div>
        <ChevronRight className="w-6 h-6 text-[#6D7A86]" />
      </div>
    </Link>
  );
}
