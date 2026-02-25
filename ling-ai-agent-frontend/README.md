## Ling AI Agent Frontend

This is a small Vue 3 + Vite frontend that provides:

- A **home page** to switch between apps
- An **AI Love Master** chat page that streams responses via **SSE**
- An **AI Super Agent** page that shows multi‑step execution results via **SSE**

### Tech stack

- Vue 3
- Vite
- Vue Router
- Axios
- Modern CSS (flexbox, responsive layout)

### Backend

The app assumes the Spring Boot backend is running at:

- `http://localhost:8123/api`

With the following endpoints:

- `GET /ai/love_app/chat/sse` – Server‑Sent Events stream for the love app
- `GET /ai/manus/chat` – Server‑Sent Events stream for the super agent

### Getting started

```bash
cd ling-ai-agent-frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

