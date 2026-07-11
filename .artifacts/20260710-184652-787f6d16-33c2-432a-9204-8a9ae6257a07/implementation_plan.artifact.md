# Package Reorganization Plan (Feature-based Clean Architecture)

Reorganize the project structure to follow a **Feature-based Clean Architecture**. This involves moving UI components from the global `ui/` package into their respective feature packages and consolidating shared code into a `core` package.

## User Review Required

> [!IMPORTANT]
> This plan involves moving almost every file in the project. It will cause temporary compilation errors that will be resolved as package names and imports are updated.

- **Feature Naming:** I've proposed renaming some packages for clarity (e.g., `chatsystem` to `chat`).
- **Core Package:** I am moving shared UI elements (theme, global widgets) into `core/ui`.

## Proposed Changes

The goal is to move from a layer-separated structure to a feature-separated structure. Each feature will have:
- `data/`: Repositories, DTOs, Data Sources.
- `domain/`: Entities, Use Cases, Repository Interfaces.
- `presentation/`: ViewModels, Screens, Components.

---

### 1. Feature: Auth
Consolidate all authentication logic and UI.

- **Move from:** `ui/commonpart/auth/` & `ui/collectuserdata/`
- **Move to:** `auth/presentation/`
- **Structure:**
    - `auth/presentation/login/`
    - `auth/presentation/signup/`
    - `auth/presentation/profile_setup/` (from `collectuserdata`)

### 2. Feature: Course
Consolidate course management, details, and browsing.

- **Move from:** `ui/coursedetail/`, `ui/category/`, `ui/teacherpart/`
- **Move to:** `course/presentation/`
- **Structure:**
    - `course/presentation/detail/`
    - `course/presentation/category/`
    - `course/presentation/creation/` (from `teacherpart`)

### 3. Feature: Chat
Standardize naming.

- **Rename:** `chatsystem/` -> `chat/`

### 4. Feature: User & Profile
Handle profile viewing (Self, Mentors, Students).

- **Move from:** `ui/commonpart/profile/`, `ui/mentor/`
- **Move to:** `user/presentation/` (New `user` feature)
- **Structure:**
    - `user/presentation/profile/` (Self profile)
    - `user/presentation/mentor_profile/`
    - `user/presentation/edit_profile/`

### 5. Feature: Search
Merge logic and UI.

- **Move from:** `ui/search/`
- **Move to:** `search/presentation/`

### 6. Feature: 3D Gallery
Merge 3D model viewer and gallery.

- **Move from:** `ui/three_D_Part/`
- **Move to:** `threedmodel/presentation/`

### 7. Feature: Home
Dedicated package for the dashboard.

- **Move from:** `ui/home/`
- **Move to:** `home/presentation/`

---

### 8. Core & Infrastructure
Common components used by multiple features.

#### [NEW] `core/`
- `core/domain/`: Common entities (e.g., `AppCategory`, `Response`).
- `core/ui/`:
    - `core/ui/theme/` (from `ui/theme`)
    - `core/ui/spatial/` (from `ui/spatialtheme`)
    - `core/ui/components/` (Common widgets from `ui/commonpart/widget`)
- `core/utils/` (from `utils/`)

---

## Verification Plan

### Manual Verification
- **Static Analysis:** Run `Analyze -> Inspect Code` to ensure no broken imports remain.
- **Build Success:** Execute `./gradlew assembleDebug` to verify the project compiles.
- **Navigation Test:** Run the app and navigate through all main screens (Home, Auth, Chat, Course Detail, Profile) to ensure `AppNavigation` and DI bindings (Hilt) are still working correctly.
