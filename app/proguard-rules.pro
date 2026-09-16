# Add project specific ProGuard rules here.

# Keep readable stack traces in a minified build even with no crash reporter wired in yet.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*

# FloorPlan and friends round-trip through kotlinx.serialization JSON in DesignRoom.floorPlanJson,
# and Room reads entity fields by name — both need the real field names to survive.
-keep class com.interiordesign3d.data.models.** { *; }

# Navigation3 matches an entry by the destination class itself (entry<Dest.ScrX>), and NavKey state
# is saved/restored across process death — obfuscating these breaks routing and back-stack restore.
-keep class com.interiordesign3d.ui.navigation.** { *; }
