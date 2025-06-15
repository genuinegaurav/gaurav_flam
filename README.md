![photo_2025-06-15_20-02-29](https://github.com/user-attachments/assets/5c7f2a7c-96ee-4144-93d3-f7b2cd35843f)
![photo_2025-06-15_20-02-44](https://github.com/user-attachments/assets/045e7667-3383-43b9-a853-085adb9bc8b6)
![photo_2025-06-15_20-02-40](https://github.com/user-attachments/assets/13d1aaee-0231-40d2-8491-de2d8c6a8a93)
![photo_2025-06-15_20-02-35](https://github.com/user-attachments/assets/8d4b91bd-1740-427f-9300-069c8a9f93cc)
![Screenshot 2025-06-15 214417](https://github.com/user-attachments/assets/3ef76a59-111d-4378-8498-4bc6db53f3cf)




Features:
1. Live camera preview using Jetpack CameraX PreviewView (back camera)
2. Real-time frame processing in C++ with three modes:
   a. Canny edges
   b. GaussianBlur + Threshold mask
   c. Invert gray image
3. OpenGL ES overlay with ALPHA_8 texture blended over preview, synced with JNI toggles
4. Jetpack Compose UI with transparent system bars, Material 3 FABs, and runtime permission handling
5. FPS counter rendered using Compose with rolling average
6. Two FABs for toggling overlay visibility and cycling effects
7. Multi-ABI build support for armeabi-v7a, arm64-v8a, x86, x86_64 with c++_shared
8. Modular and minimal structure with separate /cpp, /gl, and /app (Compose) layers

Setup Instructions:
1. Clone the repo:
   git clone https://github.com/your-handle/OpenCVEdgeDetection.git && cd OpenCVEdgeDetection
2. Install Android NDK (version r27+): via SDK Manager → NDK
3. Install CMake (version ≥ 3.22): via SDK Manager → CMake
4. Set $ANDROID_NDK_HOME if building from CLI to point to installed NDK
5. Unzip and copy native libs:
   cp -R OpenCV-android-sdk/sdk/native/libs/* app/src/main/jniLibs/
6. This places .so files like libopencv_java4.so for all ABIs in app/src/main/jniLibs/
7. No .jar needed since System.loadLibrary("opencv_java4") is used
8. Open Android Studio → Sync Gradle → select device → Run
9. App will request camera permission on first launch

Explanation of Architecture:
1. CameraX ImageAnalysis provides frames using only the Y-plane (grayscale).
2. Frame is passed to native-lib.cpp via JNI with an effectMode integer.
3. In C++ (OpenCV CPU), one of the following effects is applied:
   a. Canny edge detection
   b. Gaussian blur + threshold
   c. Invert grayscale
4. The processed result is returned as a ByteArray (mask or edges).
5. EdgeRenderer.kt uploads this ByteArray to OpenGL ES 2.0 as an ALPHA_8 texture.
6. Jetpack Compose UI displays:
   a. PreviewView for the camera
   b. GLSurfaceView overlay for the effect
   c. FABs to toggle visibility and switch modes
   d. Text showing FPS and current mode
7. composeView.requestRender() triggers a re-render with the updated texture.
