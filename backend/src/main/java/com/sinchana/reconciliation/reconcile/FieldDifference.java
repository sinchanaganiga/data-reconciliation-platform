package com.sinchana.reconciliation.reconcile;

public record FieldDifference(String column, Object sourceValue, Object targetValue) {
}
