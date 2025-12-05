export default function Card({
  children,
  className = '',
  hover = false,
  ...props
}) {
  const hoverStyles = hover ? 'hover:shadow-xl hover:scale-[1.01] cursor-pointer' : '';

  return (
    <div
      className={`bg-white rounded-2xl shadow-lg p-6 transition-all ${hoverStyles} ${className}`}
      {...props}
    >
      {children}
    </div>
  );
}
