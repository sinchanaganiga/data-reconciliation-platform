export type ReconciliationStatus = 'MATCHED' | 'CHANGED' | 'MISSING' | 'EXTRA' | 'DUPLICATE' | 'INVALID_KEY'

export type FieldDifference = {
  column: string
  sourceValue: unknown
  targetValue: unknown
}

export type RecordReconciliation = {
  status: ReconciliationStatus
  key: unknown
  sourceRowIndex: number | null
  targetRowIndex: number | null
  sourceRecord: Record<string, unknown>
  targetRecord: Record<string, unknown>
  sourceRowIndices: number[]
  targetRowIndices: number[]
  sourceRecords: Record<string, unknown>[]
  targetRecords: Record<string, unknown>[]
  differences: FieldDifference[]
}

export type ReconciliationSummary = {
  total: number
  matched: number
  changed: number
  missing: number
  extra: number
  duplicate: number
  invalidKey: number
}

export type ReconciliationMetadata = {
  runId: string | null
  sourceFileName: string | null
  targetFileName: string | null
  sourceKey: string | null
  targetKey: string | null
  executedAt: string | null
}

export type ReconciliationResult = {
  records: RecordReconciliation[]
  summary: ReconciliationSummary
  metadata: ReconciliationMetadata
}
