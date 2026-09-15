import type { ReconciliationStatus } from '../types/reconciliation'

export function StatusBadge({ status }: { status: ReconciliationStatus }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{status}</span>
}
