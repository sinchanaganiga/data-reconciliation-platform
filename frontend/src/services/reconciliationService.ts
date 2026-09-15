import type { ReconciliationConfiguration } from '../types/configuration'
import type { ReconciliationResult } from '../types/reconciliation'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export async function reconcileFiles(
  source: File,
  target: File,
  configuration: ReconciliationConfiguration,
): Promise<ReconciliationResult> {
  const body = new FormData()
  body.append('source', source)
  body.append('target', target)
  body.append('configuration', new Blob([JSON.stringify(configuration)], { type: 'application/json' }))

  const response = await fetch(`${API_URL}/api/reconciliation/execute`, { method: 'POST', body })
  const payload = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(payload.message ?? 'The files could not be reconciled.')
  return payload as ReconciliationResult
}

export async function downloadReconciliationReport(runId: string): Promise<Blob> {
  const response = await fetch(`${API_URL}/api/reconciliation/${encodeURIComponent(runId)}/report`)
  if (!response.ok) {
    const payload = await response.json().catch(() => ({}))
    throw new Error(payload.message ?? 'The reconciliation report could not be downloaded.')
  }
  return response.blob()
}
