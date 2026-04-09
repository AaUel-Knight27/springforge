# 🚀 SpringForge

> ⚡ *A CLI accelerator that generates full-stack Spring Boot microservices + Next.js applications in minutes.*

---

## 📌 Overview

**SpringForge** is a developer productivity tool that automates the creation of full-stack applications using:

- Spring Boot (Java 25, Maven)
- Microservices architecture
- PostgreSQL + Flyway migrations
- Next.js (App Router)
- shadcn/ui + Tailwind
- Axios-based API integration

It eliminates repetitive setup and boilerplate by generating production-ready code from simple CLI commands.

---

## 🎯 Problem It Solves

Modern full-stack development with Spring Boot + Next.js involves:

- Repetitive microservice setup
- Manual entity/service/controller creation
- Rewriting authentication systems
- Reconnecting frontend APIs repeatedly
- Time-consuming DB + migration setup

👉 SpringForge removes all of that.

---

## ⚡ What It Does

With a single CLI tool, SpringForge can:

### 🏗 Backend (Spring Boot Microservices)
- Generate microservices (user, blog, etc.)
- Create entities, DTOs, repositories, services, controllers
- Auto-generate Flyway migrations
- Set up JWT authentication + role-based security
- Configure PostgreSQL integration

### 🌐 Frontend (Next.js)
- Generate structured Next.js app
- Create Axios API clients automatically
- Generate hooks for data fetching
- Build admin dashboard UI using shadcn/ui

### 🔄 Full-Stack Sync
- Keep backend and frontend aligned
- Auto-update API contracts
- Generate CRUD UI from backend entities

---

## 🧠 Philosophy

> “Stop writing boilerplate. Start building features.”

SpringForge focuses on:
- Speed
- Consistency
- Scalability
- Microservices-first architecture

---

## 🧪 Example Usage

```bash
forge init blog-app --microservices --maven --java25

forge add service user
forge add service blog

forge add auth --service user

forge add entity Blog --service blog

forge add admin

forge setup-db --docker

forge sync

forge run
