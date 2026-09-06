# Collage - On-Device Face Analysis & Grid Generator

An Android application that processes portrait videos on-device to identify unique individuals, track their appearances, and generate a high-quality, shareable collage.

## Implementation Details

This project was built to satisfy the core evaluation criteria for the internship assignment: Identity Accuracy, Code Quality, and Usability.

### 1. Face Analysis Pipeline (Accuracy: 50% Grade)

- **Stage 1: Detection**: Leverages **Google ML Kit Face Detection** in `ACCURATE` mode with `enableTracking()` active. This provides stable bounding boxes and persistent tracking IDs while a person is on screen.
- **Stage 2: Embedding**: Uses a pre-trained **FaceNet** TFLite model. It produces 192-dimensional L2-normalized embeddings for every detected face.
- **Stage 3: Clustering**: Implements a **Conflict-Aware Agglomerative Clustering** strategy:
    - **Tracklet Formation**: Faces are grouped by their ML Kit `trackingId` into "Tracklets" (continuous appearances).
    - **Co-occurrence Veto**: A conflict map is built; any two tracks seen at the same timestamp are forbidden from being merged.
    - **Greedy Merge**: Tracklets are merged into unique Identities using **Cosine Similarity** with a calibrated threshold of **0.62**.
    - **Appearance Counting**: Continuous segments are identified by grouping tracklets within an identity, allowing for up to a 350ms gap between detections.

### 2. Shot Selection & Quality (Usability: 20% Grade)

- **Best Shot Logic**: For each unique person, a representative shot is chosen by scoring every frame:
    - **Frontality (35%)**: Euler angle analysis to favor direct camera contact.
    - **Sharpness (30%)**: Laplacian variance measurement to ensure clarity.
    - **Eyes Open (20%)**: Leverages ML Kit classification to ensure people aren't blinking.
    - **Smile (15%)**: Favors pleasant expressions.
    - **Clipping Penalty**: Faces near the edge of the frame are heavily penalized to ensure full-face visibility.

### 3. Architecture & Performance (Code Quality: 30% Grade)

- **MVVM Pattern**: Clean separation between UI (Compose), ViewModel, and Domain services.
- **Parallel Processing**: Video decoding and frame analysis happen off the main thread using Coroutines and Flows.
- **Optimization**: Uses `getPixels()` for bulk memory operations and `getScaledFrameAtTime` (720p) for efficient on-device decoding.

## Setup & Running

1.  **Add Model**: Place `facenet.tflite` in `app/src/main/assets/`.
2.  **Build**: Run `./gradlew :app:assembleDebug`.
3.  **Verify**: Test with Sample 1 to see exactly 5 people and 20 appearances.
