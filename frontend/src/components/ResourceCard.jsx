/* eslint-disable no-unused-vars */
export default function ResourceCard({ icon: Icon, title, description, link }) {
        return (
          <div className="bg-white rounded-xl p-6 shadow-md hover:shadow-lg transition-all border border-gray-100 group">
            <div className="inline-block p-3 bg-gradient-to-br from-[#0A2A43]/10 to-[#6D7A86]/10 rounded-lg mb-4 group-hover:scale-110 transition-transform">
              <Icon className="w-10 h-10 text-[#0A2A43]" />
            </div>
            <h3 className="text-xl font-bold text-[#0A2A43] mb-2">{title}</h3>
            <p className="text-[#6D7A86] mb-4 leading-relaxed">{description}</p>
            <button className="text-[#FF6A3D] font-semibold hover:text-[#ff5a2d] transition-colors">
              READ MORE →
            </button>
          </div>
        );
      }
