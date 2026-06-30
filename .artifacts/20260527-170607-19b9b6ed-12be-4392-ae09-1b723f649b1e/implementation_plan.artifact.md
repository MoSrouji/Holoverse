# Implementation Plan - Adaptive UI for HoloVerseV2

Make the HoloVerseV2 app adaptive to different form factors (phones, tablets, foldables) following the `SKILL.md` guide. This includes making the navigation bar adaptive, adding multi-pane layouts, making lists adaptive, and adding scrolling behavior to app bars.

## Proposed Changes

### 1. Verify Current UI (Previews)

Add multi-device previews to verify the UI on different form factors.

#### [HomeScreen.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/ui/home/HomeScreen.kt)

- Add `FormFactorPreviews` annotation class.
- Apply `@FormFactorPreviews` and `@PreviewTest` (if applicable) to `HomeScreenPreview`.

---

### 2. Adaptive Navigation Bar

Replace the standard `Scaffold` with `NavigationSuiteScaffold` to automatically switch between a bottom navigation bar (on mobile) and a navigation rail (on larger screens).

#### [AppNavigation.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/navigation/AppNavigation.kt)

- Replace `Scaffold` with `NavigationSuiteScaffold`.
- Use `rememberNavigationSuiteScaffoldState()` to manage navigation area visibility.
- Control visibility using `LaunchedEffect(showBottomBar)`.
- Define navigation items dynamically from `topLevelRoutes`.

---

### 3. Multi-pane Layouts (Navigation 3)

Implement a list-detail layout for the Chat feature.

#### [AppNavigation.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/navigation/AppNavigation.kt)

- Add `rememberListDetailSceneStrategy()` and pass it to `NavDisplay`.
- Add `listPane` and `detailPane` metadata to `ChatList` and `ChatScreen` entries.

---

### 4. Adaptive Lists

Convert vertical lists to adaptive grids that change the number of columns based on screen width.

#### [PopularCourses.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/ui/home/coursesList/PopularCourses.kt)

- Replace `LazyColumn` with `LazyVerticalGrid` using `GridCells.Adaptive(300.dp)`.

#### [CategoryCoursesScreen.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/ui/category/CategoryCoursesScreen.kt)

- Replace `LazyColumn` with `LazyVerticalGrid` using `GridCells.Adaptive(300.dp)`.

---

### 5. Scrolling App Bars

Hide the top app bar when scrolling down to provide more content space.

#### [PopularCourses.kt](file:///D:/HoloVerseV2/app/src/main/java/com/example/holoverse/ui/home/coursesList/PopularCourses.kt)

- Use `TopAppBarDefaults.exitUntilCollapsedScrollBehavior()`.
- Apply the scroll behavior to `TopAppBar` and `Scaffold`.

---

## Verification Plan

### Automated Tests
- Run Gradle build to ensure no compilation errors:
  `./gradlew assembleDebug`

### Manual Verification
- Use **Compose Preview** to verify the `HomeScreen` on Phone, Tablet, and Desktop.
- Deploy the app and verify:
  - Navigation bar appears as bottom bar on phone and rail on tablet (simulated via resizing or different emulators).
  - Chat screen shows list and conversation side-by-side on larger screens.
  - Popular courses list shows multiple columns on wider screens.
  - Popular courses top bar hides on scroll down.
