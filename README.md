# XPathy Server

### A Local Server with GUI Launcher for XPathy Chrome Extension

XPathy Server is a **Java-based Spring Boot application** with a **modern Swing GUI launcher**. It powers the [XPathy Chrome Extension](https://github.com/Volta-Jebaprashanth/xpathy-chrome-extension), which converts human-friendly **XPathy Java snippets** into standard **XPath expressions** and highlights elements directly in the browser.

---

## 🚀 Overview

This application provides a backend API that interprets and executes XPathy expressions. The GUI launcher allows you to easily **start**, **stop**, and **configure** the local server without using the command line.

Once launched, the server listens on a configurable port (default: **5055**) and can be accessed from both **localhost** and other devices on the same network.


---

## 📦 Download Latest Build

### ➡️ [Download xpathy-server 3.0.0.jar](https://github.com/Volta-Jebaprashanth/xpathy-server/raw/main/releases/xpathy-server%203.0.0/xpathy-server%203.0.0.jar)

If you already have **Java 17 or above**, simply **double-click the JAR file** to open the GUI launcher.

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

| Property | Default     | Description                  |
| -------- | ----------- | ---------------------------- |
| Port     | `5055`      | Default server port          |
| Host     | `localhost` | Accessible via local and LAN |
| Endpoint | `/convert`  | Main conversion endpoint     |

---


## 🧰 Tech Stack

* **Java 17+**
* **Spring Boot 3+**
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