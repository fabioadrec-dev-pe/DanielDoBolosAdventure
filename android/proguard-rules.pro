# ProGuard — primeira build debug/release sem minify.
-keep class com.badlogic.** { *; }
-keep class com.fabioad.ddba.** { *; }

# gdx-controllers (Android)
-keep class com.badlogic.gdx.controllers.android.AndroidControllers { *; }
