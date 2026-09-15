# Data Reconciliation Platform

The Data Reconciliation Platform compares two Excel workbooks as a source and
target pair. It helps users inspect uploaded datasets, configure how records
correspond, identify reconciliation outcomes, review field-level differences,
and download an Excel report.

The platform is generic: it detects columns at runtime and does not depend on a
particular business schema or fixed set of field names.

## Capabilities

### Upload, preview, and profiling

- Accepts `.xls` and `.xlsx` workbooks.
- Reads the first worksheet.
- Preserves typed Excel values such as strings, numbers, booleans, dates, and
  formula results.
- Detects source and target columns dynamically.
- Displays a bounded preview of the uploaded data.
- Profiles columns with detected data type, populated and blank counts,
  distinct-value counts, and applicable numeric or string statistics.
- Rejects blank or duplicate headers and unsupported or unreadable files.

### Reconciliation configuration

Users select a reconciliation key independently for the source and target
datasets, so the two key columns may have different names. Comparison rules
include:

- trimming outer whitespace
- normalizing repeated internal whitespace
- case-sensitive or case-insensitive text comparison
- numeric normalization
- date normalization
- treating null and blank values as equivalent

### Reconciliation results

Each logical result is classified as one of:

- **MATCHED** — source and target records agree under the configured rules.
- **CHANGED** — corresponding records exist but one or more comparable fields
  differ.
- **MISSING** — a source record has no corresponding target record.
- **EXTRA** — a target record has no corresponding source record.
- **DUPLICATE** — the configured key occurs more than once on either side.
- **INVALID_KEY** — a source or target record has a null or blank configured
  reconciliation key.

The results view supports status filtering, record selection, record-level
source/target inspection, duplicate record inspection, and field-level
difference inspection. Original values are retained for reporting while
configured normalization is used for comparison.

Completed reconciliation runs can also be exported as an Excel workbook with
summary, record-result, and field-difference sheets.

## Architecture

### Backend

The backend is a Java 17 Spring Boot application organized around focused
components:

- readers and reader factories for workbook ingestion
- dataset validation and profiling
- normalization and comparison configuration
- key-based record matching
- reconciliation result models and summary counts
- REST controllers and services
- Excel report export
- structured exception handling

The main backend packages are under
`com.sinchana.reconciliation`.

### Frontend

The frontend is a React and TypeScript application built with Vite. It
separates:

- API services
- domain types
- stateful hooks
- reusable upload, preview, configuration, filter, table, summary, and detail
  components
- workflow pages and shared styling

## Technologies

- Java 17
- Spring Boot
- Apache POI
- Maven
- React 18
- TypeScript
- Vite
- Node.js 18 or later

## Run locally

### Prerequisites

- Java 17 or later
- Maven 3.9 or later
- Node.js 18 or later

### Start the backend

```powershell
cd backend
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`. Health is available at
`GET /actuator/health`.

### Start the frontend

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open the Vite URL, normally `http://localhost:5173`. Set `VITE_API_URL` if
the backend is hosted at a different URL.

## Main API endpoints

- `POST /api/reconciliation/preview` — upload source and target workbooks for
  preview and profiling.
- `POST /api/reconciliation/configuration/validate` — validate a selected
  source/target key configuration.
- `POST /api/reconciliation/execute` — execute reconciliation for two uploaded
  workbooks and a configuration.
- `GET /api/reconciliation/{runId}/report` — download the Excel report for a
  completed run.
- `GET /actuator/health` — application health check.

## Verification

Run the backend test suite:

```powershell
cd backend
mvn test
```

Build the frontend:

```powershell
cd frontend
npm run build
```

## Current limitations

- Only the first worksheet in each workbook is processed.
- Supported upload formats are `.xls` and `.xlsx`.
- Completed reconciliation results are stored in a bounded in-memory run
  store, so they are not persistent across application restarts.
- The preview is intentionally bounded rather than a complete data browser.
- Comparison depends on the selected reconciliation keys and configured
  normalization rules; no business-specific matching policy is included.
