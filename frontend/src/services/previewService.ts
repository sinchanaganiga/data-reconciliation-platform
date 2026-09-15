import type { PreviewResponse } from '../types/preview'

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export async function previewFiles(source: File, target: File): Promise<PreviewResponse> {
  const body = new FormData()
  body.append('source', source)
  body.append('target', target)
  const response = await fetch(`${API_URL}/api/reconciliation/preview`, { method: 'POST', body })
  const payload = await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(payload.message ?? 'The files could not be previewed.')
  return payload as PreviewResponse
}

export async function checkHealth(): Promise<boolean> {
  try {
    const response = await fetch(`${API_URL}/actuator/health`)
    return response.ok
  } catch {
    return false
  }
}
