# FaceFrame

FaceFrame identifies unique people in videos and creates a shareable collage using on-device AI.


## Implementation

### 1. Analysis Pipeline
The core processing engine consists of a multi-stage pipeline:
*   **Detection**: Leverages Google ML Kit Face Detection in accurate mode to locate faces and stabilize tracking across frames.
*   **Embedding**: Utilizes a FaceNet-based TensorFlow Lite model to generate 192-dimensional L2-normalized identity vectors for each detected face.
*   **Clustering**: Implements a conflict-aware clustering algorithm that groups face tracklets into unique identities using Cosine Similarity with a chosen threshold of 0.62, while enforcing temporal constraints to prevent identity overlap.

### 2. Quality-Based Shot Selection
For each unique identity identified, the system automatically selects the highest-quality representative shot based on a weighted scoring mechanism:
*   **Frontality (35%)**: Evaluates Euler angles to prioritize direct camera engagement.
*   **Sharpness (30%)**: Measures Laplacian variance to ensure clarity and focus.
*   **Engagement (20% Eyes, 15% Smile)**: Prioritizes frames where the subject's eyes are open and an optimistic expression is detected.

### 3. Data Persistence and Management
FaceFrame maintains a persistent history of all processed results using the Room Database.
*   **Hybrid Storage**: Metadata (durations, counts, timestamps) is stored in SQLite, while high-resolution generated assets are managed via internal file storage to optimize database performance.
*   **Dependency Injection**: Koin is used as the DI framework to manage component lifecycles and facilitate testability.

## Technology Stack

*   **Language**: Kotlin (2.0.21)
*   **UI Framework**: Jetpack Compose
*   **Navigation**: Jetpack Navigation Compose
*   **Machine Learning**: Google ML Kit, TensorFlow Lite
*   **Database**: Room Persistence Library
*   **Image Loading**: Coil
*   **Dependency Injection**: Koin

## Technical Architecture

The application is built on modern Android principles, utilizing a Clean Architecture approach paired with the MVVM (Model-View-ViewModel) pattern. This ensures a strict separation of concerns between the data persistence, business logic, and presentation layers.

```text
com.example.collage/
├── data/
│   ├── local/          Room database, DAOs, and entities
│   └── VideoRepository.kt Video-processing orchestration
├── di/                 Koin dependency-injection modules
├── domain/
│   ├── model/          Processing and analysis data models
│   └── processor/      ML Kit detection, TFLite embeddings, tracking, grouping, collage rendering
├── ui/
│   ├── home/           Creation flow components
│   ├── history/        History and detail components
│   ├── components/     Shared UI components
│   └── MainScreen.kt   App shell and navigation
└── util/               Bitmap, similarity, gallery, and sharing helpers
```

The app uses MVVM. CollageViewModel exposes UI state through StateFlow, VideoRepository handles the data persistence layer, and Koin provides the database and processing dependencies.

## Build and Deployment

1.  **Machine Learning Model**: Ensure `facenet.tflite` is present in the `app/src/main/assets/` directory.
2.  **Gradle Configuration**: Run `./gradlew :app:assembleDebug` to build the debug variant.
3.  **Permissions**: The application requires read access to media storage to process video files.
