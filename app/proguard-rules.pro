# ProGuard / R8 rules for MedTracker

# --- Hilt ---
-keep,allowobfuscation @interface dagger.hilt.codegen.api.Generated
-keep,allowobfuscation @interface dagger.Module
-keep,allowobfuscation @interface dagger.Provides
-keep @dagger.hilt.android.components.ActivityScoped class *
-keep @dagger.hilt.android.components.FragmentScoped class *
-keep @dagger.hilt.android.components.ViewScoped class *
-keep @dagger.hilt.android.components.ServiceScoped class *
-keep @dagger.hilt.android.components.ActivityComponent interface *
-keep @dagger.hilt.android.components.FragmentComponent interface *
-keep @dagger.hilt.android.components.ViewComponent interface *
-keep @dagger.hilt.android.components.ServiceComponent interface *
-keep @dagger.hilt.android.components.ApplicationComponent interface *
-dontwarn dagger.**

# --- Room ---
-keep class com.example.med_tracker.data.local.** { *; }
-keep class com.example.med_tracker.data.local.dao.** { *; }
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# --- DataStore ---
-keep class com.example.med_tracker.di.** { *; }

# --- Compose ---
-keepclassmembers,allowobfuscation,allowshrinking class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private !static !final <fields>;
    private !static <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    private void writeObjectNoData();
}

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.** { *; }
-keep,allowshrinking class kotlin.coroutines.intrinsics.**

# --- General ---
# Keep generated classes
-keep class **$$*Hilt* { *; }
-dontwarn kotlin.Unit
