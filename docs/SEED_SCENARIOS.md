# SEED SCENARIOS SPECIFICATION (OCTOBER – NOVEMBER 2026)

> **PURPOSE**: This document specifies the deterministic, hardcoded seed datasets designed for hackathon judges, QA testing, and automated end-to-end demonstrations.

---

## 1. Organization Structure

### Teams
1. **Team 1**: `Core Engineering` (ID: 1) — Size: 7 (1 Manager + 6 Engineers)
2. **Team 2**: `Product & Design` (ID: 2) — Size: 7 (1 Manager + 6 Specialists)
3. **Team 0**: `Executive & People Operations` (ID: 0) — HR Hub

### Users (15 Total)
| ID | Name | Email | Role | Team | Manager | Join Date | Status / Notes |
|:---|:---|:---|:---|:---|:---|:---|:---|
| 1 | **Hema HR** | `hr@co.com` | HR | Team 0 | *None* | 2018-01-01 | HR Administrator |
| 2 | **Manoj Kumar** | `manager@co.com` | MANAGER | Team 1 | Hema HR | 2020-01-01 | Engineering Lead |
| 3 | **Priya Sharma** | `priya.mgr@co.com` | MANAGER | Team 2 | Hema HR | 2019-06-01 | Product Lead (delegates to Vikram) |
| 4 | **Riya Sen** | `riya@co.com` | EMPLOYEE | Team 1 | Manoj Kumar | **2026-07-01** | **Mid-Year Joiner** (Pro-rata: 12 days) |
| 5 | **Arun Patel** | `arun@co.com` | EMPLOYEE | Team 1 | Manoj Kumar | 2024-03-01 | Senior Dev |
| 6 | **Sam Wilson** | `sam@co.com` | EMPLOYEE | Team 1 | Manoj Kumar | 2023-06-01 | Fullstack Dev |
| 7 | **Karan Mehta** | `karan@co.com` | EMPLOYEE | Team 1 | Manoj Kumar | 2021-11-01 | Backend Dev |
| 8 | **Deepak Verma** | `deepak@co.com` | EMPLOYEE | Team 1 | Manoj Kumar | 2022-01-15 | DevOps Engineer |
| 9 | **Neha Gupta** | `neha@co.com` | EMPLOYEE | Team 1 | Manoj Kumar | 2025-05-10 | QA Lead |
| 10 | **Vikram Rao** | `vikram@co.com` | EMPLOYEE | Team 2 | Priya Sharma | 2023-04-01 | Senior Designer (**Active Delegate**) |
| 11 | **Ananya Roy** | `ananya@co.com` | EMPLOYEE | Team 2 | Priya Sharma | 2024-08-15 | UX Researcher |
| 12 | **Rohit Joshi** | `rohit@co.com` | EMPLOYEE | Team 2 | Priya Sharma | 2022-02-01 | Product Owner |
| 13 | **Sneha Nair** | `sneha@co.com` | EMPLOYEE | Team 2 | Priya Sharma | 2025-09-01 | Visual Designer |
| 14 | **Tarun Sethi** | `tarun@co.com` | EMPLOYEE | Team 2 | Priya Sharma | 2024-11-01 | Technical Writer |
| 15 | **Kavita Das** | `kavita@co.com` | EMPLOYEE | Team 2 | Priya Sharma | 2023-10-10 | Content Strategist |

---

## 2. Public Holidays (October – November 2026)
| Date | Day | Holiday Name | Working Day Deduction |
|:---|:---|:---|:---|
| `2026-10-02` | Friday | Gandhi Jayanti | Yes (Excluded from working days) |
| `2026-10-20` | Tuesday | Dussehra | Yes (Excluded from working days) |
| `2026-11-08` | Sunday | Diwali Eve | No (Already weekend) |
| `2026-11-09` | Monday | Diwali | Yes (Excluded from working days) |
| `2026-11-10` | Tuesday | Govardhan Puja | Yes (Excluded from working days) |

---

## 3. Active Delegation Scenario
- **Delegator**: Priya Sharma (Manager, Team 2)
- **Delegate**: Vikram Rao (Employee, Team 2)
- **Date Range**: `2026-10-20` to `2026-10-31`
- **Active**: `true`
- **Behavior**: Requests submitted by Team 2 members during this period assign their manager approval step to Vikram Rao with `delegatedFrom = Priya Sharma`.

---

## 4. Seed Leave Requests & Test Matrix (18 Deterministic Scenarios)

| # | Employee | Type | Date Range | Days | Status | Flagged | Flag / Rejection Reason | Key Demonstrations |
|:--|:---|:---|:---|:---|:---|:---|:---|:---|
| 1 | **Arun Patel** | ANNUAL | 2026-10-05 to 2026-10-07 | 3 | `APPROVED` | No | - | Happy path standard manager approval |
| 2 | **Sam Wilson** | ANNUAL | 2026-10-05 to 2026-10-07 | 3 | `APPROVED` | No | - | Teammate concurrent approval |
| 3 | **Karan Mehta** | ANNUAL | 2026-10-05 to 2026-10-07 | 3 | `PENDING_MANAGER` | **YES** | *"Team capacity warning: 3 of 7 members away (42.9% > 30%)"* | **Conflict detection flag** without auto-rejection |
| 4 | **Deepak Verma**| SICK | 2026-10-12 to 2026-10-14 | 3 | `APPROVED` | No | - | Non-pro-rata sick leave deduction |
| 5 | **Neha Gupta** | CASUAL | 2026-10-21 to 2026-10-23 | 3 | `PENDING_MANAGER` | No | - | Active pending inbox for Manoj Manager |
| 6 | **Riya Sen** | UNPAID | 2026-11-16 to 2026-11-20 | 5 | `PENDING_HR` | No | - | **Manager approved, now awaiting HR** (Unpaid leave requires HR) |
| 7 | **Manoj Kumar** | ANNUAL | 2026-11-23 to 2026-11-25 | 3 | `PENDING_HR` | No | - | **Manager self-leave**: Bypasses manager stage, routes directly to HR |
| 8 | **Ananya Roy** | ANNUAL | 2026-10-14 to 2026-10-16 | 3 | `ESCALATED` | No | *"Manager review SLA exceeded (1 minute timeout)"* | **Escalation engine demo**: Reassigned to HR with SYSTEM audit event |
| 9 | **Rohit Joshi** | ANNUAL | 2026-10-08 to 2026-10-09 | 2 | `REJECTED` | No | *"Critical client release milestone; cannot approve"* | Manager rejection path with feedback comments |
| 10 | **Sneha Nair** | CASUAL | 2026-10-12 to 2026-10-13 | 2 | `CANCELLED` | No | *"Personal plans postponed"* | User cancellation before approval (pending balance restored) |
| 11 | **Tarun Sethi** | ANNUAL | 2026-10-26 to 2026-10-28 | 3 | `PENDING_MANAGER` | No | - | **Delegation in action**: Step assigned to Vikram Rao (`delegatedFrom = Priya`) |
| 12 | **Priya Sharma**| ANNUAL | 2026-10-26 to 2026-10-30 | 5 | `APPROVED` | No | - | Manager leave approved by Hema HR |
| 13 | **Vikram Rao** | ANNUAL | 2026-11-04 to 2026-11-06 | 3 | `APPROVED` | No | - | Approved after escalation by Hema HR |
| 14 | **Sam Wilson** | CASUAL | 2026-11-11 to 2026-11-13 | 3 | `PENDING_MANAGER` | No | - | Multi-request history for employee |
| 15 | **Riya Sen** | ANNUAL | 2026-10-27 to 2026-10-29 | 3 | `APPROVED` | No | - | **Mid-year joiner entitlement**: Pro-rata balance (12 - 3 = 9.0 remaining) |
| 16 | **Karan Mehta** | ANNUAL | 2026-11-02 to 2026-11-04 | 3 | `PENDING_MANAGER` | No | - | Standard future pending request |
| 17 | **Kavita Das** | ANNUAL | *N/A (Preview)* | 15 | `BLOCKED` | - | *"Insufficient balance: Requested 15.0 days, remaining: 4.0"* | **Insufficient balance edge case** |
| 18 | **Rohit Joshi** | CASUAL | 2026-11-09 to 2026-11-10 | 0 | `BLOCKED` | - | *"All requested days are public holidays (Diwali, Govardhan Puja)"* | **Zero working days edge case** |

---

## 5. Balance Ledger Snapshot (Selected Highlights)

### Riya Sen (Mid-Year Joiner - July 1, 2026):
- Annual Quota: 24 days $\times \frac{6 \text{ months}}{12} =$ **12.0 days**
- Approved Leave (Request #15): 3.0 days
- Remaining Annual Balance: **9.0 days**

### Arun Patel:
- Annual Quota: **24.0 days**
- Approved Leave (Request #1): 3.0 days
- Remaining Annual Balance: **21.0 days**

---

## 6. Audit Trail Sample (Request #8: Escalated Leave)
1. `2026-09-20 09:00:00` | Actor: `Ananya Roy` (ID: 11) | Action: `APPLY` | To: `PENDING_MANAGER` | Comment: *"Family vacation"*
2. `2026-09-20 09:01:05` | Actor: `SYSTEM` (null) | Action: `ESCALATE_TIMEOUT` | From: `PENDING_MANAGER` | To: `ESCALATED` | Comment: *"Auto-escalated to HR due to manager inactivity SLA"*
