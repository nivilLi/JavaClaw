# JavaClaw

A self-hosted, extensible AI assistant built with Java 25 and Spring Boot 4. Connect your preferred LLM provider, reach it from multiple channels, and let it manage tasks on your machine.

## Features

- **Multi-Channel** — Web chat (WebSocket), Telegram, Discord, WeChat (iLink Bot), and a plugin API for adding more
- **Multi-Provider** — OpenAI, Anthropic (Claude), Google Gemini, Ollama (local), and Alibaba Qwen (DashScope); switchable at runtime via the onboarding wizard
- **Task Management** — Create, schedule (one-off, delayed, or recurring via cron), and track tasks as human-readable Markdown files
- **Extensible Skills** — Drop a `SKILL.md` into `workspace/skills/` and the agent picks it up at runtime
- **Dynamic Tool Discovery** — Lucene-backed tool search so the model finds relevant tools instead of receiving every definition upfront
- **MCP Support** — Model Context Protocol client for connecting external tool servers
- **File & Web Access** — Agent can read/write files, run Brave web searches, and scrape pages with Playwright

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 25, Spring Boot 4.0, Spring AI 2.0 |
| Background Jobs | JobRunr 8 |
| Templates | Pebble + Bulma + htmx |
| Build | Gradle (version catalog) |
| Packaging | Docker via Jib |

## Quick Start

**Prerequisites**: Java 25+, Gradle (wrapper included)

```bash
# Clone
git clone https://github.com/your-org/JavaClaw.git
cd JavaClaw

# Run (workspace/ is created automatically on first boot)
./gradlew :app:bootRun
```

Open http://localhost:8080 — the onboarding wizard will guide you through choosing an LLM provider and (optionally) enabling channels.

## LLM Providers

| Provider | Notes |
|---|---|
| OpenAI | GPT-4o, GPT-4.1, etc. — requires API key |
| Anthropic | Claude Opus/Sonnet/Haiku — supports Claude Code OAuth (zero-config) |
| Google Gemini | Gemini 2.x — requires API key |
| Ollama | Local models (Llama, Mistral, …) — no API key |
| Qwen (通义千问) | DashScope OpenAI-compatible endpoint — requires DashScope API key |

## Channels

| Channel | Setup |
|---|---|
| Web Chat | Built-in, always available at `/` |
| Telegram | Set `agent.channels.telegram.bot-token` in onboarding |
| Discord | Set `agent.channels.discord.bot-token` in onboarding |
| WeChat | Set `agent.channels.wechat.bot-token` via iLink QR scan in onboarding |

## Configuration

Key properties in `app/src/main/resources/application.yaml` (or `application.private.yaml` for secrets):

| Property | Purpose |
|---|---|
| `spring.ai.model.chat` | Active LLM provider (set by onboarding) |
| `agent.workspace` | Workspace root (default: `file:./workspace/`) |
| `agent.onboarding.completed` | Skip wizard on next boot |
| `javaclaw.tools.dynamic-discovery.enabled` | Toggle Lucene tool search (default: `true`) |
| `jobrunr.background-job-server.worker-count` | Concurrent task workers (default: `1`) |
| `jobrunr.dashboard.port` | JobRunr dashboard (default: `8081`) |

## Module Structure

```
JavaClaw/
├── base/           # Core: agent, tasks, tools, channels, config
├── app/            # Spring Boot entry point, web chat, onboarding UI
├── providers/
│   ├── anthropic/  # Claude + Claude Code OAuth
│   ├── openai/     # GPT
│   ├── ollama/     # Local LLMs
│   ├── google/     # Gemini
│   └── qwen/       # Alibaba DashScope (Qwen)
└── plugins/
    ├── telegram/   # Telegram long-poll
    ├── discord/    # Discord Gateway (JDA)
    ├── wechat/     # WeChat iLink Bot long-poll
    ├── brave/      # Brave web search tool
    └── playwright/ # Browser automation tool
```

## Docker

```bash
./gradlew app:jibDockerBuild
docker run -p 8080:8080 -v $(pwd)/workspace:/workspace javaclaw
```

## Adding a Plugin

1. Create a Gradle module under `plugins/` or `providers/`
2. Declare any tools as `AutoDiscoveredTool<?>` Spring beans — `JavaClawConfiguration` picks them up automatically
3. Register the module in `settings.gradle` and add it as a dependency in `app/build.gradle`

## Workspace Layout

| Path | Purpose |
|---|---|
| `workspace/AGENT.md` | System prompt (editable) |
| `workspace/AGENT.private.md` | Private override — loaded instead of `AGENT.md` when present |
| `workspace/INFO.md` | Environment context injected into every prompt |
| `workspace/skills/` | Runtime skills — drop a `SKILL.md` here |
| `workspace/tasks/` | Task files (date-bucketed Markdown) |
| `workspace/conversations/` | Per-channel chat history |
| `workspace/context/` | Agent memory and long-term context |

## License

MIT
