# HomeWorkout production rules

# Keep Firebase model-related metadata that may be accessed reflectively.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep enum values and names used by Android/framework integrations.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin serialization-style metadata.
-keep class kotlin.Metadata { *; }

# Do not remove Android components.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Firebase, CameraX and ML Kit provide their own consumer rules.

# Filament & SceneView JNI native bindings
-keep class com.google.android.filament.** { *; }
-keep class io.github.sceneview.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# AndroidX Room & WorkManager (WorkDatabase is generated/instantiated reflectively)
-keep class androidx.room.** { *; }
-keep class androidx.work.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class androidx.work.impl.WorkDatabase_Impl {
    <init>(...);
}
-keep class * implements androidx.work.impl.WorkDatabase {
    *;
}

# ML Kit Component Registrars & ComponentDiscovery reflection
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_** { *; }
-keep interface com.google.firebase.components.ComponentRegistrar { *; }
-keep class * implements com.google.firebase.components.ComponentRegistrar {
    public <init>();
    public <init>(...);
    *;
}
-keep class com.google.firebase.components.** { *; }

