![photo_2025-06-15_20-02-29](https://github.com/user-attachments/assets/5c7f2a7c-96ee-4144-93d3-f7b2cd35843f)
![photo_2025-06-15_20-02-44](https://github.com/user-attachments/assets/045e7667-3383-43b9-a853-085adb9bc8b6)
![photo_2025-06-15_20-02-40](https://github.com/user-attachments/assets/13d1aaee-0231-40d2-8491-de2d8c6a8a93)
![photo_2025-06-15_20-02-35](https://github.com/user-attachments/assets/8d4b91bd-1740-427f-9300-069c8a9f93cc)


Features:
• Live camera preview using Jetpack CameraX PreviewView (back camera)
• Real-time frame processing in C++ with three modes: Canny edges, GaussianBlur + Threshold mask, Invert gray image
• OpenGL ES overlay with ALPHA_8 texture blended over preview, synced with JNI toggles
• Jetpack Compose UI with transparent system bars, Material 3 FABs, and runtime permission handling
• FPS counter rendered using Compose with rolling average
• Two FABs for toggling overlay visibility and cycling effects
• Multi-ABI build support for armeabi-v7a, arm64-v8a, x86, x86_64 with c++_shared
• Modular and minimal structure with separate /cpp, /gl, and /app (Compose) layers

Setup Instructions:
• Clone the repo: git clone https://github.com/your-handle/OpenCVEdgeDetection.git && cd OpenCVEdgeDetection
• Install Android NDK (version r27+): via SDK Manager → NDK
• Install CMake (version ≥ 3.22): via SDK Manager → CMake
• Set $ANDROID_NDK_HOME if building from CLI to point to installed NDK
• Unzip and copy native libs: cp -R OpenCV-android-sdk/sdk/native/libs/* app/src/main/jniLibs/
• This places .so files like libopencv_java4.so for all ABIs in app/src/main/jniLibs/
• No .jar needed since System.loadLibrary("opencv_java4") is used
• Open Android Studio → Sync Gradle → select device → Run
• App will request camera permission on first launch

Explanation of Architecture:
1. CameraX ImageAnalysis provides frames using only the Y-plane (grayscale).
2. Frame is passed to native-lib.cpp via JNI with an effectMode integer.
3. In C++ (OpenCV CPU), one of the following effects is applied:
   o Canny edge detection
   o Gaussian blur + threshold
   o Invert grayscale
4. The processed result is returned as a ByteArray (mask or edges).
5. EdgeRenderer.kt uploads this ByteArray to OpenGL ES 2.0 as an ALPHA_8 texture.
6. Jetpack Compose UI displays:
   o PreviewView for the camera
   o GLSurfaceView overlay for the effect
   o FABs to toggle visibility and switch modes
   o Text showing FPS and current mode
7. composeView.requestRender() triggers a re-render with the updated texture.



         ┌───────────────┐
         │ CameraX       │
         │ ImageAnalysis │
         └──────┬────────┘
                │ Y-plane (gray)
                ▼
      ┌────────────────────┐  JNI call (mode:int)
      │ native-lib.cpp     │  ← effectMode
      │ 1) Canny           │
      │ 2) Blur+Threshold  │  ← OpenCV CPU
      │ 3) Invert gray     │
      └──────┬─────────────┘
             │ ByteArray (mask/edges)
             ▼
      ┌────────────────────┐   ByteBuffer
      │ EdgeRenderer.kt    │   uploads as
      │   OpenGL ES 2.0    │ ─ ALPHA_8 texture
      └──────┬─────────────┘
             │
             ▼  composeView.requestRender()
      ┌──────────────────────────────┐
      │ Jetpack Compose UI           │
      │ • PreviewView                │
      │ • GLSurfaceView overlay      │
      │ • FABs  (show / cycle mode)  │
      │ • FPS / Mode text            │
      └──────────────────────────────┘
