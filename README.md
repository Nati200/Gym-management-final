# Gym Management System

A Java console application that lets you run a gym from your terminal — register members, track check-ins, renew memberships, and generate reports. Built with **Java** and **SQLite**, so all your data is saved in a local database file and survives between sessions.

---

## What It Does

When you launch the app, you get a simple numbered menu and can do seven things:

| # | Action | What happens |
|---|--------|--------------|
| 1 | **Register New Member** | Adds a member to the database with their name, phone, membership tier, and expiry date |
| 2 | **View All Members** | Lists every member with their ID, type, expiry date, and whether they're still active |
| 3 | **Check In Member** | Logs a visit for a member (blocked if their membership has expired) |
| 4 | **Renew Membership** | Extends a member's expiry by one month and shows the fee |
| 5 | **View Check-In History** | Shows all past visits for a specific member, with an option to export to a file |
| 6 | **View Expiring Soon** | Shows anyone whose membership runs out in the next 7 days |
| 7 | **Generate Report** | Writes a full membership summary to a `.txt` file in the `reports/` folder |
| 0 | **Exit** | Closes the app and the database connection cleanly |

---

## Membership Tiers

There are two types of members you can register:

**Basic — 300 ETB/month**
- Access to the gym floor only

**Premium — 600 ETB/month**
- Access to the gym floor, pool, and sauna
- Option to add a personal trainer for an extra 200 ETB/month (800 ETB total)

---

## Project Structure

```
Gym-management-final/
│
├── src/
│   └── GYM/
│       ├── Main.java                          ← Entry point; runs the menu loop
│       ├── model/
│       │   ├── Member.java                    ← Abstract base class for all members
│       │   ├── BasicMember.java               ← Basic tier (300 ETB)
│       │   └── PremiumMember.java             ← Premium tier (600–800 ETB)
│       ├── Database/
│       │   └── DatabaseManager.java           ← All SQL operations (SQLite)
│       ├── exception/
│       │   ├── MemberNotFoundException.java   ← Thrown when an ID doesn't exist
│       │   └── ExpiredMembershipException.java← Thrown on check-in if membership expired
│       └── util/
│           └── ReportGenerator.java           ← Writes .txt reports to disk
│
├── lib/
│   └── sqlite-jdbc-3.43.0.0.jar              ← SQLite driver (bundled)
│
├── gym.db                                     ← SQLite database file (auto-created)
└── gym managment final.iml                    ← IntelliJ project file
```
---

## Generated Files

Every report lands in a `reports/` folder that gets created automatically:

- `reports/membership_report_YYYY-MM-DD.txt` — full member list with a summary (total, basic, premium, expired counts)
- `reports/attendance_member_<ID>.txt` — all check-in timestamps for a specific member (appends on each export, so old data isn't overwritten)

---

## OOP Concepts Used

This project was built as a demonstration of core Java and OOP principles:

- **Inheritance & Abstraction** — `Member` is an abstract class; `BasicMember` and `PremiumMember` extend it and each override `calculateRenewalFee()` and `getMemberType()`
- **Polymorphism** — the app works with a `List<Member>` and calls `.calculateRenewalFee()` without caring whether it's basic or premium
- **Custom Exceptions** — `MemberNotFoundException` and `ExpiredMembershipException` give meaningful error messages instead of generic crashes
- **File I/O** — `ReportGenerator` uses `BufferedWriter` to write and `BufferedReader` to read `.txt` report files
- **JDBC / SQLite** — `DatabaseManager` handles all database operations using prepared statements to prevent SQL injection
- **Static fields** — `Member.sessionRegistrations` tracks how many members were registered in the current session without needing a separate counter variable

---

## Database Schema

Two tables are created automatically when the app first runs:

**`members`**
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER | Auto-incremented primary key |
| name | TEXT | Full name |
| phone | TEXT | Contact number |
| member_type | TEXT | `"BASIC"` or `"PREMIUM"` |
| join_date | TEXT | ISO date (YYYY-MM-DD) |
| expiry_date | TEXT | ISO date (YYYY-MM-DD) |
| has_trainer | INTEGER | `1` if premium member has a personal trainer, else `0` |

**`checkins`**
| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER | Auto-incremented |
| member_id | INTEGER | Foreign key → members.id |
| checkin_time | TEXT | Date + time of the visit |
| note | TEXT | Optional note left at check-in |
