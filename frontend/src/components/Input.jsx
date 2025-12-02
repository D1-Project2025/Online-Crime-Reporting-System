export default function Input({
  label,
  type = 'text',
  placeholder = '',
  value,
  onChange,
  required = false,
  className = '',
  ...props
}) {
  return (
    <div className={className}>
      {label && (
        <label className="block text-sm font-medium text-[#0A2A43] mb-2">
          {label}
          {required && <span className="text-[#FF6A3D] ml-1">*</span>}
        </label>
      )}
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        required={required}
        className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg focus:ring-2 focus:ring-[#FF6A3D] focus:border-transparent focus:bg-white transition-all"
        {...props}
      />
    </div>
  );
}
