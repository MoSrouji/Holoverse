# HoloVerse Authentication Flow

This document provides a visual representation of the authentication and registration processes in
the HoloVerse application.

---

## 1. High-Level Flow (Visual Layout)

```text
       [ APP START ]
             |
      ( HoloIntroScreen )
             |
      [ SignInScreen ] <---------------------------+
             |                                     |
      { Is New User? } --- Yes ---> [ SignUpScreen ]
             |                         |           |
             No                        |     { User Type? }
             |                         |      /        \
      { Credentials? } <---------------+   Mentor    Student
       /      \                               |          |
    Error   Success                  [ Teacher Prof ] [ Student Prof ]
      |        |                              |          |
      +--------|                     [ Teacher Info ] [ Student Pref ]
               |                              |          |
        { Check Role }                ( Finalize & Pay) ( Finalize )
        /      |      \                       |          |
    Admin   Mentor   Student                  +----------+
      |        |        |                           |
[ Admin ]  [   HOME SCREEN   ] <--------------------+
```

---

## 2. Authentication Sequence

This diagram illustrates the interaction between the UI, ViewModels, Repositories, and Firebase
during a standard Sign-In process.

```mermaid
sequenceDiagram
    participant U as User
    participant S as SignInScreen
    participant VM as SignInViewModel
    participant R as AuthRepository
    participant F as Firebase (Auth/Firestore)
    participant P as PreferenceManager

    U->>S: Enters Email & Password
    U->>S: Clicks "Login"
    S->>VM: onEvent(Submit)
    VM->>R: firebaseSignIn(email, password)
    activate R
    R->>F: signInWithEmailAndPassword()
    F-->>R: AuthResult (Success)
    R->>R: getCurrentUser()
    R->>F: Fetch Firestore Doc
    F-->>R: User Data
    R->>P: saveUser(user)
    deactivate R
    R-->>VM: Flow<Response.Success>
    VM-->>S: NavigationEvent.Success
    S->>U: Navigate to HomeScreen
```

---

## 3. Multi-Step Signup Activity

The signup process is role-based and involves multiple steps to gather profile information before
final creation in Firebase.

```mermaid
stateDiagram-v2
    [*] --> HoloIntro
    HoloIntro --> SignIn
    SignIn --> SignUp: Click "Sign Up"

    state SignUp {
        [*] --> BasicInfo: Name, Email, Password
        BasicInfo --> RoleSelection: Select "Student" or "Mentor"
        RoleSelection --> RegistrationState: Update RegistrationViewModel
    }

    SignUp --> TeacherProfile: Role == Mentor
    SignUp --> StudentProfile: Role == Student

    state TeacherProfile {
        [*] --> PersonalInfoT: Photo, Bio, DOB
        PersonalInfoT --> ProfessionalInfoT: Experience, Specialization, Rate
        ProfessionalInfoT --> FinalizeMentor: Click "Finish"
    }

    state StudentProfile {
        [*] --> PersonalInfoS: Photo, DOB, Grade
        PersonalInfoS --> PreferenceInfoS: Interests, Learning Time
        PreferenceInfoS --> FinalizeStudent: Click "Finish"
    }

    state "Firebase Creation" as FirebaseOp {
        FinalizeMentor --> CreateUserM
        FinalizeStudent --> CreateUserS

        state CreateUserM {
            direction TB
            AuthM: Create Firebase Auth User
            StoreM: Save to "mentors" collection
            FeeM: Process $10 Setup Fee
            AuthM --> StoreM
            StoreM --> FeeM
        }

        state CreateUserS {
            direction TB
            AuthS: Create Firebase Auth User
            StoreS: Save to "students" collection
            AuthS --> StoreS
        }
    }

    FirebaseOp --> HomeScreen: Success
    FirebaseOp --> ErrorState: Failure
    ErrorState --> SignUp: Retry
```

---

## 🛠️ Troubleshooting: Missing "Preview" Button

If you don't see the **Split/Preview** icon in the top-right corner of your editor while viewing
this file:

1. **Enable Mermaid Support**:
    - Go to `File` > `Settings` > `Languages & Frameworks` > `Markdown`.
    - Scroll to **Markdown Extensions** and check **Mermaid**.
2. **Fix Missing Preview Pane**:
    - Press `Shift` twice and type **"Choose Boot Java Runtime"**.
    - Select a version that includes **"jcef"** in its name.
    - Restart Android Studio.
