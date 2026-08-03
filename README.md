# XPathy Server

### A Local Server with GUI Launcher for XPathy Chrome Extension

[![Build & Release](https://github.com/Volta-Jebaprashanth/xpathy-server/actions/workflows/build-release.yml/badge.svg)](https://github.com/Volta-Jebaprashanth/xpathy-server/actions/workflows/build-release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Volta-Jebaprashanth/xpathy-server)](https://github.com/Volta-Jebaprashanth/xpathy-server/releases/latest)

XPathy Server is a **Java-based Spring Boot application** with a **modern Swing GUI launcher**. It powers the [XPathy Chrome Extension](https://github.com/Volta-Jebaprashanth/xpathy-chrome-extension), which converts human-friendly **XPathy Java snippets** into standard **XPath expressions** and highlights elements directly in the browser.

---

## 🚀 Overview

This application provides a backend API that interprets and executes XPathy expressions. The GUI launcher allows you to easily **start**, **stop**, and **configure** the local server without using the command line.

Once launched, the server listens on a configurable port (default: **5055**) and can be accessed from both **localhost** and other devices on the same network — over plain HTTP for browsers/scripts, and over **MCP (Model Context Protocol)** for AI agents.

---

## 📦 Download Latest Build

### ➡️ [Download xpathy-server.jar (latest release)](https://github.com/Volta-Jebaprashanth/xpathy-server/releases/latest/download/xpathy-server.jar)

See the [Releases page](https://github.com/Volta-Jebaprashanth/xpathy-server/releases/latest) for release notes and past versions.

If you already have **Java 21 or above**, simply **double-click the JAR file** to open the GUI launcher.

No terminal or command line needed — it starts the launcher directly.



| Step 1                                            | Step 2                                                                                     |
|---------------------------------------------------| ------------------------------------------------------------------------------------------ |
| ![Step 1](https://github.com/Volta-Jebaprashanth/xpathy-server/raw/main/screenshots/1.png) | ![Step 2](https://github.com/Volta-Jebaprashanth/xpathy-server/raw/main/screenshots/2.png) |

| Step 3                                                                                     |
| ------------------------------------------------------------------------------------------ |
| ![Step 3](https://github.com/Volta-Jebaprashanth/xpathy-server/raw/main/screenshots/3.png) |

## 📡 API Endpoints

* Make sure firewall allows local connections on your selected port.


### `GET /execute?expression={xpathy-snippet}`

Evaluate and execute a snippet directly via browser or REST client.

how to access the server

```text
Local:   http://localhost:5055/execute?expression={xpathy-snippet}
Network: http://192.168.x.x:5055/execute?expression={xpathy-snippet}
```

Sample Usage:   http://localhost:5055/execute?expression=id.contains("id-1");

---

## 🤖 MCP (Model Context Protocol) Integration

XPathy Server also exposes an **MCP server** on the same port, using the **Streamable HTTP** transport. This lets AI agents (Claude Code, Claude Desktop, and any other MCP-compatible client) call XPathy directly as a tool instead of hitting the raw REST endpoint.

* **Endpoint:** `http://localhost:5055/mcp` (JSON-RPC 2.0 over HTTP POST, per the [MCP spec](https://modelcontextprotocol.io))
* **Transport:** `streamable-http` (also referred to as `http` by some clients) — not `stdio`
* **Tool exposed:** `execute_xpathy` — takes one string argument, `expression`, containing the XPathy Java snippet, and returns the resulting XPath expression (or `Error: ...` on failure)

The server must be running (via the GUI launcher or `mvn spring-boot:run`) before an agent connects — there is no separate process to start.

### Claude Code

```bash
claude mcp add --transport http xpathy-server http://localhost:5055/mcp
```

Or add it to `.mcp.json` in your project:

```json
{
  "mcpServers": {
    "xpathy-server": {
      "type": "http",
      "url": "http://localhost:5055/mcp"
    }
  }
}
```

### Claude Desktop / other JSON-config clients

Claude Desktop's config only supports launching local `stdio` processes directly, so to reach an HTTP server like this one, point it at a thin stdio↔HTTP bridge such as [`mcp-remote`](https://www.npmjs.com/package/mcp-remote):

```json
{
  "mcpServers": {
    "xpathy-server": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:5055/mcp"]
    }
  }
}
```

### Any other MCP client

Any client that supports the Streamable HTTP (or SSE) transport can connect using the URL above — no API key or auth header is required for local/LAN use. If the client asks for a transport type, choose `http` / `streamable-http` (not `stdio`, `sse`-only, or `websocket`).

### Changing the MCP endpoint or disabling it

Both are controlled from `src/main/resources/application.properties`:

```properties
# Disable the MCP server entirely
spring.ai.mcp.server.enabled=false

# Change the MCP path (default: /mcp)
spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp
```

---

## ⚙️ Installation (Optional Manual Setup)

If you prefer to build from source:

### 1. Clone the repository

```bash
git clone https://github.com/Volta-Jebaprashanth/xpathy-server.git
cd xpathy-server
```

### 2. Build the project

If using **Maven**:

```bash
mvn clean install
```

If using **Gradle**:

```bash
gradle build
```

### 3. Run the application manually

You can run it via the GUI launcher or from the command line.

**Option A: Launch GUI**

```bash
java -jar target/xpathy-server.jar
```

Then click **Start Server** in the GUI.

**Option B: Run directly with Spring Boot**

```bash
mvn spring-boot:run
```

---

## 🌍 Default Configuration

| Property      | Default     | Description                                    |
| ------------- | ----------- | ----------------------------------------------- |
| Port          | `5055`      | Default server port                            |
| Host          | `localhost` | Accessible via local and LAN                   |
| REST Endpoint | `/execute`  | Main conversion/execution endpoint (HTTP GET)  |
| MCP Endpoint  | `/mcp`      | MCP Streamable HTTP endpoint for AI agents      |

---


## 🧰 Tech Stack

* **Java 21+**
* **Spring Boot 4+**
* **Spring AI 2.0 (MCP server, WebMVC/Streamable HTTP transport)**
* **Swing GUI (Java Desktop)**
* **Maven / Gradle**

---

## 🔒 Security Notes

* Make sure firewall allows local connections on your selected port.
* Avoid running the server on public networks.
* Default access is limited to LAN unless explicitly opened.

---

## 🧑‍💻 Developer Notes

### Run in Development Mode

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=5055"
```

### Build Executable JAR

```bash
mvn clean package
```

Output will be available under:

```
target/xpathy-server.jar
```

### Logging

Logs are stored in the `logs/` directory (ignored in `.gitignore`).

### Continuous Integration & Releases

Every push to `main` or a `release-*` branch (and every pull request) is built automatically via [GitHub Actions](.github/workflows/build-release.yml) using JDK 21:

```bash
mvn -B clean package
```

After a successful build on `main`, the workflow checks the version in `pom.xml`. If a GitHub Release for that version (tag `v<version>`) doesn't exist yet, it publishes a new release with `target/xpathy-server.jar` attached. If the version was already released, the workflow just builds — no duplicate release is created, so routine commits (docs, fixes) don't spam new releases. To ship a new version, bump `<version>` in `pom.xml` and merge to `main`.

---



## 📄 License

This project is licensed under the **MIT License** — free to use, modify, and distribute with attribution.

----------
## 👨‍💻 Author

Created by **Volta Jebaprashanth**  
📧 [voltajeba@gmail.com](mailto:voltajeba@gmail.com)  
📞 +94 77 463 7185  
🔗 [LinkedIn](https://www.linkedin.com/in/voltajeba)

---
*Happy Xpathying!* 🧙‍♂️