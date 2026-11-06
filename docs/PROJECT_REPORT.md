# LostLink Project Report

## Problem statement

Campus lost-and-found notices are spread across class groups, notice boards, and word of mouth. LostLink provides a structured place to record and search reports and suggests potentially related reports.

## Users and permissions

| Role | Capabilities |
| --- | --- |
| Standard user | Register, sign in, browse/search/filter reports, create reports, edit/delete their own reports |
| Administrator | Standard user capabilities, plus edit/delete any report, verify reports, and set report status |

The first launch asks the local operator to create one administrator account. This avoids a shared default credential. Additional registrations are standard users.

## Main flow

1. The first operator creates the local administrator account.
2. A user registers and signs in.
3. The user creates a Lost or Found report with item, category, colour, date, campus location, description, and contact details.
4. LostLink saves the report in SQLite and compares it with open reports of the opposite type.
5. The interface presents up to five possible matches with an explainable score; users review the details and decide whether to contact one another.
6. The author can edit or remove their report. An administrator can verify a report, remove an inaccurate report, or change its status to Open, Matched, or Returned.

## Matching score

The engine awards points for:

| Signal | Points |
| --- | ---: |
| Same category | 35 |
| Same non-empty colour | 20 |
| Same non-empty location | 20 |
| Date within one day / seven days / thirty days | 20 / 12 / 5 |
| At least one shared descriptive word longer than three characters | 5 |

Reports scoring at least 25 points are suggested, sorted by score, and limited to five. A score is a heuristic, not proof that two reports concern the same item. It does not automatically change report status.

## Data model

### `users`

`id` is the primary key; `name`, unique case-insensitive `email`, `salt`, `password_hash`, and `is_admin` store the account profile and role. Passwords are hashed with PBKDF2-HMAC-SHA256 and a per-user random salt.

### `reports`

`id` is the primary key; `user_id` references the author; `type` is Lost or Found; item fields include `name`, `category`, `colour`, `item_date`, `location`, `description`, and `contact`; workflow fields include `status` (Open, Matched, Returned), `verified`, and `created_at`.

## Java topics

- Classes, objects, constructors, encapsulation, and package organization
- JavaFX controls, event handlers, and collections
- JDBC with prepared statements and SQLite persistence
- Input validation and exception handling
- String comparison and weighted matching
- Password hashing through Java cryptography APIs

## Safety and scope

This is a single-computer desktop demonstration. Each installation has its own local SQLite file; it has no network service or cross-device synchronization. Report contact details are stored locally without database encryption, so the operating system account must be protected and the app should only be used on a trusted computer. Users should not enter highly sensitive information into descriptions. A campus deployment requires a shared backend, authenticated identity policy, access controls, backups, and privacy/security review.

## Limitations and next steps

- No campus-wide synchronization, email verification, or password recovery.
- No image attachments, notifications, or audit history.
- No backup/restore interface.
- A production campus deployment should use a shared secured service and define report retention and moderation procedures.
