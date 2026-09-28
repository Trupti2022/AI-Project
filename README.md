# AI-Project Retail stores


Retail stores (for simplicity, imagine a supermarket) have many associates working on the shop floor. They handle multiple tasks throughout the day. These tasks may be pre-planned, such as cleaning aisles or replenishing shelves, or they may arise in real time and be assigned by the store manager on an ad-hoc basis.

For example:
An associate starts restocking shelves (a planned task)
A BOPIS (Buy Online, Pick Up In Store) order arrives → the associate must collect the items
While picking items, a customer asks for help finding gluten-free products
While assisting the customer, the store manager alerts the associate: "Milk spill in Aisle 3 - clean immediately!"

In essence, associates constantly deal with multiple tasks that have competing priorities.

We want to design a mobile application that helps store associates manage these tasks effectively. Tasks should be visible in the app and prioritised appropriately. Associates should be able to pause an in-progress task to address a higher-priority task and then resume the original task later.

The store manager should be able to intelligently prioritise tasks for each associate and communicate those priorities through the application.

Bring an AI perspective to this use case and propose an application design. Consider the technical capabilities the application should provide. Focus more on engineering the solution than on functional completeness. Explain the high-level architecture, the key components involved, and the AI-related considerations for the solution.


Stack: Spring Boot 4 (Java 21) + React frontend + H2 in-memory DB + WebSocket + Claude AI API

Project name: StoreIQ — smart retail task manager

---

Architecture Overview

┌──────────────────────────────────────────────────────┐
│              React Frontend (port 3000)              │
│  ┌───────────────────┐   ┌──────────────────────┐   │
│  │  Associate View   │   │   Manager Console    │   │
│  │  - Task list      │   │  - Quick scenarios   │   │
│  │  - Start/Pause/   │   │  - Custom task form  │   │
│  │    Resume/Done    │   │  - AI score feedback │   │
│  └────────┬──────────┘   └──────────┬───────────┘   │
│           │ WebSocket                │ REST POST     │
└───────────┼──────────────────────────┼───────────────┘
            │ ws://localhost:8080/ws/tasks  │
            │                          │
┌───────────▼──────────────────────────▼───────────────┐
│           Spring Boot Backend (port 8080)            │
│                                                      │
│  TaskController  ──▶  TaskService  ──▶  TaskRepo     │
│                           │               (H2 DB)    │
│                           ▼                          │
│                  AiPriorityService                   │
│                  (Claude API call)                   │
│                           │                          │
│                  TaskWebSocketHandler                │
│                  (pushes updates live)               │
└──────────────────────────────────────────────────────┘

---

Every File Explained

Task.java — The Domain Model

- Uses the Builder pattern (a classic OOP pattern) — you create tasks fluently: Task.builder().title(...).category(...).build()
- Key fields: priorityScore (1–100, set by AI), aiReasoning (Claude's explanation), status (the state machine)
- JPA @PrePersist / @PreUpdate hooks auto-set timestamps — no manual date management needed

TaskStatus.java — The State Machine

**Work Process**

On Architecture:

▎ "The backend is Spring Boot with three layers: REST API for task CRUD, a WebSocket handler for real-time push to the associate's device, and an AI service that calls Claude to score task urgency. I chose H2 in-memory DB for the PoC to keep it zero-config, but it maps directly to PostgreSQL in production."

On the AI integration:

▎ "The AI doesn't just assign a number — it reasons about context. It sees the associate's current task list alongside the new task. So if an associate already has a BOPIS order in progress and a safety hazard arrives, Claude understands that safety hazards always outrank order fulfillment. The score plus reasoning surfaces directly in the UI so the associate understands why their task order changed."

On the auto-pause logic:

▎ "When a high-priority task arrives, the service layer automatically pauses any lower-priority IN_PROGRESS task before creating the new one. The associate doesn't have to manually juggle tasks — the system handles the interruption, and they can resume later."

On WebSocket vs polling:

▎ "I used raw WebSocket rather than polling because task priority interruptions are time-critical. If a spill happens and the associate's phone only refreshes every 30 seconds, that's a safety gap. With WebSocket, the update is pushed in under 100ms."

On the demo fallback:

▎ "The AI service has a graceful degradation path — if the API key isn't configured or the Claude call fails, it falls back to deterministic keyword scoring. This means the app is always functional even without external dependencies, which matters for reliability."

Setup (30 seconds before they arrive)

Open two browser windows side by side — both on http://localhost:3000:
- Left window → click "👤 Associate" tab
- Right window → click "👔 Manager" tab

This visually shows the two roles simultaneously.

---

The Demo Narrative (walk through this live)

Step 1 — Start a planned task
- On the Manager side: click "📋 Restock Frozen Aisle"
- On the Associate side: task appears in Queue with score 40
- Click ▶ Start — it moves to In Progress
- Say: "Alex starts the planned shelf restock"

Step 2 — BOPIS order arrives
- On Manager: click "📦 New BOPIS Order"
- Watch the Associate side update instantly (no refresh — that's the WebSocket)
- The restock auto-pauses, BOPIS appears at top with score 75
- Say: "A customer order comes in — AI scores it 75, higher than the restock, so the system automatically pauses the shelf work"
- Point to the purple italic text: "🤖 Customer order with time commitment..." — that's the AI reasoning

Step 3 — Safety emergency
- On Manager: click "🚨 Milk Spill Aisle 3"
- Watch everything shift — spill jumps to top with score 95, red priority bar
- Say: "Manager spots a spill. AI scores it 95 — safety always wins. Associate sees this in real time, no phone call needed"

Step 4 — Resume the original task
- After completing the spill, click ✓ Complete
- The BOPIS order is still there — click ▶ Resume
- Then eventually resume the restock
- Say: "Nothing is lost — paused tasks stay in the queue and can be resumed in order"

PENDING → IN_PROGRESS → COMPLETED
              ↕
           PAUSED
This is a finite state machine — a core engineering concept. Tasks can only move between valid states, not jump arbitrarily.

AiPriorityService.java — The AI Brain

This is the most important class for your interview. It does two things:

1. Real mode (when API key is set):
- Calls Claude Haiku API with a structured prompt
- Prompt gives Claude: new task title + description + associate's current task list
- Claude replies with JSON: {"score": 92, "reasoning": "Safety hazard..."}
- Score is clamped to 1–100

2. Demo mode (no API key):
- Falls back to keyword matching (spill/safety → 95, bopis/pickup → 75, etc.)
- Lets you demo without needing a live API call

Why Claude Haiku specifically? Fast and cheap — sub-second latency for a real-time user-facing flow. Using Opus here would be overkill.

TaskService.java — The Business Logic

The smartest class in the backend. When a new task arrives:
1. Gets associate's current task list
2. Asks AI to score the new task
3. Auto-pauses any IN_PROGRESS task if new task scores higher — this is the key behaviour from the requirements ("milk spill interrupts shelf restocking")
4. Saves the new task
5. Immediately pushes updated task list via WebSocket — associate's phone refreshes instantly without polling

TaskWebSocketHandler.java — Real-time Push

- Maintains a ConcurrentHashMap<associateId, WebSocketSession> — thread-safe, one connection per associate
- When anything changes, broadcastToAssociate() pushes the full updated task list as JSON
- Associate connects via ws://localhost:8080/ws/tasks?associateId=associate-1

TaskController.java — REST API

Clean, RESTful endpoints:
GET  /api/tasks/{associateId}     → get all non-completed tasks
POST /api/tasks                   → create task (triggers AI scoring)
PUT  /api/tasks/{id}/start        → PENDING → IN_PROGRESS
PUT  /api/tasks/{id}/pause        → IN_PROGRESS → PAUSED
PUT  /api/tasks/{id}/resume       → PAUSED → IN_PROGRESS
PUT  /api/tasks/{id}/complete     → any → COMPLETED

App.js — React Frontend

Two views in one app:
- Associate View: shows In Progress / Paused / Queue sections, live WebSocket dot (green = connected), AI reasoning shown in purple italic under each task, priority bar (red if >85, amber if >60, purple otherwise)

