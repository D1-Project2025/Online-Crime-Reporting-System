export default function AuthLayout({ children, title, subtitle }) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-[#0A2A43] via-[#1a3a53] to-[#6D7A86] flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-block bg-white/10 backdrop-blur-sm px-8 py-3 rounded-full mb-6 border border-white/20">
            <h1 className="text-sm font-semibold text-white tracking-wide">
              ONLINE CRIME REPORTING SYSTEM
            </h1>
          </div>
        </div>

        <div className="bg-white rounded-2xl shadow-2xl p-8 md:p-12">
          <h2 className="text-3xl font-bold text-[#0A2A43] mb-2 text-center">{title}</h2>
          {subtitle && <p className="text-[#6D7A86] text-center mb-8">{subtitle}</p>}
          {children}
        </div>
      </div>
    </div>
  );
}
