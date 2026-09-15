import { formatValue } from '../utils/formatValue'
import type { RecordReconciliation } from '../types/reconciliation'
import { StatusBadge } from './StatusBadge'

function recordColumns(record: RecordReconciliation) {
  return [...new Set([...Object.keys(record.sourceRecord), ...Object.keys(record.targetRecord)])]
}

function valuesForRecords(records: Record<string, unknown>[]) {
  return [...new Set(records.flatMap((record) => Object.keys(record)))]
}

function RecordValues({
  label,
  records,
}: {
  label: string
  records: Record<string, unknown>[]
}) {
  const columns = valuesForRecords(records)
  return (
    <section>
      <h4>{label} ({records.length})</h4>
      {records.length === 0
        ? <p className="empty">No records.</p>
        : records.map((values, recordIndex) => (
          <article className="duplicate-record" key={`${label}-${recordIndex}`}>
            <h5>Record {recordIndex + 1}</h5>
            {columns.map((column) => (
              <div className="value-row" key={`${label}-${recordIndex}-${column}`}>
                <b>{column}</b><span>{formatValue(values[column])}</span>
              </div>
            ))}
          </article>
        ))}
    </section>
  )
}

export function ReconciliationRecordDetails({ record }: { record: RecordReconciliation | null }) {
  if (!record) {
    return <aside className="record-details empty-details"><strong>No record selected</strong><span>Select a reconciliation result to view its source and target values.</span></aside>
  }

  const columns = recordColumns(record)
  return (
    <aside className="record-details">
      <div className="details-heading">
        <div>
          <p className="card-kicker">Selected result</p>
          <h3>Record details</h3>
        </div>
        <StatusBadge status={record.status} />
      </div>
      <div className="details-key"><span>Record key</span><strong>{formatValue(record.key)}</strong></div>
      {record.status === 'DUPLICATE' ? (
        <div className="duplicate-details">
          <div className="duplicate-summary">
            <span>Source records: <b>{record.sourceRecords.length}</b></span>
            <span>Target records: <b>{record.targetRecords.length}</b></span>
          </div>
          <div className="duplicate-row-indexes">
            <span>Source row indexes: <b>{record.sourceRowIndices.join(', ') || '—'}</b></span>
            <span>Target row indexes: <b>{record.targetRowIndices.join(', ') || '—'}</b></span>
          </div>
          <div className="record-values">
            <RecordValues label="Source records" records={record.sourceRecords} />
            <RecordValues label="Target records" records={record.targetRecords} />
          </div>
        </div>
      ) : (
        <div className="record-values">
          <section className="value-section source-section">
            <h4>Source</h4>
            {columns.map((column) => (
              <div className="value-row" key={`source-${column}`}>
                <b>{column}</b><span>{formatValue(record.sourceRecord[column])}</span>
              </div>
            ))}
          </section>
          <section className="value-section target-section">
            <h4>Target</h4>
            {columns.map((column) => (
              <div className="value-row" key={`target-${column}`}>
                <b>{column}</b><span>{formatValue(record.targetRecord[column])}</span>
              </div>
            ))}
          </section>
        </div>
      )}
      <section className="differences">
        <h4>Field differences</h4>
        {record.differences.length === 0
          ? <p className="empty">The selected record has no detected field-level differences.</p>
          : record.differences.map((difference) => (
            <div className="difference-row" key={difference.column}>
              <b>{difference.column}</b>
              <span><small>Source</small>{formatValue(difference.sourceValue)}</span>
              <span><small>Target</small>{formatValue(difference.targetValue)}</span>
            </div>
          ))}
      </section>
    </aside>
  )
}
