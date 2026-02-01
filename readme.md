# NovaTech Knowledge Agent – Intelligent Document Search System

## 📖 Overview

**NovaTech Knowledge Agent** is an AI-powered document search and question-answering system that allows users to query their PDF documents using natural language.  
Built with **Spring Boot, React, PostgreSQL with pgvector, and Ollama**, this system provides fast and private semantic search and RAG (Retrieval-Augmented Generation).

---

## 🚀 Features

- **Semantic Search** – Find documents based on meaning, not just keywords  
- **Natural Language Q&A** – Ask questions in plain English and get answers from your documents  
- **Source Citation** – Every answer includes references to source documents  
- **Local AI Processing** – Uses Ollama for privacy and cost-efficiency  
- **PDF Processing** – Automatically indexes PDF documents from a folder  
- **Vector Database** – Fast similarity search using PostgreSQL + pgvector  

---

## 🏗️ Architecture

```text
┌─────────────────┐     ┌─────────────────┐     ┌────────────────────┐
│  React Frontend │ ──▶ │ Spring Boot API │ ──▶ │ PostgreSQL +        │
│                 │     │                 │     │ pgvector            │
└─────────────────┘     └─────────────────┘     └────────────────────┘
                                │                          │
                                ▼                          ▼
                        ┌─────────────────┐      ┌─────────────────┐
                        │   Ollama AI     │      │   PDF Document  │
                        │  (Local LLM)    │      │     Storage     │
                        └─────────────────┘      └─────────────────┘


novatech-agent/
├── src/main/java/com/novatech/agent/
│   ├── NovatechAgentApplication.java
│   ├── controller/
│   │   └── AgentController.java
│   ├── service/
│   │   ├── SearchService.java
│   │   ├── OllamaService.java
│   │   └── PDFReaderService.java
│   ├── repository/
│   │   └── DocumentRepository.java
│   ├── entity/
│   │   └── DocumentChunk.java
│   └── component/
│       └── DataLoader.java
├── src/main/resources/
│   └── application.properties
├── novatech-kb/
├── pom.xml
└── README.md
```

## 🚀 app.properties
```bash
spring.application.name=NovatechAgent
spring.datasource.url=jdbc:postgresql://localhost:5432/novatech_kb
spring.datasource.username=postgres
spring.datasource.password=root
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

openai.api-key=sk-proj-qhuNcC

app.knowledge-base-path=./novatech-kb
app.chunk-size=1000
app.max-results=5
```

## how to create database and Vector

```bash
CREATE DATABASE novatech_kb;
CREATE EXTENSION vector;
SELECT * FROM pg_extension WHERE extname = 'vector';
```

## how to install vector extension in postgresql
```bash
https://github.com/andreiramani/pgvector_pgsql_windows/releases/tag/0.8.1_18.0.2
Download the zip

Step 1 – Unzip the pgvector zip file

After extracting, you should see these files:
vector.dll
vector.control
vector--0.8.1.sql

🔹 Step 2 – Copy the DLL file
Copy this file:
vector.dll


➡ Paste it into:
C:\Program Files\PostgreSQL\18\lib\

🔹 Step 3 – Copy the extension files
Copy these two files:
vector.control
vector--0.8.1.sql


➡ Paste them into:
C:\Program Files\PostgreSQL\18\share\extension\
```