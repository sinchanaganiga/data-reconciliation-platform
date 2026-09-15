import { useEffect, useMemo, useState } from 'react'
import { ReconciliationFilters, type ResultFilter } from '../components/ReconciliationFilters'
import { ReconciliationRecordDetails } from '../components/ReconciliationRecordDetails'
import { ReconciliationSummary } from '../components/ReconciliationSummary'
import { ReconciliationTable } from '../components/ReconciliationTable'
import type { ReconciliationResult } from '../types/reconciliation'
import { downloadReconciliationReport } from '../services/reconciliationService'

export function ReconciliationResultsPage({ result }: { result: ReconciliationResult }) {
  const [filter, setFilter] = useState<ResultFilter>('ALL')
  const [selectedRecord, setSelectedRecord] = useState(result.records[0] ?? null)
  const [downloading, setDownloading] = useState(false)
  const [downloadError, setDownloadError] = useState('')
  const filteredRecords = useMemo(
    () => filter === 'ALL' ? result.records : result.records.filter((record) => record.status === filter),
    [filter, result],
  )
  useEffect(() => {
    if (selectedRecord && filteredRecords.includes(selectedRecord)) return
    setSelectedRecord(filteredRecords[0] ?? null)
  }, [filteredRecords, selectedRecord])

  const downloadReport = async () => {
    const runId = result.metadata.runId
    if (!runId) return
    setDownloading(true)
    setDownloadError('')
    try {
      const blob = await downloadReconciliationReport(runId)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'reconciliation-report.xlsx'
      link.click()
      URL.revokeObjectURL(url)
    } catch (error) {
      setDownloadError(error instanceof Error ? error.message : 'The report could not be downloaded.')
    } finally {
      setDownloading(false)
    }
  }

  return (
    <section className="dashboard results" aria-live="polite">
      <div className="section-heading">
        <div>
          <p className="card-kicker">Completed reconciliation</p>
          <h2>Reconciliation results</h2>
          <p className="source-target">{result.metadata.sourceFileName} <span>→</span> {result.metadata.targetFileName}</p>
        </div>
        <span>{filteredRecords.length} records shown</span>
      </div>
      <div className="report-action">
        <button type="button" onClick={() => void downloadReport()} disabled={downloading || !result.metadata.runId}>
          {downloading ? 'Preparing report…' : 'Download Excel report'}
        </button>
        {downloadError && <p className="error" role="alert">{downloadError}</p>}
      </div>
      <ReconciliationSummary result={result} />
      <ReconciliationFilters value={filter} onChange={setFilter} />
      <div className="reconciliation-layout">
        <ReconciliationTable records={filteredRecords} selectedRecord={selectedRecord} onSelect={setSelectedRecord} />
        <ReconciliationRecordDetails record={selectedRecord} />
      </div>
    </section>
  )
}
