# KangooFit

KangooFit is a mobile fitness application designed to transform the way people approach exercise by combining real-time feedback, gamification, and a fully connected ecosystem. The app addresses two of the most common problems in fitness: lack of motivation and incorrect exercise execution.

The application was developed using **Android Studio**, ensuring a robust and optimized native development workflow. All visual assets and images used throughout the app were generated with **Nano Banana from Gemini**, providing a consistent and modern visual identity.

Most fitness apps focus on tracking numbers such as steps, calories, or time. KangooFit goes further by actively guiding the user and creating a meaningful reason to stay consistent.

---

## Core Idea

At the center of the experience is a virtual companion. Each user is paired with a kangaroo whose state depends entirely on their physical activity:

* **Positive Reinforcement:** If the user exercises, the kangaroo becomes happy, gains energy, and grows.
* **Accountability:** If the user is inactive, the kangaroo loses energy and becomes sad.

This creates an emotional connection that increases accountability and encourages consistency through a gamified experience. In addition, KangooFit includes a competitive leaderboard system with monthly resets. Every month represents a fresh start, giving all users an equal chance to reach the top and stay motivated over time.

---

## Key Features

* **Real-time exercise form detection** using the phone camera.
* **AI-based validation** that ensures users receive points only for correctly executed movements.
* **Daily reminders** to maintain consistency.
* **Global leaderboard** with monthly reset.
* **Detailed daily and monthly statistics**.
* **Cross-device experience** between phone and smartwatch.

---

## Technologies Used

### Android Application (Central Hub)
The application is developed natively for Android using **Java and XML** within Android Studio. Native development was chosen to ensure full control over hardware resources, which is critical for smooth video processing and efficient battery usage.

### Computer Vision and AI
KangooFit uses **Google MediaPipe** with the Pose Landmarker model to perform real-time pose estimation.
* The system detects 33 key body points.
* Joint angles are calculated using trigonometry to validate exercise execution.
* All processing is done on-device for low latency and enhanced privacy.

### Wear OS Integration
The application integrates with **Wear OS** to extend functionality to smartwatches.
* Collects passive activity data (steps, calories) via built-in sensors.
* Allows remote exercise selection to trigger the phone camera.

### Real-time Synchronization with Firebase
**Firebase** is used for backend infrastructure:
* **Real-Time Database:** Enables instant updates across devices and live leaderboard rankings.
* **Data Sync:** Workout results are uploaded post-session to optimize battery performance.

### Authentication
**Google Sign-In** is implemented to provide secure, fast, and frictionless user authentication.

---

## What Makes It Different

* **Emotional Gamification:** A virtual companion that reacts dynamically to user behavior.
* **Fair Competition:** AI validation prevents cheating, ensuring points are earned through effort.
* **Complete Ecosystem:** Seamless connection between mobile and wearable devices.
* **Active Coaching:** Replaces passive tracking with real-time feedback and guidance.

---

## Future Improvements

KangooFit is built with a modular architecture to allow for rapid expansion:
* Support for more complex workouts such as yoga and pilates.
* A customization system for the virtual companion (unlockable items via points).
* Social features including group challenges and team-based competitions.

---

## Conclusion

KangooFit is more than a fitness tracker. It is an intelligent companion that guides users, keeps them accountable, and makes exercise engaging and interactive. The project demonstrates how real-time AI, mobile development, and gamification can be combined to create a meaningful and scalable fitness experience.
