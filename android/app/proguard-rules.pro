# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/aven/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,LineNumberTable

# retrofit specific
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

#okhttp
-dontwarn com.squareup.okhttp.*
-keepattributes *Annotation*

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn cokio.**
-keep class okio.**{ *; }
-keep interface okio.**{ *; }
-dontwarn java.nio.**
-dontwarn org.codehaus.**

-keep public class com.jikexueyuan.mobile.address.R$*{
    public static final int *;
}
-dontwarn com.jikexueyuan.mobile.address.**
# kotlin
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-dontwarn org.w3c.dom.events.**
-dontwarn org.jetbrains.kotlin.di.InjectorForRuntimeDescriptorLoader
-keep class kotlinx.android.synthetic.**
-dontwarn kotlinx.android.**

# support
-keep class android.support.design.** { *; }
-dontwarn android.support.**
