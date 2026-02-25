# Ling AI Agent 🤖

An enterprise-grade AI application platform built with Spring AI 1.0.0, featuring a RAG-powered relationship coaching chatbot and an autonomous AI agent capable of multi-step task execution.

> **Containerized with Docker and pushed to Alibaba Cloud Container Registry (ACR)**

---

## 📸 Screenshots

### 🏠 Home Page
![Home Page](docs/images/homepage.png)

### 💕 AI Love Coach
![Love Coach 1](docs/images/lovecoah-1.png)
![Love Coach 2](docs/images/lovecoah2.png)

### 🤖 AI Super Agent
![Super Agent 1](docs/images/super minus 1.png)
![Super Agent 2](docs/images/super minus 2.png)

### 📄 PDF Generation Result
![PDF Result](docs/images/pdf.png)

## ✨ Features

### AI Love Coach
- Real-time streaming responses via **SSE (Server-Sent Events)**
- **RAG Pipeline** with PGVector semantic search for domain-specific knowledge retrieval
- Persistent chat memory across sessions using Spring AI `MessageWindowChatMemory`
- PDF report generation summarizing conversation insights
- MCP integration for image search (Pexels API) and location recommendations (Amap)

### AI Super Agent (LingManus)
- Autonomous **ReAct Agent** (Reasoning + Acting loop) with up to 20 steps
- **6 built-in tools**: web search, web scraping, file operations, terminal, resource download, PDF generation
- Stuck-loop detection (`isStuck()`) with automatic recovery strategy
- Real-time step-by-step streaming output via SSE
- `AskHuman` tool for interactive clarification when task is ambiguous

---

## 🏗️ Architecture

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Ling AI Agent — Architecture</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: 'Segoe UI', system-ui, sans-serif;
    background: #f8f9fa;
    padding: 40px;
    min-width: 1000px;
  }
  h1 {
    text-align: center;
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 32px;
    letter-spacing: 0.5px;
  }
  .diagram {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-width: 1000px;
    margin: 0 auto;
  }

  /* Layer */
  .layer {
    display: flex;
    align-items: stretch;
    gap: 12px;
  }
  .layer-label {
    width: 110px;
    min-width: 110px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 700;
    color: #fff;
    border-radius: 8px;
    padding: 10px 8px;
    text-align: center;
    line-height: 1.3;
  }
  .layer-content {
    flex: 1;
    border-radius: 10px;
    padding: 14px 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
  }

  /* Colors */
  .label-client   { background: #4a90d9; }
  .label-server   { background: #e8a838; }
  .label-app      { background: #e8a838; }
  .label-spring   { background: #5aaa6e; }
  .label-external { background: #4a90d9; }

  .bg-client   { background: #dbeafe; border: 1.5px solid #93c5fd; }
  .bg-server   { background: #fef9c3; border: 1.5px solid #fde68a; }
  .bg-app      { background: #fff7e6; border: 1.5px solid #fcd34d; }
  .bg-spring   { background: #dcfce7; border: 1.5px solid #86efac; }
  .bg-external { background: #dbeafe; border: 1.5px solid #93c5fd; }

  /* Box */
  .box {
    background: #fff;
    border: 1.5px solid #d1d5db;
    border-radius: 8px;
    padding: 8px 14px;
    font-size: 13px;
    font-weight: 600;
    color: #1f2937;
    white-space: nowrap;
  }
  .box.highlight {
    border-color: #3b82f6;
    background: #eff6ff;
    color: #1d4ed8;
  }

  /* Arrow */
  .arrow {
    text-align: center;
    font-size: 12px;
    color: #6b7280;
    padding: 2px 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }
  .arrow::before, .arrow::after {
    content: '';
    flex: 1;
    height: 1px;
    background: #d1d5db;
    max-width: 400px;
  }

  /* Spring AI inner grid */
  .spring-grid {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr 1fr;
    gap: 10px;
    width: 100%;
  }
  .spring-col {
    background: #fff;
    border: 1.5px solid #86efac;
    border-radius: 8px;
    padding: 10px;
  }
  .spring-col-title {
    font-size: 12px;
    font-weight: 700;
    color: #166534;
    margin-bottom: 8px;
    text-align: center;
    border-bottom: 1px solid #bbf7d0;
    padding-bottom: 6px;
  }
  .spring-item {
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
    border-radius: 5px;
    padding: 5px 8px;
    font-size: 11.5px;
    color: #166534;
    margin-bottom: 5px;
    text-align: center;
    font-weight: 500;
  }
  .spring-item:last-child { margin-bottom: 0; }

  /* Advisors section */
  .advisors-section {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 10px;
    width: 100%;
    margin-bottom: 0;
  }
  .advisor-col {
    background: #fff;
    border: 1.5px solid #86efac;
    border-radius: 8px;
    padding: 10px;
  }
  .advisor-col-title {
    font-size: 12px;
    font-weight: 700;
    color: #166534;
    margin-bottom: 8px;
    text-align: center;
    border-bottom: 1px solid #bbf7d0;
    padding-bottom: 6px;
  }

  /* LLM row */
  .llm-row {
    display: flex;
    gap: 8px;
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
  .llm-box {
    background: #fff;
    border: 1.5px solid #d1d5db;
    border-radius: 8px;
    padding: 7px 18px;
    font-size: 12.5px;
    font-weight: 700;
    color: #374151;
    text-align: center;
  }
  .llm-box.active {
    border-color: #f59e0b;
    background: #fffbeb;
    color: #92400e;
  }

  /* External icons row */
  .ext-grid {
    display: flex;
    gap: 10px;
    width: 100%;
    flex-wrap: wrap;
    align-items: center;
  }
  .ext-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 5px;
    flex: 1;
    min-width: 80px;
  }
  .ext-icon {
    width: 42px;
    height: 42px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    background: #fff;
    border: 1.5px solid #d1d5db;
  }
  .ext-label {
    font-size: 11px;
    color: #374151;
    font-weight: 600;
    text-align: center;
  }

  .spring-wrapper {
    display: flex;
    flex-direction: column;
    gap: 10px;
    width: 100%;
  }
  .interact-label {
    text-align: center;
    font-size: 11px;
    color: #6b7280;
    margin: 2px 0;
  }
</style>
</head>
<body>

<h1>Ling AI Agent — System Architecture</h1>

<div class="diagram">

  <!-- CLIENT -->
  <div class="layer">
    <div class="layer-label label-client">Client</div>
    <div class="layer-content bg-client">
      <div class="box">Web Browser</div>
      <div class="box">Server</div>
      <div class="box">Mini Program</div>
      <div class="box">Desktop App</div>
    </div>
  </div>

  <div class="arrow">Conversation Request (HTTP / SSE)</div>

  <!-- SERVER -->
  <div class="layer">
    <div class="layer-label label-server">Server</div>
    <div class="layer-content bg-server">
      <div class="box highlight">Serverless / Docker Deployment</div>
      <div class="box">Tomcat Application Server</div>
    </div>
  </div>

  <!-- APPLICATION LAYER -->
  <div class="layer">
    <div class="layer-label label-app">Application Layer</div>
    <div class="layer-content bg-app">
      <div class="box">AI Love Coach</div>
      <div class="box">AI Super Agent</div>
      <div class="box">Other App 1</div>
      <div class="box">Other App 2</div>
    </div>
  </div>

  <!-- SPRING AI LAYER -->
  <div class="layer">
    <div class="layer-label label-spring">Spring AI<br>Services</div>
    <div class="layer-content bg-spring">
      <div class="spring-wrapper">

        <!-- Advisors + ChatModel + Advisors -->
        <div class="advisors-section">
          <div class="advisor-col">
            <div class="advisor-col-title">Pre-Advisors</div>
            <div class="spring-item">Security Guard</div>
            <div class="spring-item">ReReading</div>
            <div class="spring-item">Query Rewriter</div>
          </div>

          <div class="advisor-col">
            <div class="advisor-col-title">ChatModel / ChatClient</div>
            <div class="llm-row" style="margin-top:4px">
              <div class="llm-box active">Qwen-Plus</div>
              <div class="llm-box active">GPT-4o</div>
              <div class="llm-box active">DeepSeek</div>
            </div>
          </div>

          <div class="advisor-col">
            <div class="advisor-col-title">Post-Advisors</div>
            <div class="spring-item">Chat Memory</div>
            <div class="spring-item">Logger</div>
          </div>
        </div>

        <div class="interact-label">↕ interact</div>

        <!-- RAG / Tools / MCP / Memory -->
        <div class="spring-grid">
          <div class="spring-col">
            <div class="spring-col-title">RAG</div>
            <div class="spring-item">ETL Pipeline</div>
            <div class="spring-item">Search Enhancement</div>
            <div class="spring-item">VectorStore (PGVector)</div>
          </div>
          <div class="spring-col">
            <div class="spring-col-title">Tools (6)</div>
            <div class="spring-item">File Operations</div>
            <div class="spring-item">Resource Download</div>
            <div class="spring-item">PDF Generation</div>
            <div class="spring-item">Web Search</div>
            <div class="spring-item">Web Scraping</div>
            <div class="spring-item">Terminal</div>
          </div>
          <div class="spring-col">
            <div class="spring-col-title">MCP</div>
            <div class="spring-item">MCP Client</div>
            <div class="spring-item">MCP Server<br>(Image Search)</div>
            <div class="spring-item">Amap Location</div>
          </div>
          <div class="spring-col">
            <div class="spring-col-title">Memory</div>
            <div class="spring-item">In-Memory<br>ChatMemory</div>
            <div class="spring-item">File-based<br>ChatMemory</div>
          </div>
        </div>

      </div>
    </div>
  </div>

  <div class="arrow">invoke</div>

  <!-- EXTERNAL DEPENDENCIES -->
  <div class="layer">
    <div class="layer-label label-external">External<br>Dependencies</div>
    <div class="layer-content bg-external">
      <div class="ext-grid">
        <div class="ext-item">
          <div class="ext-icon">🗄️</div>
          <div class="ext-label">Vector DB<br>(PostgreSQL)</div>
        </div>
        <div class="ext-item">
          <div class="ext-icon">🌐</div>
          <div class="ext-label">Internet</div>
        </div>
        <div class="ext-item">
          <div class="ext-icon">🖥️</div>
          <div class="ext-label">Terminal</div>
        </div>
        <div class="ext-item">
          <div class="ext-icon">⚙️</div>
          <div class="ext-label">API Services<br>(Pexels, Amap)</div>
        </div>
        <div class="ext-item">
          <div class="ext-icon">💾</div>
          <div class="ext-label">Memory</div>
        </div>
        <div class="ext-item">
          <div class="ext-icon">📄</div>
          <div class="ext-label">File System</div>
        </div>
        <div class="ext-item">
          <div class="ext-icon">🗃️</div>
          <div class="ext-label">Database</div>
        </div>
      </div>
    </div>
  </div>

</div>
</body>
</html>
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot 3.4, Spring AI 1.0.0 |
| **LLM** | Alibaba DashScope (qwen-plus) |
| **Vector DB** | PostgreSQL + PGVector |
| **Agent Framework** | ReAct (custom implementation) |
| **Streaming** | SSE (Server-Sent Events) |
| **RAG** | Custom Advisor + PGVector semantic search |
| **MCP** | Spring AI MCP Client/Server (SSE mode) |
| **Frontend** | Vue3 + Axios |
| **Containerization** | Docker (pushed to Alibaba Cloud ACR) |
| **API Docs** | Knife4j (Swagger) |

---

## 📊 Performance Metrics

| Metric | Value |
|--------|-------|
| Average API response time | ~3.8s (AI inference included) |
| P95 latency (10 concurrent users) | ~6.1s |
| Error rate under load | 0% |
| Throughput | 41.2 requests/min |
| Agent avg task completion | ~6-8 steps (max 20) |
| Embedding API calls optimized | 30 → 2 per document (batch ingestion) |

> Load tested with Apache JMeter: 10 concurrent users × 5 iterations = 50 requests

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL with PGVector extension
- Node.js 18+

### Backend Setup

**1. Clone the repository**
```bash
git clone https://github.com/LING-6150/ling-ai-agent.git
cd ling-ai-agent
```

**2. Configure environment**

Create `src/main/resources/application-local.yml`:
```yaml
spring:
  ai:
    dashscope:
      api-key: YOUR_DASHSCOPE_API_KEY
  datasource:
    url: jdbc:postgresql://localhost:5432/ling_ai_agent
    username: postgres
    password: postgres
```

**3. Start MCP Server (optional)**
```bash
cd ling-image-search-mcp-server
mvn spring-boot:run
```

**4. Start Backend**
```bash
cd ..
mvn spring-boot:run
```

Backend runs on: `http://localhost:8123/api`

### Frontend Setup

```bash
cd ling-ai-agent-frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:5173`

### Docker Deployment
```bash
# Build image
docker build -t loveapp-backend:1.0 .

# Run container
docker run -p 8123:8123 \
  -e DASHSCOPE_API_KEY=your_key \
  -e DB_URL=jdbc:postgresql://host:5432/ling_ai_agent \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  loveapp-backend:1.0

# Push to Alibaba Cloud ACR
docker tag loveapp-backend:1.0 \
  crpi-6dkl7i3034etqh5m.cn-hangzhou.personal.cr.aliyuncs.com/ling-ai-agent/loveapp-backend:1.0
docker push \
  crpi-6dkl7i3034etqh5m.cn-hangzhou.personal.cr.aliyuncs.com/ling-ai-agent/loveapp-backend:1.0
```

---

## 📁 Project Structure

```
ling-ai-agent/
├── src/main/java/com/ling/lingaiagent/
│   ├── agent/                    # ReAct Agent framework
│   │   ├── BaseAgent.java        # Agent loop + SSE streaming
│   │   ├── ReActAgent.java       # think() + act() abstraction
│   │   ├── ToolCallAgent.java    # Tool calling implementation
│   │   └── LingManus.java        # Final agent with tools
│   ├── app/
│   │   └── LoveApp.java          # RAG chatbot application
│   ├── controller/
│   │   └── AiController.java     # REST + SSE endpoints
│   ├── rag/                      # RAG pipeline components
│   ├── tools/                    # 6 built-in tools
│   └── config/                   # CORS, Exception Handler
├── ling-image-search-mcp-server/ # MCP Server (Pexels API)
└── ling-ai-agent-frontend/       # Vue3 frontend
```

---

## 🔌 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/ai/love_app/chat/sync` | Synchronous chat response |
| GET | `/ai/love_app/chat/sse` | Streaming chat via SSE |
| GET | `/ai/love_app/chat/mcp` | Chat with MCP tools enabled |
| GET | `/ai/manus/chat` | LingManus agent via SSE |

---

## 🎯 Key Implementation Highlights

**1. Custom ReAct Agent Framework**
Built a 4-layer agent architecture from scratch: `BaseAgent → ReActAgent → ToolCallAgent → LingManus`, implementing the ReAct pattern with autonomous tool selection and stuck-loop detection. Agent completes complex tasks in 6-8 steps on average.

**2. RAG Pipeline with Custom Advisors**
Implemented custom `QueryRewriter` and `LoveAppRagCustomAdvisorFactory` for query optimization before vector similarity search, improving retrieval relevance. Optimized batch embedding ingestion from 30 API calls down to 2 per document.

**3. SSE Streaming for Long-running Tasks**
Used `CompletableFuture.runAsync()` + `SseEmitter` to stream agent step results in real-time without blocking web server threads. Achieved 0% error rate under 10 concurrent users.

**4. MCP Protocol Integration**
Developed a standalone MCP Server supporting both Stdio (local) and SSE (remote) transport modes, enabling AI to dynamically call external services (Pexels image search, Amap location) via standardized protocol.

**5. Docker Containerization**
Containerized the full backend with Docker and successfully pushed to Alibaba Cloud Container Registry (ACR) for cloud deployment.

---

## 🗺️ Future Work

- Integrate Meetup/Eventbrite API for real local event recommendations
- Add user progress tracking across sessions
- AWS deployment with ECS
- Frontend progress dashboard with weekly action plans

---

## 📝 Resume Bullets

```
· Built autonomous AI Agent using ReAct framework with Spring AI 1.0.0,
  completing complex tasks in avg 6-8 steps with stuck-loop detection

· Implemented RAG pipeline with PGVector, reducing embedding API calls
  from 30 to 2 per document via batch ingestion (20 docs/batch)

· Developed real-time streaming API with SSE achieving 0% error rate
  under 10 concurrent users, 41 req/min throughput (JMeter tested)

· Built MCP Server with Pexels image search, supporting Stdio and SSE
  transport modes for standardized AI tool integration

· Containerized application with Docker and deployed to
  Alibaba Cloud Container Registry (ACR)
```

---

## 👤 Author

**Ling Duan**
MS Information Systems, Northeastern University
[GitHub](https://github.com/LING-6150)

---

*Built with Spring AI 1.0.0 | February 2026*
