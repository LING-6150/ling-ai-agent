# 💖 Ling AI Agent — Intelligent Relationship & Life Assistant

An AI-powered multi-agent system built with **Spring AI 1.0.0**, combining  
LLM orchestration, RAG knowledge retrieval, tool calling, and PDF generation  
to provide personalized relationship guidance and life assistance.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-brightgreen)](https://spring.io/projects/spring-ai)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue)](https://www.docker.com/)
[![AWS](https://img.shields.io/badge/Deployment-AWS%20EC2-orange)](https://aws.amazon.com/)

---

## 🚀 Key Features

- 💬 **AI Love Coach**
  - Real-time streaming responses via SSE (Server-Sent Events)
  - Personalized advice powered by RAG knowledge retrieval
  - Context-aware dialogue with persistent chat memory

- 🤖 **AI Super Agent (LingManus)**
  - Autonomous ReAct Agent completing tasks in **avg 6-8 steps**
  - 6 built-in tools: web search, scraping, file ops, terminal, download, PDF generation
  - Stuck-loop detection with automatic recovery strategy

- 🧠 **Retrieval-Augmented Generation (RAG)**
  - Custom document ingestion pipeline with batch processing
  - Reduced embedding API calls from **30 → 2 per document** via batch ingestion
  - Vector search powered by **PGVector**

- 🔧 **Multi-LLM Support**
  - GPT-4o (OpenAI)
  - Qwen-Plus (DashScope)
  - DeepSeek

- 🔌 **MCP Protocol Integration**
  - Custom MCP Server with Pexels image search
  - Supports both Stdio and SSE transport modes
  - Amap location-based recommendations

---

## 📊 Performance Metrics

| Metric | Result |
|--------|--------|
| Average API response time | ~3.8s (AI inference included) |
| P95 latency (10 concurrent users) | ~6.1s |
| Error rate under load | **0%** |
| Throughput | **41 req/min** |
| Agent avg task completion | **6-8 steps** (max 20) |
| Embedding API calls optimized | **30 → 2** per document |

> Load tested with Apache JMeter: 10 concurrent users × 5 iterations

---

## 🏗 System Architecture

![Architecture](docs/images/architecture.png)

---

## 🖥 Application Screenshots

### 🏠 Home Page
![Homepage](docs/images/homepage.png)

### 💬 AI Love Coach
![Love Coach 1](docs/images/love-coach-1.png)
![Love Coach 2](docs/images/love-coach-2.png)
![Love Coach 2](docs/images/love-coach-3.png)

### 🤖 AI Super Agent
![Super Agent 1](docs/images/super-agent-1.png)
![Super Agent 2](docs/images/super-agent-2.png)

### 📄 PDF Generation Result
![PDF Result](docs/images/pdf-result.png)

### 📊 LangSmith Tracing
![LangSmith Dashboard](docs/images/LangSmith1.png)
![LangSmith Trace Detail](docs/images/LangSmith2.png)


---

## ⚙ Tech Stack

**Backend**
- Java 21
- Spring Boot 3.4
- Spring AI 1.0.0

**AI & LLM**
- OpenAI API (GPT-4o)
- Alibaba DashScope API (Qwen-Plus)
- DeepSeek API
- Ollama (local models)

**RAG & Storage**
- PostgreSQL + PGVector (vector similarity search)
- Document ETL pipeline with batch embedding

**Communication**
- REST API
- Server-Sent Events (SSE) for real-time streaming

**DevOps**
- Docker (containerized, pushed to Docker Hub & Alibaba Cloud ACR)
- AWS EC2 (t3.micro, Ubuntu 24.04) — production deployment
- Alibaba Cloud ACR — container registry
- Apache JMeter (load testing)

**Testing**
- JUnit 5 (unit tests for core agent logic)
- Spring Boot Test (integration tests)

**API Documentation**
- Knife4j (Swagger UI)

---

## 🔑 Key Implementation Highlights

**1. Custom ReAct Agent Framework**
Designed a 4-layer agent architecture (`BaseAgent → ReActAgent → ToolCallAgent → LingManus`) implementing the ReAct pattern with autonomous tool selection, async execution, and stuck-loop detection. Complex tasks typically complete in 6-8 steps.

**2. RAG Pipeline with Custom Advisors**
Built `LoveAppRagCustomAdvisorFactory` and `QueryRewriter` from scratch instead of using Spring AI defaults, enabling query rewriting and search enhancement before vector similarity search for improved retrieval relevance. Optimized batch embedding ingestion from 30 API calls down to 2 per document.

**3. Non-blocking SSE Streaming Architecture**
Built real-time streaming responses using `CompletableFuture.runAsync()` and `SseEmitter`, ensuring long-running agent tasks do not block web server threads. Maintained 0% error rate under concurrent load tests.

**4. Multi-LLM Capable Architecture**
Decoupled model providers from application logic, enabling seamless switching between GPT-4o, Qwen-Plus, DeepSeek, and local Ollama models via configuration — with zero code changes required.

**5. MCP Protocol Integration**
Developed a standalone MCP Server supporting both Stdio (local) and SSE (remote) transport modes, enabling AI to dynamically call external services (Pexels image search, Amap location) via standardized protocol.

**6. Agent Stability Engineering**
Implemented `isStuck()` loop-detection heuristics and recovery prompts in `BaseAgent` to prevent infinite reasoning cycles and uncontrolled token consumption.

**7. Prototype-scoped Agent Instances**
Applied `@Scope("prototype")` to LingManus so each conversation creates a fresh agent instance, preventing state pollution across concurrent users — a deliberate concurrency design decision.

**8. Cloud-ready Container Deployment**
Containerized the system with Docker and deployed on AWS EC2 and Alibaba Cloud ACR, validating cross-platform builds. Resolved ARM → amd64 architecture mismatch using Docker Buildx for cross-platform compilation.

---

## 🧩 System Modules

### Agent Framework (4-layer architecture)
```
BaseAgent → ReActAgent → ToolCallAgent → LingManus
```
- `BaseAgent`: Agent loop, SSE streaming, stuck-loop detection
- `ReActAgent`: Splits execution into `think()` + `act()`
- `ToolCallAgent`: Tool selection and execution via Spring AI
- `LingManus`: Final agent with all 6 tools injected

### AI Services Layer
- Pre-Advisors: security guard, query rewriting, RAG retrieval
- ChatModel routing engine (multi-LLM support)
- Tool execution framework
- RAG knowledge pipeline with custom advisors

### Tools Implemented (6)
1. Web Search
2. Web Scraping
3. File Operations
4. Resource Download
5. PDF Generation
6. Terminal Operations

### MCP Server
- Standalone Spring Boot service on port 8127
- Pexels API image search tool
- Supports Stdio (local) and SSE (remote) transport modes

---

## ☁️ Cloud Deployment

Backend containerized with Docker and deployed to multiple cloud environments:

| Platform | Type | Details |
|----------|------|---------|
| AWS EC2 | Compute | t3.micro, Ubuntu 24.04, Docker runtime |
| Alibaba Cloud ACR | Registry | Container image storage |
| Docker Hub | Registry | Public image hosting |

**Public API endpoint:** `http://100.53.178.115:8123/api`

> Resolved ARM → amd64 cross-platform build issue using Docker Buildx

---

## 📦 Local Docker Deployment

```bash
# Build image (for amd64/EC2)
docker buildx build --platform linux/amd64 -t isyjijiji/loveapp-backend:amd64 .

# Push to Docker Hub
docker push isyjijiji/loveapp-backend:amd64

# Run container
docker run -d --name ling-ai-backend \
  -p 8123:8123 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DASHSCOPE_API_KEY=your_key \
  isyjijiji/loveapp-backend:amd64
```

---

## 🚀 Quick Start

### Option 1: Docker Compose (One-click startup)

```bash
# 1. Copy environment file
cp .env.example .env
# Edit .env and fill in your API keys

# 2. Start all services (PostgreSQL + Backend)
docker-compose up -d

# 3. Check status
docker-compose ps
```

### Option 2: Local Development

**Prerequisites**
- Java 21, Maven 3.9+
- PostgreSQL with PGVector extension
- Node.js 18+

**Backend**
```bash
git clone https://github.com/LING-6150/ling-ai-agent.git
cd ling-ai-agent
# Configure application-local.yml with your API keys
mvn spring-boot:run
```

**MCP Server (optional)**
```bash
cd ling-image-search-mcp-server
mvn spring-boot:run
```

**Frontend**
```bash
cd ling-ai-agent-frontend
npm install
npm run dev
```

---

## 🎯 Project Goals

This project demonstrates how to build a **production-style AI agent system**, integrating:
- Multi-LLM orchestration with cost-aware routing
- Tool calling pipelines with 6 real-world tools
- RAG architecture with optimized batch ingestion
- Real-time streaming interaction via SSE
- MCP protocol for standardized tool integration
- Containerized deployment with Docker on AWS and Alibaba Cloud

Designed as part of an advanced AI engineering portfolio.

---

## 📝 Resume Bullets

```
· Built autonomous AI Agent using ReAct framework with Spring AI 1.0.0,
  completing complex tasks in avg 6-8 steps with stuck-loop detection

· Implemented RAG pipeline with PGVector, reducing embedding API calls
  from 30 to 2 per document via batch ingestion (20 docs/batch)

· Developed real-time streaming API with SSE achieving 0% error rate
  under 10 concurrent users, 41 req/min throughput (JMeter tested)

· Built MCP Server with Pexels image search supporting Stdio and SSE
  transport modes for standardized AI tool integration

· Deployed on AWS EC2 (t3.micro) and Alibaba Cloud ACR using Docker;
  resolved ARM → amd64 cross-platform build using Docker Buildx

· Wrote unit tests for ReAct Agent core logic (isStuck detection,
  state machine) using JUnit 5, separate from Spring integration tests

· Supports multi-LLM switching via configuration (OpenAI, DashScope,
  DeepSeek, Ollama) with zero code changes required
```

---

## 👩‍💻 Author

**Ling Duan**
MS in Information Systems — Northeastern University
AI Engineering & Intelligent Systems Focus
[GitHub](https://github.com/LING-6150)

---

*Built with Spring AI 1.0.0 | February 2026*
