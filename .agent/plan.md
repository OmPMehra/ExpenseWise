# Project Plan

ExpenseWise – Personal Finance & Expense Tracker using Java and XML layouts.

## Project Brief

# Project Brief: ExpenseWise

ExpenseWise is a personal finance and expense tracking Android application built to help users seamlessly log daily financial transactions, visualize spending patterns, and maintain budget discipline.

---

## Features

1. **Transaction Logging**: Record income and expense transactions with category, date, amount, and notes.
2. **Visual Financial Analytics**: Display interactive pie and bar charts using MPAndroidChart for monthly spending breakdowns and category-wise analysis.
3. **Budgeting & Notifications**: Define spending thresholds per category and trigger local system notifications when expenses reach or exceed budget limits.
4. **User Preferences**: Save user configurations such as preferred currency and notification settings across app sessions.

---

## High-Level Tech Stack

- **Programming Language**: Java
- **UI Framework**: XML Layouts & Material Design Components (MDC)
- **Database**: Room Persistence Library
- **Key-Value Storage**: SharedPreferences
- **Data Visualization**: MPAndroidChart
- **System Services**: Android Notification APIs (`NotificationManager`)

## Implementation Steps
**Total Duration:** 7h 32m 1s

### Task_1_Database_and_Core_Models: Setup package com.example.expensewise, Room database entities (User, Transaction, Category, Budget, SavingsGoal, RecurringExpense), DAOs, Database class, Repositories, SessionManager (SharedPreferences), and Sample Data Generator.
- **Status:** COMPLETED
- **Updates:** Room entities, DAOs, AppDatabase, Repositories, SessionManager, FinancialUtils, and DemoDataGenerator created in Java. Gradle build passed cleanly.
- **Acceptance Criteria:**
  - Room entities, DAOs, Database, and Repositories created in Java
  - SessionManager and Demo Data Generator implemented
  - build pass

### Task_2_Auth_Splash_Security_Settings: Implement Splash Screen, Login & Registration, PIN Lock security screen, and Profile/Settings Screen (Currency selector, PIN setup, Load Demo Data button, Logout).
- **Status:** COMPLETED
- **Updates:** Implemented SplashActivity, LoginActivity, RegisterActivity, PinLockActivity, Settings/ProfileFragment, and updated AndroidManifest. App builds successfully.
- **Acceptance Criteria:**
  - Splash Screen navigates based on authentication state
  - Login, Registration, PIN Lock, and Settings screens implemented using Java and XML
  - Session management and local auth working as expected
  - build pass
- **Duration:** 12h 44m 40s

### Task_3_Dashboard_Transaction_Budget_Management: Build Main Navigation, Dashboard screen (Greeting, Financial Summary, Budget Progress, Recent Transactions, Quick Action FABs), Add/Edit Transaction screen, Transaction List screen with Search, Filter & Sort, and Budget Management screen with Category Budgets and warnings.
- **Status:** COMPLETED
- **Updates:** Main Navigation, DashboardFragment, AddTransactionActivity, TransactionsFragment, TransactionDetailsActivity, BudgetFragment, and AddBudgetActivity created and tested. Build assembleDebug pass.
- **Acceptance Criteria:**
  - Dashboard displaying summary, budget progress, and recent transactions
  - Full Transaction Management (Add/Edit/Delete, Search, Filter, Sort) working
  - Budget Management with threshold warnings (70%, 90%, 100%) operational
  - build pass

### Task_4_Insights_Charts_Savings_Recurring_Notifications: Implement Charts & Reports using MPAndroidChart (Pie Chart, Bar Chart, Line Chart with Weekly/Monthly/Yearly toggle), Insights & Financial Health score, Savings Goals screen (with deposit flow), Recurring Expenses screen, and Local Android Notifications (Budget alerts, Recurring expense reminders).
- **Status:** COMPLETED
- **Updates:** ReportsFragment with MPAndroidChart (Pie, Bar, Line), Insights & Financial Health card, SavingsGoalsActivity with deposit flow, RecurringExpensesActivity with reminders, and NotificationHelper fully implemented and verified. Build pass.
- **Acceptance Criteria:**
  - Charts (Pie, Bar, Line) and Insights correctly displaying financial metrics using MPAndroidChart
  - Savings Goals and Recurring Expenses fully functional
  - Local notifications scheduled and triggered for warnings and recurring expenses
  - build pass

### Task_5_Run_and_Verify: Build app, perform full end-to-end verification of ExpenseWise functionality, instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Full verification completed by critic_agent. App builds successfully, runs without crashes, and all features are fully functional.
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - Application stability verified and alignment with user requirements confirmed
- **Duration:** N/A

