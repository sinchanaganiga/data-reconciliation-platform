import type { ReconciliationResult, ReconciliationStatus } from '../types/reconciliation'

const statuses: ReconciliationStatus[] = ['MATCHED', 'CHANGED', 'MISSING', 'EXTRA', 'DUPLICATE', 'INVALID_KEY']

export function getReconciliationCounts(result: ReconciliationResult) {
  return {
    MATCHED: result.summary.matched,
    CHANGED: result.summary.changed,
    MISSING: result.summary.missing,
    EXTRA: result.summary.extra,
    DUPLICATE: result.summary.duplicate,
    INVALID_KEY: result.summary.invalidKey,
  }
}

export function ReconciliationSummary({ result }: { result: ReconciliationResult }) {
  const counts = getReconciliationCounts(result)
  return (
    <div className="summary-grid">
      <article className="summary-card summary-total">
        <span>Total results</span>
        <strong>{result.summary.total}</strong>
      </article>
      {statuses.map((status) => (
        <article className={`summary-card summary-${status.toLowerCase()}`} key={status}>
          <span>{status}</span>
          <strong>{counts[status]}</strong>
        </article>
      ))}
    </div>
  )
}
