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
