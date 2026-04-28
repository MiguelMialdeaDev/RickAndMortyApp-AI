# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.miguelangel.rickandmortyai.**$$serializer { *; }
-keepclassmembers class com.miguelangel.rickandmortyai.** {
    *** Companion;
}
-keepclasseswithmembers class com.miguelangel.rickandmortyai.** {
    kotlinx.serialization.KSerializer serializer(...);
}
