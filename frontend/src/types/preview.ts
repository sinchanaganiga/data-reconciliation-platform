export type DataType = 'STRING' | 'INTEGER' | 'DECIMAL' | 'BOOLEAN' | 'DATE' | 'DATETIME' | 'MIXED' | 'UNKNOWN'

export type ColumnProfile = {
  name: string
  dataType: DataType
  totalCount: number
  nonNullCount: number
  nullCount: number
  blankCount: number
  populatedCount: number
  distinctCount: number
  duplicateCount: number
  nullPercentage: number
  blankPercentage: number
  uniquenessPercentage: number
  min: unknown
  max: unknown
  mean: number | null
  median: number | null
  standardDeviation: number | null
  minLength: number | null
  maxLength: number | null
  sampleValues: unknown[]
}

export type DataProfile = {
  rowCount: number
  columnCount: number
  columns: ColumnProfile[]
  metadata: Record<string, unknown>
}

export type DatasetPreview = {
  fileName: string
  rowCount: number
  columnCount: number
  columns: string[]
  rows: Record<string, unknown>[]
  profile: DataProfile
}

export type PreviewResponse = { datasets: DatasetPreview[] }
