# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/gaurav/Library/Android/sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

############ For Image cropping library ##############
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }


######## For Razorpay SDK ##########
 -keepclassmembers class * {
     @android.webkit.JavascriptInterface <methods>;
 }

 -keepattributes JavascriptInterface
 -keepattributes *Annotation*

 -dontwarn com.razorpay.**
 -keep class com.razorpay.** {*;}

 -optimizations !method/inlining/*

 -keepclasseswithmembers class * {
   public void onPayment*(...);
 }

 ############ For Picasso ##############
 -dontwarn com.squareup.okhttp.**

############ For OkHttp ###############
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

 #*********************************For BottomNavigationView*******************************************#
 -keepclassmembers class android.support.design.internal.BottomNavigationMenuView {
     boolean mShiftingMode;
 }
 ####################### For IcePick  ###########################
 -dontwarn icepick.**
 -keep class icepick.** { *; }
 -keep class **$$Icepick { *; }
 -keepclasseswithmembernames class * {
     @icepick.* <fields>;
 }
 -keepnames class * { @icepick.State *;}

 ########## Search View ##########
 -keep class android.support.v7.widget.SearchView { *; }

 ######### Facebook SDK #########
 -keep class com.facebook.** {
    *;
 }

#### For Render script ####
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class android.support.v8.renderscript.** { *; }


##### For showcase view
 -keep class com.wooplr.spotlight.** { *; }
 -keep interface com.wooplr.spotlight.**
 -keep enum com.wooplr.spotlight.**

 #################### For Fresco
 # Keep our interfaces so they can be used by other ProGuard rules.
 # See http://sourceforge.net/p/proguard/bugs/466/
 -keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
 -keep,allowobfuscation @interface com.facebook.soloader.DoNotOptimize

 # Do not strip any method/class that is annotated with @DoNotStrip
 -keep @com.facebook.common.internal.DoNotStrip class *
 -keepclassmembers class * {
     @com.facebook.common.internal.DoNotStrip *;
 }

 # Do not strip any method/class that is annotated with @DoNotOptimize
 -keep @com.facebook.soloader.DoNotOptimize class *
 -keepclassmembers class * {
     @com.facebook.soloader.DoNotOptimize *;
 }

 # Keep native methods
 -keepclassmembers class * {
     native <methods>;
 }

 -dontwarn okio.**
 -dontwarn com.squareup.okhttp.**
 -dontwarn okhttp3.**
 -dontwarn javax.annotation.**
 -dontwarn com.android.volley.toolbox.**
 -dontwarn com.facebook.infer.**

 ##### For CRASH LYTICS
 -keep class com.crashlytics.** { *; }
 -dontwarn com.crashlytics.**

 -keepattributes *Annotation*
 -keepattributes SourceFile,LineNumberTable
 -keep public class * extends java.lang.Exception




