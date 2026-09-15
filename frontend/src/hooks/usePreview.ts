import { useCallback, useState } from 'react'
import { previewFiles } from '../services/previewService'
import type { PreviewResponse } from '../types/preview'

export function usePreview() {
  const [result, setResult] = useState<PreviewResponse | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const preview = useCallback(async (source: File, target: File) => {
    setLoading(true)
    setError('')
    setResult(null)
    try {
      const response = await previewFiles(source, target)
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

  return { result, error, loading, preview }
}
