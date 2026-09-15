import type { DatasetPreview } from '../types/preview'
import { formatValue } from '../utils/formatValue'

export function DatasetPreviewCard({ dataset }: { dataset: DatasetPreview }) {
  return (
    <article className="dataset-card">
      <div className="dataset-heading">
        <div>
          <p className="card-kicker">Workbook</p>
          <h3 title={dataset.fileName}>{dataset.fileName}</h3>
        </div>
        <span>{dataset.rowCount} rows · {dataset.columnCount} columns</span>
      </div>
      <div className="table-wrap">
        <table>
          <thead><tr>{dataset.columns.map((column) => <th key={column}>{column}</th>)}</tr></thead>
          <tbody>
            {dataset.rows.map((row, rowIndex) => (
              <tr key={rowIndex}>{dataset.columns.map((column) => <td key={column}>{formatValue(row[column])}</td>)}</tr>
            ))}
          </tbody>
        </table>
        {dataset.rows.length === 0 && <p className="empty">No data rows found.</p>}
      </div>
      <div className="profile">
        <div className="profile-heading">
          <span className="profile-label">Data quality profile</span>
          <span className="profile-caption">Detected from uploaded data</span>
        </div>
        <div className="profile-items">
        {dataset.profile.columns.map((column) => (
          <span className="profile-item" key={column.name} title={`${column.distinctCount} distinct values`}>
            <b>{column.name}</b> · {column.dataType} · {column.populatedCount} filled
          </span>
        ))}
        </div>
      </div>
    </article>
  )
}
