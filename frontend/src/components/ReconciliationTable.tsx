import { formatValue } from '../utils/formatValue'
import type { RecordReconciliation } from '../types/reconciliation'
import { StatusBadge } from './StatusBadge'

export function ReconciliationTable({
  records,
  selectedRecord,
  onSelect,
}: {
  records: RecordReconciliation[]
  selectedRecord: RecordReconciliation | null
  onSelect: (record: RecordReconciliation) => void
}) {
  return (
    <div className="table-wrap result-table-wrap">
      <table className="result-table">
        <thead>
          <tr>
            <th>Status</th>
            <th>Key</th>
            <th>Source row</th>
            <th>Target row</th>
            <th>Differences</th>
          </tr>
        </thead>
        <tbody>
          {records.map((record, index) => (
            <tr
              className={selectedRecord === record ? 'result-row selected' : 'result-row'}
              key={`${record.status}-${record.sourceRowIndex ?? 'none'}-${record.targetRowIndex ?? 'none'}-${index}`}
              onClick={() => onSelect(record)}
            >
              <td><StatusBadge status={record.status} /></td>
              <td>{formatValue(record.key)}</td>
              <td>{formatValue(record.sourceRowIndex)}</td>
              <td>{formatValue(record.targetRowIndex)}</td>
              <td>{record.differences.length}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {records.length === 0 && <p className="empty table-empty">No results match this filter. Try another status or clear the current filter.</p>}
    </div>
  )
}
