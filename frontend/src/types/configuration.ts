export type ComparisonConfiguration = {
  trimWhitespace: boolean
  normalizeInternalWhitespace: boolean
  caseSensitive: boolean
  normalizeNumbers: boolean
  normalizeDates: boolean
  nullEqualsBlank: boolean
}

export type ReconciliationConfiguration = {
  sourceKey: string
  targetKey: string
  comparison: ComparisonConfiguration
}

export const defaultComparisonConfiguration: ComparisonConfiguration = {
  trimWhitespace: true,
  normalizeInternalWhitespace: false,
  caseSensitive: true,
  normalizeNumbers: true,
  normalizeDates: true,
  nullEqualsBlank: false,
}
