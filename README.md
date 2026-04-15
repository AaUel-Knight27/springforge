<div align="center">
  
# ⚡ SpringForge

**A production-grade CLI accelerator for building full-stack microservices using Spring Boot (Java 25) + Next.js (TypeScript)**

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://java.oracle.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-15-black.svg)](https://nextjs.org/)
[![CLI](https://img.shields.io/badge/CLI-Tool-blue.svg)]()

</div>

SpringForge is a developer productivity "adder" tool designed to minimize boilerplate coding. By applying a convention-over-configuration philosophy, SpringForge allows you to instantly scaffold Spring Boot microservices, synchronize Next.js frontend interfaces, bootstrap JWT authentication, generate PostgreSQL/Flyway migrations, and orchestrate it all locally from a single terminal interface.

---

## 🎯 Features

- **Microservices by Default**: Spin up separated Maven modules representing distinct Spring Boot application domains in seconds.
- **Full-Stack Synchronization**: Generated Spring Data JPA Entities automatically produce matching TypeScript Interfaces, Axios API Hooks, and Next.js service files.
- **Instant Secure Auth**: Inject Spring Security, stateless JWT handling, `User` definitions, and Login/Registration API endpoints with one command.
- **Automated Relational Migrations**: Configure JPA Relationships natively in the CLI, dynamically generating matching SQL `Flyway` migration scripts for PostgreSQL constraints.
- **Admin Dashboards**: Auto-generates generic "Django-style" generic administration CRUD interfaces inside Next.js to monitor databases instantly.
- **Dev Runtime Orchestrator**: Spin up all Spring Boot microservices, connect Dockerized PostgreSQL containers, and boot the frontend simultaneously using standard commands.

---

## 🚀 Installation 

### Prerequisites
- **Java 25** (or newer)
- **Maven** 
- **Node.js** & npm
- **Docker & Docker Compose** (for automated PostgreSQL databases)

### Clone & Install
```bash
git clone https://github.com/AaUel-Knight27/springforge.git
cd springforge

# Install the global 'forge' CLI wrapper safely to your path:
sudo bash scripts/install.sh
```

Restart your terminal, and verify the installation:
```bash
forge --version
```

---

## 🛠️ Quick Start Guide

Transform an empty directory into a complex connected microservice architecture locally.

### 1. Initialize a Project
Creates the workspace, configuration files (`forge.config.yml`), and root structure.
```bash
forge init my-app
cd my-app
```

### 2. Scaffold Microservices
Generate modular Spring Boot sub-projects dynamically on unique application ports.
```bash
forge add service user
forge add service inventory
```

### 3. Build the Domain (Entities)
Inject fully equipped JPA Entities complete with Repositories, Services, Controllers, and DTOs.
```bash
forge add entity User username:string email:string password:string --service user
forge add entity Product title:string price:double stock:int --service inventory
```

### 4. Create Foreign Relationships 
Map entities together. SpringForge will formulate the code annotations alongside SQL schema migrations instantly.
```bash
forge add relation Product User many-to-one --service inventory
```

### 5. Secure with Authentication
Drop in a robust, stateless JWT Security layer over any specific microservice:
```bash
forge add auth --service user
```

### 6. Setup Databases and Sync Frontend
Initialize your Docker PostgreSQL instances and map all Backend java types securely into the Next.js React client hooks.
```bash
forge setup-db --docker
forge sync
```

### 7. Run the Application
Boot all defined microservices, PostgreSQL Docker-Compose networks, and the frontend dynamically! 
```bash
forge run
```

---

## 📁 Repository Structure

SpringForge functions as a Java-based CLI written with **Picocli** and uses **Mustache** templates to perform its logic-less generations. 

```
.
├── src/main/java/dev/springforge
│   ├── cli/        # Picocli command controllers
│   ├── generator/  # Core file-write compilation logic
│   ├── model/      # Local Configuration State management 
│   └── util/       # File mapping and Output coloring extensions
├── src/main/resources/templates
│   ├── backend/    # Spring Boot template files
│   ├── frontend/   # Next.js React templates + Admin module layouts
│   └── migration/  # SQL script files
└── scripts
    └── install.sh  # Unix Global Binary linker
```

## 📜 License
Distributed under the MIT License. See `LICENSE` for more information.
