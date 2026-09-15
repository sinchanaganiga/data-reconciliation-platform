import type { ChangeEvent } from 'react'

type Props = { label: string; file: File | null; onChange: (file: File | null) => void }

export function FilePicker({ label, file, onChange }: Props) {
  const handleChange = (event: ChangeEvent<HTMLInputElement>) => onChange(event.target.files?.[0] ?? null)
  return (
    <label className="file-picker">
      <span>{label}</span>
      <input type="file" accept=".xls,.xlsx" onChange={handleChange} />
      <strong>{file?.name ?? 'Choose an .xls or .xlsx workbook'}</strong>
      <small>{file ? `${file.type || 'Excel workbook'} · ${(file.size / 1024).toFixed(0)} KB · Ready` : 'One worksheet is read for preview'}</small>
    </label>
  )
}
