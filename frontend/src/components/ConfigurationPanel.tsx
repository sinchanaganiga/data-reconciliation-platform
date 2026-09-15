import type { DatasetPreview } from '../types/preview'
import type { ReconciliationConfiguration } from '../types/configuration'

type Props = {
  source: DatasetPreview
  target: DatasetPreview
  configuration: ReconciliationConfiguration
  onChange: (configuration: ReconciliationConfiguration) => void
  onRun: () => void
  loading: boolean
}

const rules = [
  ['trimWhitespace', 'Trim whitespace'],
  ['normalizeInternalWhitespace', 'Normalize repeated internal whitespace'],
  ['caseSensitive', 'Case-sensitive text comparison'],
  ['normalizeNumbers', 'Normalize numeric representations'],
  ['normalizeDates', 'Normalize date representations'],
  ['nullEqualsBlank', 'Treat null as equivalent to blank'],
] as const

export function ConfigurationPanel({ source, target, configuration, onChange, onRun, loading }: Props) {
  const updateRule = (name: keyof typeof configuration.comparison, value: boolean) => {
    onChange({ ...configuration, comparison: { ...configuration.comparison, [name]: value } })
  }

  return (
    <section className="configuration-card" aria-labelledby="configuration-title">
      <div className="section-heading">
        <div>
          <p className="card-kicker">Next step</p>
          <h2 id="configuration-title">Configure reconciliation</h2>
        </div>
        <span>Ready to compare the selected files</span>
      </div>
      <p className="configuration-help">
        The reconciliation key identifies the same record across the source and target files.
      </p>
      <div className="key-grid">
        <label>
          <span><small>Source key</small>{configuration.sourceKey || 'Select a source column'}</span>
          <select value={configuration.sourceKey} onChange={(event) => onChange({ ...configuration, sourceKey: event.target.value })}>
            <option value="">Select a source column</option>
            {source.columns.map((column) => <option key={column} value={column}>{column}</option>)}
          </select>
        </label>
        <label>
          <span><small>Target key</small>{configuration.targetKey || 'Select a target column'}</span>
          <select value={configuration.targetKey} onChange={(event) => onChange({ ...configuration, targetKey: event.target.value })}>
            <option value="">Select a target column</option>
            {target.columns.map((column) => <option key={column} value={column}>{column}</option>)}
          </select>
        </label>
      </div>
      <h3>Comparison rules</h3>
      <div className="rules-grid">
        {rules.map(([name, label]) => (
          <label className="rule" key={name}>
            <input type="checkbox" checked={configuration.comparison[name]} onChange={(event) => updateRule(name, event.target.checked)} />
            <span>{label}</span>
          </label>
        ))}
      </div>
      <div className="configuration-footer">
        <p className="configuration-note">The selected configuration will be used to compare the uploaded workbooks.</p>
        <button type="button" onClick={onRun} disabled={loading || !configuration.sourceKey || !configuration.targetKey}>
          {loading ? 'Reconciling…' : 'Run reconciliation →'}
        </button>
      </div>
    </section>
  )
}
