export default function Button({
  children,
  variant = 'primary',
  type = 'button',
  onClick,
  className = '',
  ...props
}) {
  const baseStyles = 'font-semibold py-3 px-6 rounded-lg transition-all transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed';

  const variants = {
    primary: 'bg-gradient-to-r from-[#FF6A3D] to-[#ff8c66] hover:from-[#ff5a2d] hover:to-[#ff7c56] text-white shadow-lg',
    secondary: 'bg-[#6D7A86] hover:bg-[#5d6a76] text-white',
    navy: 'bg-[#0A2A43] hover:bg-[#0d3354] text-white',
    outline: 'border-2 border-[#0A2A43] text-[#0A2A43] hover:bg-[#0A2A43] hover:text-white',
    danger: 'bg-red-500 hover:bg-red-600 text-white'
  };

  return (
    <button
      type={type}
      onClick={onClick}
      className={`${baseStyles} ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
