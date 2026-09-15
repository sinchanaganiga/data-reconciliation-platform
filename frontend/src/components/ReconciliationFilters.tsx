import type { ReconciliationStatus } from '../types/reconciliation'

export type ResultFilter = 'ALL' | ReconciliationStatus

const filters: ResultFilter[] = ['ALL', 'MATCHED', 'CHANGED', 'MISSING', 'EXTRA', 'DUPLICATE', 'INVALID_KEY']

export function ReconciliationFilters({
  value,
  onChange,
}: {
  value: ResultFilter
  onChange: (value: ResultFilter) => void
}) {
  return (
    <div className="result-filters" aria-label="Filter reconciliation results">
      <span className="filter-label">Filter results</span>
      {filters.map((filter) => (
        <button
          className={value === filter ? 'filter-button active' : 'filter-button'}
          key={filter}
          type="button"
          onClick={() => onChange(filter)}
          aria-pressed={value === filter}
        >
          {filter}
        </button>
      ))}
    </div>
  )
}
