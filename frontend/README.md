# Frontend

React/Vite web client for the Telecom Campaign Management Platform.

## Development

```bash
npm ci
npm run dev
```

Vite serves the application at `http://localhost:5173` by default. The client sends requests to the relative `/api` path unless `VITE_BACKEND_URL` is set:

```dotenv
VITE_BACKEND_URL=http://localhost:8081
```

## Available Commands

| Command | Purpose |
| --- | --- |
| `npm run dev` | Start the Vite development server |
| `npm run build` | Create a production bundle in `dist/` |
| `npm run lint` | Run ESLint |
| `npm run preview` | Serve the production bundle locally |

## Production Container

The Dockerfile builds the app with Node 20 and serves the generated bundle through Nginx. Nginx serves the SPA fallback and proxies `/api/` to `BACKEND_URL`. The root project README contains the complete Docker Compose setup.

## Client Behavior

- Axios attaches the JWT from `localStorage` as a Bearer token.
- A `401` response clears the token and redirects the browser to `/login`.
- Authentication state and route access are managed by the existing context and route modules.
