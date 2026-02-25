# Ling AI Agent 🤖

An enterprise-grade AI application platform built with Spring AI 1.0.0, featuring a RAG-powered relationship coaching chatbot and an autonomous AI agent capable of multi-step task execution.

**Live Demo:** [Frontend UI](http://localhost:5173) | **Backend API:** [Swagger Docs](http://localhost:8123/api/swagger-ui.html)

---

## ✨ Features

### AI Love Coach (LoveApp)
- Real-time streaming responses via **SSE (Server-Sent Events)**
- **RAG Pipeline** with PGVector semantic search for domain-specific knowledge retrieval
- Persistent chat memory across sessions using Spring AI `MessageWindowChatMemory`
- PDF report generation summarizing conversation insights
- MCP integration for image search (Pexels API) and location recommendations (Amap)

### AI Super Agent (LingManus)
- Autonomous **ReAct Agent** (Reasoning + Acting loop) with up to 20 steps
- **6 built-in tools**: web search, web scraping, file operations, terminal, resource download, PDF generation
- Stuck-loop detection with automatic recovery strategy
- Real-time step-by-step streaming output via SSE
- `AskHuman` tool for interactive clarification when task is ambiguous

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Vue3 Frontend                     │
│         AI Love Coach  |  AI Super Agent            │
└──────────────────────┬──────────────────────────────┘
                       │ SSE / HTTP
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot Backend (Port 8123)         │
│                                                      │
│  ┌─────────────┐    ┌──────────────────────────┐    │
│  │   LoveApp   │    │       LingManus           │    │
│  │  (RAG Chat) │    │    (ReAct Agent)          │    │
│  └──────┬──────┘    └────────────┬─────────────┘    │
│         │                        │                   │
│  ┌──────▼────────────────────────▼─────────────┐    │
│  │              Spring AI 1.0.0                │    │
│  │   ChatClient | Tool Calling | Advisors      │    │
│  └──────┬──────────────────────────────────────┘    │
│         │                                            │
│  ┌──────▼──────┐  ┌──────────┐  ┌───────────────┐  │
│  │  PGVector   │  │ DashScope│  │  MCP Server   │  │
│  │ (RAG Store) │  │  (LLM)   │  │ (Image Search)│  │
│  └─────────────┘  └──────────┘  └───────────────┘  │
└─────────────────────────────────────────────────────┘
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
| **Containerization** | Docker |
| **API Docs** | Knife4j (Swagger) |

---

## 📊 Performance Metrics

| Metric | Value |
|--------|-------|
| Average API response time | ~3.8s (AI inference included) |
| P95 latency (10 concurrent users) | ~6.1s |
| Error rate under load | 0% |
| Throughput | 41.2 requests/min |
| Agent avg task completion | ~5 steps (max 20) |
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
docker build -t loveapp-backend:1.0 .
docker run -p 8123:8123 \
  -e DASHSCOPE_API_KEY=your_key \
  -e DB_URL=jdbc:postgresql://host:5432/ling_ai_agent \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  loveapp-backend:1.0
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
Built a 4-layer agent architecture from scratch: `BaseAgent → ReActAgent → ToolCallAgent → LingManus`, implementing the ReAct pattern with autonomous tool selection and stuck-loop detection.

**2. RAG Pipeline with Custom Advisors**
Implemented custom `QueryRewriter` and `LoveAppRagCustomAdvisorFactory` for query optimization before vector similarity search, improving retrieval relevance.

**3. SSE Streaming for Long-running Tasks**
Used `CompletableFuture.runAsync()` + `SseEmitter` to stream agent step results in real-time without blocking web server threads.

**4. MCP Protocol Integration**
Developed a standalone MCP Server supporting both Stdio (local) and SSE (remote) transport modes, enabling AI to dynamically call external services via standardized protocol.

---

## 📝 Resume Bullets

```
· Built autonomous AI Agent using ReAct framework with Spring AI 1.0.0, 
  completing tasks in avg 5 steps with stuck-loop detection

· Implemented RAG pipeline with PGVector, reducing embedding API calls 
  from 30 to 2 per document via batch ingestion (20 docs/batch)

· Developed real-time streaming API with SSE achieving 0% error rate 
  under 10 concurrent users, 41 req/min throughput (JMeter tested)

· Built MCP Server with Pexels image search, supporting Stdio and SSE 
  transport modes for standardized AI tool integration
```

---

## 👤 Author

**Ling Duan**  
MS Information Systems, Northeastern University  
[GitHub](https://github.com/LING-6150)

---

*Built with Spring AI 1.0.0 | February 2026*
