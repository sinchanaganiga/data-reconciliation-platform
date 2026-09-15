# Running the application in Replit

This project uses two Replit workflows:

- **Frontend** — `cd frontend && VITE_API_URL= npm run dev -- --host 0.0.0.0 --port 5000`
  - Open the Replit web preview on port 5000.
  - Vite proxies `/api` and `/actuator` requests to the backend on port 8080.
- **Backend** — `cd backend && CORS_ALLOWED_ORIGINS=https://$REPLIT_DEV_DOMAIN:5000 mvn spring-boot:run`
  - The Spring Boot API listens on port 8080.
  - Health check: `GET /actuator/health`.
  - The backend keeps `http://localhost:5173` enabled and receives the Replit
    preview origin through `CORS_ALLOWED_ORIGINS`.

The frontend dependencies are installed from `frontend/package-lock.json` with
`npm ci`. The backend uses the Maven configuration in `backend/pom.xml`.

Useful verification commands:

```bash
cd frontend && npm run build
cd backend && mvn test
```