# SR University ExamVault - Question Paper Archive & Contributor Hub

A comprehensive Android application for SR University students to browse, filter, search, preview, and download previous semester and mid-term exam question papers across all engineering branches, complete with an offline document viewer, secure student authentication, peer ratings, contributor leaderboard, and upload notifications.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been confirmed based on your input:

- **Confirmed Delivery Format**: Dedicated Native Android App with a rich in-app offline document viewer, formatted exam renderers, and local storage caching.
- **Confirmed Authentication Model**: Student Roll Number (e.g., `21SR1A0501`) and SRU campus email (`@sru.edu.in`) with branch and semester profile setup.
- **Confirmed Exam & Branch Scope**: Full coverage of Mid-1, Mid-2, Semester End, and Supply examinations across all branches (CSE, ECE, EEE, Mechanical, Civil, AI & ML, Data Science) with regulations (R20, R22, R24).
- **Offline File Storage Strategy**: Papers saved to internal app storage and indexed in Room DB with instant offline access and status indicators.

---

## 1. Overview & Core Concept

- **What It Does**: Provides an organized repository of SR University question papers categorized by branch, semester, regulation, subject, and exam type (Mid-1, Mid-2, Sem). Students can search subjects, filter by branch/semester, download papers for zero-network study sessions, rate paper quality, upload new papers to earn contributor points, and compete on the university leaderboard.
- **Target Audience / Persona**: SR University undergraduate and postgraduate students preparing for mid-term tests and end-semester examinations, as well as student representatives and class toppers archiving academic resources.
- **Key Value**: Eliminates chaotic WhatsApp group drives by creating a single, verified, searchable, and offline-accessible archive with gamified student incentives.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Onboarding & Student Login**:
   - Welcome screen with SR University colors (deep varsity navy and athletic amber gold).
   - Sign in with Roll Number and SRU student email; optional quick preview mode.
   - Profile setup with branch, current semester, and academic year.

2. **Browse & Discovery (Hub)**:
   - Branch filter chips (CSE, ECE, EEE, Mechanical, Civil, AIML).
   - Exam type pills (Mid-1, Mid-2, Semester End, Supply).
   - Real-time search by subject code (e.g., `CS301`), subject name (e.g., `Data Structures`), or year.
   - Paper cards displaying subject name, exam date/year, regulation, contributor badge, average rating stars, and offline status badge.

3. **Paper Reader & Offline Viewer**:
   - Interactive document viewer showing paper metadata (Max Marks, Duration, Code), sections (Part A / Part B), detailed questions, and marks distribution.
   - One-tap "Download for Offline" button with visual progress indicator.
   - Zoom/pan controls, dark mode reading contrast, and "Mark as Studied" flag.

4. **Upload & Contribution Studio**:
   - Multi-step upload form: Select Branch -> Year/Semester -> Exam Type (Mid 1/2, Sem) -> Subject & Code -> Question Text or File Picker.
   - Live preview of contribution before submitting.
   - Instant contributor score increment (+50 points) and confirmation alert.

5. **Ratings & Community Feedback**:
   - 5-star rating system with evaluation tags ("Clear Questions", "Matches Syllabus", "Answer Key Included").
   - Reviews and commentary tab on every paper detail view.

6. **Leaderboard & Contributor Ranks**:
   - Top contributors sorted by karma points and verified uploads.
   - Badges: "Campus Legend", "Senior Scholar", "Top Contributor", "Rising Star".
   - Monthly and all-time leaderboards.

7. **Push Notifications & Preferences**:
   - Push notifications for newly uploaded papers in user's branch via Android NotificationManager.
   - In-app notification center tray.
   - System/Light/Dark theme toggle with dynamic Material 3 color harmonizing.

### Visual Identity & Theme

- **Aesthetic Direction**: Academic Varsity Modern — clean, collegiate, structured, with high readability and crisp typography.
- **Color Palette**:
  - *Primary*: Deep Varsity Navy (`#0D2040`)
  - *Secondary / Accent*: SR Gold & Amber (`#E5A823` / `#FFBF00`)
  - *Surface Light*: Soft Alabaster (`#F7F9FC`)
  - *Surface Dark*: Deep Night Charcoal (`#0F141C`)
  - *Success / Verified*: Emerald (`#10B981`)
- **Typography**: Bold display headings for subject titles, monospace for subject and exam codes (e.g., `20CS301T`), and legible body sans-serif for question readability.
- **Interactive Feedback**: Spring animations on download toggles, smooth animated filter transitions, elevated card hover/press ripples, and snackbar confirmations.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Local Room Database for Offline-First Architecture**
  - *Chosen Approach*: Store paper catalogue, questions, downloaded status, student profile, ratings, and leaderboard rankings locally in Room SQLite database.
  - *Why*: Guarantees instant performance, full offline usability in lecture halls or basements with poor cellular signal, and zero external dependency failures.
  - *Alternatives Considered*: Cloud-only storage (would break during campus network outages).

- **Decision 2: Rich Formatted Question Paper Document Viewer**
  - *Chosen Approach*: A specialized exam paper viewer rendering official SR University question paper layouts (Header, Course Objectives, Part-A Short Questions, Part-B Long Answer Options) alongside offline export capabilities.
  - *Why*: Allows immediate reading on any Android screen without relying on external third-party PDF viewers that may not be installed.

- **Decision 3: Android Notification Channel for Push Alerts**
  - *Chosen Approach*: Native Android `NotificationManager` channel with `POST_NOTIFICATIONS` support that triggers realistic campus alerts whenever a classmate uploads a paper in the student's branch.
  - *Why*: Fulfills the requirement for real push notification delivery directly to the Android notification drawer.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI Layer                     │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   Browse & Search   Paper Viewer      Upload Studio  Leaderboard │
│   Branch & Sem Hub  Offline Reader    Ratings/Review  Profile   │
└────────┬────────┴────────┬────────┴────────┬────────┴─────┬─────┘
         │                 │                 │              │
┌────────▼─────────────────▼─────────────────▼──────────────▼─────┐
│                    ExamVaultViewModel (M3 / MVVM)               │
│   • Active Branch/Sem Filters     • Download & Offline State    │
│   • Auth Session (Roll No/Email)  • Dark Mode & Notification Pref│
└────────┬──────────────────────────────────────────────────┬─────┘
         │                                                  │
┌────────▼────────────────────────────────────────┐ ┌───────▼─────┐
│            ExamPaperRepository                  │ │Notification │
│  • Paper Cache  • Local File Storage            │ │  Service    │
│  • Search Index • Contributor Scoring           │ │(System Tray)│
└────────┬────────────────────────────────────────┘ └─────────────┘
         │
┌────────▼────────────────────────────────────────────────────────┐
│                      Room Database                              │
│  ┌────────────────────┐ ┌────────────────────┐ ┌──────────────┐ │
│  │   PaperEntity      │ │   StudentProfile   │ │ RatingEntity │ │
│  └────────────────────┘ └────────────────────┘ └──────────────┘ │
│  ┌────────────────────┐ ┌────────────────────┐                  │
│  │ LeaderboardEntity  │ │ NotificationEntity │                  │
│  └────────────────────┘ └────────────────────┘                  │
└─────────────────────────────────────────────────────────────────┘
```

### Data Model & State Entities

1. **`PaperEntity`**:
   - `id`: Unique identifier
   - `subjectCode`: e.g., `21CS302`
   - `subjectName`: e.g., `Database Management Systems`
   - `branch`: `CSE`, `ECE`, `EEE`, `MECH`, `CIVIL`, `AIML`
   - `semester`: 1 through 8
   - `examType`: `MID_1`, `MID_2`, `SEM_END`, `SUPPLEMENTARY`
   - `academicYear`: e.g., `2023-2024`
   - `regulation`: `R20`, `R22`, `R24`
   - `maxMarks`: e.g., 60 or 30
   - `duration`: e.g., "3 Hours" or "90 Mins"
   - `content`: Formatted question paper sections and questions
   - `uploaderName` & `uploaderRollNo`: Contributor attribution
   - `averageRating` & `ratingCount`: Peer review aggregate
   - `downloadCount`: Engagement tracking
   - `isDownloaded`: Local offline availability boolean
   - `localFilePath`: Internal storage path for offline document access

2. **`StudentProfile`**:
   - `rollNo`: Primary key
   - `name`: Student full name
   - `email`: `@sru.edu.in` campus email
   - `branch`: Student engineering branch
   - `semester`: Current semester
   - `contributionPoints`: Total points earned from uploads and high ratings
   - `uploadedPapersCount`: Number of papers submitted

3. **`LeaderboardEntry`**:
   - `rank`: Leaderboard placement
   - `studentName`: Display name
   - `rollNo`: Masked roll number (e.g. `21SR1A...`)
   - `branch`: Branch badge
   - `points`: Karma points
   - `papersCount`: Verified uploads
   - `badge`: Title badge

4. **`PaperRating`**:
   - `paperId`, `studentRollNo`, `ratingScore` (1-5), `comment`, `tag`, `timestamp`

---

## 5. Verification & Testing Plan

1. **Compilation Check**: Verify with `compile_applet` ensuring all Room DAOs, entities, and Jetpack Compose screens build with zero errors.
2. **Search & Filter Flow**: Verify filtering by CSE, ECE, EEE, Mechanical, Civil and toggling between Mid-1, Mid-2, Sem-End.
3. **Download & Offline Flow**: Test downloading papers and viewing them in simulated offline mode.
4. **Upload Flow**: Test submitting a new paper, checking the +50 point increment, and triggering push notification.
5. **Dark Mode & Leaderboard**: Verify instant dark/light switching and correct leaderboard rankings.
