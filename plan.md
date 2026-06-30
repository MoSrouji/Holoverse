# Mentor Search Fix Plan

This plan outlines the steps taken to fix the mentor search functionality and associated issues in the Holoverse app.

## Steps

### 1. Fix Backend Collection Reference
- [x] **Correct Collection Path**: Updated `SearchRepositoryImpl` to query the `teachers` collection instead of `users`. This was identified as the primary reason why mentor search results were empty, as mentors are stored in their own collection according to `NetworkConstant.COLLECTION_NAME_MENTORS`.
- [x] **Remove Redundant Filters**: Removed the `.whereEqualTo("accountType", "Mentor")` filter from the mentor query in `SearchRepositoryImpl`. Since we are now querying the specific `teachers` collection, this filter is no longer necessary and could lead to issues if data is inconsistent.

### 2. Refactor Data Models and Utils
- [x] **Fix Typo in `Response`**: Renamed `Response.Error.massage` to `message` in `com.example.holoverse.utils.Response`. This was a critical typo causing potential compile errors and confusion throughout the project.
- [ ] **Update Project-wide Usages**: Update all occurrences of `.massage` to `.message` in ViewModels and UI screens to maintain consistency and ensure the project builds.

### 3. Improve Search UI and ViewModel Logic
- [ ] **Optimize SearchViewModel**: 
    - Improve the `onQueryChange` logic to use a more robust `Job` cancellation pattern for debouncing.
    - Ensure that search coroutines are properly managed and cancelled when a new search starts or filters change.
    - Fix the usage of the renamed `message` property in the error state.

### 4. Verification and Testing
- [ ] **Functional Test**: Perform a search for a known mentor name in the search screen.
- [ ] **Category Filter Test**: Verify that selecting a mentor category correctly filters the results.

## Course Enrollment Implementation
- [x] **Define Enrollment Logic in Repository**: Add an `enrollInCourse` method to `AuthRepository` to update the student's `enrolledCourses` and `currentCourses` lists in Firestore.
- [x] **Implement Enrollment in ViewModel**: Add an `enrollInCourse` function to `CourseDetailViewModel` that calls the repository and manages the loading/success/error state for enrollment.
- [x] **Update UI to Handle Enrollment**:
    - Update `CourseDetailScreen` to trigger the enrollment process when "Enroll Now" is clicked.
    - Show a loading indicator or disable the button during the process.
    - Provide feedback (e.g., a Toast or Snackbar) upon successful enrollment.
- [x] **Wire up Navigation**: Update `AppNavHost` to correctly call the ViewModel's enrollment function.
- [x] **Update Enrollment Button State**:
    - [x] Updated `CourseDetailViewModel` to check if a user is already enrolled in a course.
    - [x] Updated `CourseDetailBottomBar` to change the button text to "Enrolled" and disable it if the user is already enrolled.
- [x] **Show Enrolled Courses on Home Screen**:
    - [x] Updated `HomeViewModel` to filter and expose `enrolledCourses` based on the current user's data.
    - [x] Refactored `HomeContentSections` to display the actual enrolled courses in the "Your Courses" tab instead of a placeholder.
