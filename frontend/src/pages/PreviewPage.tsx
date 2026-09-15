import { FormEvent, useState } from 'react'
import { DatasetPreviewCard } from '../components/DatasetPreviewCard'
import { FilePicker } from '../components/FilePicker'
import { ConfigurationPanel } from '../components/ConfigurationPanel'
import { usePreview } from '../hooks/usePreview'
import { useReconciliation } from '../hooks/useReconciliation'
import { ReconciliationResultsPage } from './ReconciliationResultsPage'
import { defaultComparisonConfiguration, type ReconciliationConfiguration } from '../types/configuration'

export function PreviewPage() {
  const [source, setSource] = useState<File | null>(null)
  const [target, setTarget] = useState<File | null>(null)
  const [configuration, setConfiguration] = useState<ReconciliationConfiguration>({
    sourceKey: '',
    targetKey: '',
    comparison: defaultComparisonConfiguration,
  })
  const { result, error, loading, preview } = usePreview()
  const reconciliation = useReconciliation()

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!source || !target) return
    await preview(source, target)
  }

  return (
    <main className="shell">
      <header className="hero">
        <div className="brand-mark" aria-hidden="true"><span /> <span /></div>
        <p className="product-name">Data Reconciliation Platform</p>
        <h1>Validate. Reconcile.</h1>
        <p className="intro">Analyze source and target data, identify record-level discrepancies, review field-level differences, and generate reconciliation results.</p>
        <div className="workflow-note" aria-label="Application workflow">
          <span>Upload</span><i>→</i><span>Preview</span><i>→</i><span>Configure</span><i>→</i><span>Reconcile</span><i>→</i><span>Results</span>
        </div>
      </header>
      <form className="upload-card" onSubmit={submit}>
        <div className="section-heading">
          <div><p className="card-kicker">Start with two inputs</p><h2>Choose workbooks</h2></div>
          <span>Excel files only · max 10 MB each</span>
        </div>
        <div className="file-grid">
          <FilePicker label="01 · Source workbook" file={source} onChange={(file) => { setSource(file) }} />
          <FilePicker label="02 · Target workbook" file={target} onChange={(file) => { setTarget(file) }} />
        </div>
        <div className="form-footer">
          <p>We read the first worksheet and return up to 20 sample rows.</p>
          <button type="submit" disabled={loading || !source || !target}>{loading ? 'Profiling…' : 'Preview workbooks →'}</button>
        </div>
        {error && <p className="error" role="alert">{error}</p>}
      </form>
      {result && (
        <section className="results" aria-live="polite">
          <div className="section-heading"><h2>Preview results</h2><span>{result.datasets.length} workbooks profiled</span></div>
          <div className="preview-grid">{result.datasets.map((dataset) => <DatasetPreviewCard key={dataset.fileName} dataset={dataset} />)}</div>
          {result.datasets.length === 2 && (
            <ConfigurationPanel
              source={result.datasets[0]}
              target={result.datasets[1]}
              configuration={configuration}
              onChange={setConfiguration}
              onRun={() => {
                if (source && target) void reconciliation.reconcile(source, target, configuration)
              }}
              loading={reconciliation.loading}
            />
          )}
          {reconciliation.error && <p className="error" role="alert">{reconciliation.error}</p>}
        </section>
      )}
      {reconciliation.result && <ReconciliationResultsPage result={reconciliation.result} />}
    </main>
  )
}
