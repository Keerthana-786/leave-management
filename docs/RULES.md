# FROZEN BUSINESS RULES & ARCHITECTURAL INVARIANTS

> **LOCK STATUS: PERMANENT & FROZEN**  
> These rules must never be changed or simplified without explicit user permission. All service implementations must adhere strictly to these invariants.

---

## 1. Working Days Calculation
- **Calendar Basis**: Standard business days are **Monday through Friday**.
- **Weekend Exclusion**: Saturdays and Sundays are excluded from leave duration calculation.
- **Holiday Exclusion**: Any public holiday falling on a Monday–Friday is deducted from the working days count.
- **Edge Case / Invalidation**:
  - An application where every day in the requested date range is a weekend or public holiday (`workingDays == 0`) is **strictly rejected**.
  - Negative day ranges (`fromDate > toDate`) are strictly rejected.

---

## 2. Pro-Rata Entitlement Calculation
- **Formula**:
  $$\text{Entitled Days} = \text{roundToHalf}\left( \frac{\text{Annual Quota} \times \text{Months Remaining}}{12} \right)$$
- **Join Month Invariant**: The employee's join month **counts fully** as an active month regardless of whether the join date is the 1st or the 31st of that month.
- **Months Remaining**: For a joiner in month $M$ ($1 \le M \le 12$), $\text{Months Remaining} = 12 - M + 1$.
- **Rounding Convention**: Round mathematically to the nearest 0.5 day (e.g. 11.666... $\to$ 11.5; 14.2 $\to$ 14.0; 14.25 $\to$ 14.5).
- **Existing Employees**: Employees who joined prior to the current calendar year receive the full annual quota without pro-ration.

---

## 3. Leave Types & Balance Semantics
| Code | Name | Default Quota | Pro-Rata | Requires HR | Paid | Balance Tracked |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `ANNUAL` | Annual Leave | 24 days | **Yes** | No (Manager only, unless escalated/conflict) | Yes | Yes |
| `SICK` | Sick Leave | 12 days | No | No | Yes | Yes |
| `CASUAL` | Casual Leave | 6 days | No | No | Yes | Yes |
| `UNPAID` | Unpaid Leave | 0 (Unlimited) | No | Yes (Always requires HR) | No | **No** (No balance tracking) |

### Balance Lifecycle:
1. **On Leave Application (`POST /api/leaves`)**:
   - Verify: $\text{Entitled} - \text{Used} - \text{Pending} \ge \text{Requested Days}$ (for paid types).
   - If balance is insufficient: **Reject application immediately**.
   - If balance is sufficient: Immediately increment `pending += Requested Days`.
2. **On Final Approval (`APPROVED`)**:
   - `pending -= Requested Days`
   - `used += Requested Days`
3. **On Rejection (`REJECTED`) or Cancellation (`CANCELLED`)**:
   - If cancelled while `PENDING_*` or `ESCALATED`: `pending -= Requested Days`
   - If cancelled after `APPROVED`: `used -= Requested Days`

---

## 4. Conflict Detection Rule
- **Threshold**: **30% (0.30)** of team capacity.
- **Formula**:
  $$\text{Team Impact Ratio} = \frac{\text{Overlapping Active Teammates} + 1}{\text{Team Size}}$$
- Overlapping leaves include any approved or pending leaves for members of the same `team_id` whose date ranges intersect with the requested dates.
- **Action**:
  - If $\text{Team Impact Ratio} > 0.30$: The leave request is marked `flagged = true` with a detailed `flagReason` (e.g. *"Team capacity warning: 2 of 5 members away (40.0% > 30%)"*).
  - **CRITICAL INVARIANT**: **Conflict NEVER causes an automatic rejection**. It serves as an alert flag visible to approving managers and HR.

---

## 5. Approval Chain & Workflow State Machine
- **Standard Hierarchy**:
  $$\text{Employee} \longrightarrow \text{Manager} \longrightarrow \text{HR (if required)}$$
- **Manager Self-Leave Invariant**:
  - If a **Manager** applies for leave, the request **bypasses the manager stage entirely** and routes directly to `PENDING_HR`.
- **HR Self-Leave Invariant**:
  - If an **HR Administrator** applies for leave, it routes to a designated peer HR or auto-approved according to policy.
- **Self-Approval Prohibition**:
  - **Nobody can ever approve or reject their own leave request.** The workflow engine strictly blocks any decision where `actor_id == employee_id`.
- **State Transitions**:
  All status transitions must occur exclusively via `WorkflowService.transition()`:
  - `PENDING_MANAGER` $\to$ `APPROVED` (if no HR required)
  - `PENDING_MANAGER` $\to$ `PENDING_HR` (if `requires_hr == true` or flagged)
  - `PENDING_MANAGER` $\to$ `ESCALATED` (on timeout SLA expiration)
  - `PENDING_MANAGER` $\to$ `REJECTED`
  - `PENDING_MANAGER` $\to$ `CANCELLED`
  - `ESCALATED` $\to$ `APPROVED` (by HR)
  - `ESCALATED` $\to$ `REJECTED` (by HR)
  - `ESCALATED` $\to$ `CANCELLED`
  - `PENDING_HR` $\to$ `APPROVED` (by HR)
  - `PENDING_HR` $\to$ `REJECTED` (by HR)
  - `PENDING_HR` $\to$ `CANCELLED`
  - `APPROVED` $\to$ `CANCELLED`

---

## 6. Escalation Engine
- **Trigger**: An active request in `PENDING_MANAGER` whose `createdAt` (or `lastActionAt`) is older than `leave.escalation-timeout-minutes` (configured to **1 minute** for testing/hackathon demos).
- **Execution**:
  - Evaluated on a scheduled cycle (`@Scheduled(fixedDelayString = "${leave.escalation-check-seconds:30}000")`) or triggered manually via `POST /api/demo/simulate-timeout/{id}`.
  - Automatically transitions status to `ESCALATED`.
  - Reassigns authority to **HR**.
  - Logs a SYSTEM audit event: `action = "ESCALATE_TIMEOUT"`, `actor = null` (SYSTEM).
  - Emits in-app notifications and simulated email to both employee and HR.

---

## 7. Delegation Rules
- A manager can delegate approval authority to a designated active user for a bounded date range `[fromDate, toDate]`.
- When a leave request is submitted while a manager has an active delegation:
  - The `ApprovalStep` is assigned to the `delegate`.
  - The field `delegatedFrom` is explicitly set to the original `manager`.
  - Both the delegator and the delegate have visibility into the step.
- Revoking a delegation immediately restores pending approvals back to the original manager.

---

## 8. Anti-Collision & Single Active Leave
- An employee **cannot apply for an overlapping leave** if they already have an existing leave request in `PENDING_MANAGER`, `ESCALATED`, `PENDING_HR`, or `APPROVED` status that intersects with the requested dates.
- Violations are rejected immediately with a 400 Bad Request error.
