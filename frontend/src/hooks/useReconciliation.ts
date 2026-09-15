import { useCallback, useState } from 'react'
import { reconcileFiles } from '../services/reconciliationService'
import type { ReconciliationConfiguration } from '../types/configuration'
import type { ReconciliationResult } from '../types/reconciliation'

export function useReconciliation() {
  const [result, setResult] = useState<ReconciliationResult | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const reconcile = useCallback(async (
    source: File,
    target: File,
    configuration: ReconciliationConfiguration,
  ) => {
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const response = await reconcileFiles(source, target, configuration)
      setResult(response)
      return response
    } catch (requestError) {
      const message = requestError instanceof Error ? requestError.message : 'Request failed.'
      setError(message)
      return null
    } finally {
      setLoading(false)
    }
  }, [])

  return { result, error, loading, reconcile }
}
