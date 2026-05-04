# AR Mode Performance Optimization & Feature Improvement Plan

The goal is to reduce the perceived and actual time it takes to open AR mode, load 3D models, and improve the surface detection experience.

## Current Issues
1. **Blank Screen During Download**: `ArScreen` stays blank while `ModelCacheManager` downloads the model.
2. **Sequential Loading**: The model is only downloaded when `ArScreen` is opened.
3. **ARCore Availability Check**: Potential delays in `ArAvailability` check.
4. **Main Thread Blocking**: Ensure all heavy operations are off the main thread.
5. **Surface Detection Sensitivity**: Surface detection can be slow or unresponsive in some environments.

## Proposed Fixes
1. **Add Loading Feedback in ArScreen**:
   - Introduce a `isDownloading` state in `ArScreen`.
   - Show a `CircularProgressIndicator` with a "Downloading Model..." message.
2. **Optimize Model Loading in ArViewer**:
   - Ensure `ArViewer` starts initializing the AR session immediately.
3. **Background Pre-caching**:
   - Start caching the selected model as soon as it's clicked in the Gallery or Home screen, before navigating to `ArScreen`.
4. **Improve ArAvailability**:
   - Reduce unnecessary delays in checking ARCore status.
5. **Enhance Surface Detection**:
   - Use `InstantPlacement` for immediate model placement feedback.
   - Prioritize `Depth API` hit results for better accuracy.
   - Implement a more intuitive visual reticle for surface targeting.

## Steps to Implement
- [ ] Modify `ArScreen.kt` to include a downloading state and UI.
- [ ] Update `ModelViewModel.kt` or the navigation logic to trigger pre-caching.
- [ ] Refactor `ModelCacheManager.kt` to provide better feedback on download progress if possible.
- [ ] Optimize `ArAvailability.kt` check logic.
- [x] Improve Surface Detection in `ArViewer.kt`:
    - [x] Refine hit test logic to include `InstantPlacementPoint`.
    - [x] Use `Raw Depth` if available for improved precision.
    - [x] Enhance visual feedback with a custom reticle node.

## UI/UX Consistency Update
1. **Apply Spatial Theme Header to all main screens**:
    - Update `ChatListScreen` with `SpatialBackground` header and consistent typography.
    - Update `ProfileScreen` with `SpatialBackground` header and improved layout.
    - Update `CategoryCoursesScreen` with `SpatialBackground` header and back button integration.
2. **Steps to Implement**:
    - [x] Modify `ChatListScreen.kt` to remove standard `TopAppBar` and add `SpatialBackground` header.
    - [x] Modify `ProfileScreen.kt` to remove standard `TopAppBar` and add `SpatialBackground` header.
    - [x] Modify `CategoryCoursesScreen.kt` to remove standard `TopAppBar` and add `SpatialBackground` header.

## Personalized Recommendations on Home Screen
1. **Implement Filtering Logic in `HomeViewModel`**:
    - [x] Update `HomeUiState` to include `recommendedCourses` and `recommendedMentors`.
    - [x] Add logic to filter courses based on student\'s `favouriteSubjects`.
    - [x] Add logic to filter mentors based on student\'s `favouriteSubjects` and mentor\'s `specialization`.
2. **Update Home Screen UI**:
    - [x] Add "For you Courses" section that displays filtered courses.
    - [x] Add "For you Mentor" section that displays filtered mentors.
    - [x] Ensure sections only appear for Students and when recommendations are available.
3. **String Resources**:
    - [x] Add `for_you_courses` and `for_you_mentor` to `strings.xml`.

## Promotional Course Carousel
1. **Create `CoursePromotionalCard` with 3 styles**:
    - Style 1: **Holographic Gradient** - Modern look using app theme colors and icon accents.
    - Style 2: **Featured Backdrop** - Full-width course image with an overlaying glassmorphic info card.
    - Style 3: **Dynamic Split** - Side-by-side layout focusing on course metrics like rating and enrollment.
2. **Implement `CourseCarouselAddsCards`**:
    - Integrate the 3 styles into a single reusable component.
    - Ensure it matches the 320dp x 160dp dimensions of the original carousel cards.
3. **Integration**:
    - Add a new carousel version that specifically handles `Courses` data objects.

## Fetch Real Data for Category Courses
1. **Refactor Category Model**: 
    - [ ] Add `categoryKey` to `Category` data class in `Category.kt`.
    - [ ] Update `categories` list with hardcoded keys that match Firebase (e.g., "3d_design", "web_dev").
2. **Update Navigation**:
    - [ ] Pass `categoryKey` to `AppDestination.CategoryCourses`.
3. **Fix Query Logic**:
    - [ ] Ensure `CategoryViewModel` uses the key for `getCoursesByCategory`.
    - [ ] (Optional) Pass the localized name separately or fetch it from resources in the ViewModel for the UI header.

## Following System Implementation
1. **Update User Entities**:
    - [x] Add `followers`, `following`, `followersCount`, and `followingCount` to `User.Mentor`.
    - [x] Add `following` and `followingCount` to `User.Student`.
2. **Repository Layer**:
    - [x] Add `followMentor`, `unfollowMentor`, and `isFollowing` to `AuthRepository`.
    - [x] Implement atomic updates using Firestore `runBatch` to ensure data consistency between student and mentor documents.
3. **UI Integration**:
    - [x] Update `MentorProfileViewModel` to handle follow/unfollow actions and track `isFollowing` state.
    - [x] Update `MentorProfileScreen` to display follower/following counts and provide a toggleable Follow button.
    - [x] Add loading states and error handling for follow operations.

## Home Screen Performance & Freeze Fix
1. **Identify Performance Bottlenecks**:
    - [x] Detected non-lazy horizontal lists (`Row` + `horizontalScroll`) inside a vertical scrollable `Column`.
    - [x] Multiple instances of `SpatialBackground` causing high GPU/CPU usage due to continuous animations.
2. **Optimization Steps**:
    - [x] Replace `Row` with `LazyRow` in `HorizontalCourseList` and `HorizontalMentorList` to implement view recycling.
    - [x] Optimize `SpatialBackground` usage by removing redundant instances (kept global one in `AppNavHost`).
    - [x] Move data filtering logic in `HomeViewModel` to `Dispatchers.Default`.
    - [x] Use `key` in `LazyRow` items to improve recomposition performance.
    - [x] Refactor `CarouselAdds` to use `remember` for its data list to avoid reallocation on recomposition.
