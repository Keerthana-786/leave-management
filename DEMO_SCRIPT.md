# Leave Management App – Hackathon Demo Script 🚀

## Overview
This Leave Management application is built using **Java 17 + Spring Boot 3 + H2** on the backend and **React 18 + TypeScript + Vite + Tailwind CSS** on the frontend.

It features:
- Multi-stage approval chains (**Manager -> HR**)
- Automatic escalation after SLA timeouts (`@Scheduled`)
- Smart conflict & capacity rules engine
- Real-time audit trails and notifications
- Team calendar and analytics dashboard

---

## Pre-requisites & Quick Start

### 1. Backend Server (Port 8080)
```bash
cd backend
mvn spring-boot:run
```
*Base URL:* `http://localhost:8080/api`

### 2. Frontend Dev Server (Port 5173)
```bash
cd frontend
npm run dev
```
*Frontend App:* `http://localhost:5173`

---

## Demo Script Walkthrough (5 Minutes)

### Step 1: One-Click Demo Login (Employee Persona)
1. Open `http://localhost:5173`.
2. Click the **Riya Sen** persona card (or sign in as `riya@co.com` with password `Demo@123`).
3. Point out:
   - **Leave Balances Card Grid:** Live entitled, used, pending, and available counts with pro-rata rules applied.
   - **Stat Summary Cards:** Overview of pending, approved, and total requests.

### Step 2: Apply for Leave with Live Preview & Conflict Check
1. Click **Apply Leave** on the dashboard.
2. Select **Annual Leave**, set dates (e.g., Nov 10 – Nov 12), and type a reason.
3. Click **Preview Leave**:
   - Show automatic calculation of working days (excluding weekends and official holidays).
   - Show remaining balance calculation and any overlap warning flags.
4. Click **Submit Leave**.

### Step 3: View Audit Trail & Real-time Timeline
1. Navigate to **My Leaves** in the left sidebar.
2. Click on the submitted leave request row.
3. Show the **Timeline Modal**:
   - Multi-stage review status (`PENDING_MANAGER`).
   - Detailed activity audit log with exact timestamps and actor names.

### Step 4: Manager Queue & Approval
1. Click **Sign Out** and click the **Manoj Kumar** persona card (`manager1@co.com`).
2. Go to **Manager Queue**.
3. Open the pending request from Riya Sen.
4. Click **Approve** (or add an optional comment).
5. Explain state transition: Status moves from `PENDING_MANAGER` to `PENDING_HR`.

### Step 5: HR Queue & Final Approval
1. Sign out and log in as **Hema HR** (`hr@co.com`).
2. Go to **HR Queue**.
3. Review and click **Approve**.
4. Status transitions to `APPROVED`. Deduct balance occurs only upon final HR approval.

### Step 6: Team Calendar & Analytics
1. Navigate to **Team Calendar** to show color-coded team leaves and official holidays.
2. Navigate to **Analytics** to show the Recharts status breakdown bar chart and leave distribution pie chart.
3. Navigate to **Notifications** to show automatic system notifications.

---

## Test Accounts Reference

| Persona | Email | Role | Password |
|---|---|---|---|
| Riya Sen | `riya@co.com` | Employee | `Demo@123` |
| Manoj Kumar | `manager1@co.com` | Manager | `Demo@123` |
| Hema HR | `hr@co.com` | HR Admin | `Demo@123` |

---
*All changes and source code pushed to [GitHub Repository](https://github.com/Keerthana-786/leave-management).*
