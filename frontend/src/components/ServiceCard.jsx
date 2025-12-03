/* eslint-disable no-unused-vars */
import { Link } from 'react-router-dom';
import Button from './Button';

export default function ServiceCard({ icon: Icon, title, description, trackPath, registerPath }) {
  return (
    <div className="bg-white rounded-2xl p-8 shadow-lg hover:shadow-xl transition-all border border-gray-100">
      <div className="inline-block p-4 bg-gradient-to-br from-[#FF6A3D]/10 to-[#ff8c66]/10 rounded-xl mb-4">
        <Icon className="w-12 h-12 text-[#FF6A3D]" />
      </div>
      <h3 className="text-2xl font-bold text-[#0A2A43] mb-3">{title}</h3>
      <p className="text-[#6D7A86] mb-6 leading-relaxed">{description}</p>
      <div className="flex gap-3">
        <Link to={trackPath}>
          <Button variant="primary">TRACK</Button>
        </Link>
        <Link to={registerPath}>
          <Button variant="outline">REGISTER</Button>
        </Link>
      </div>
    </div>
  );
}
