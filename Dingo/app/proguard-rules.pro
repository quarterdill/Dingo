# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# This rule tells ProGuard to keep the constructor that takes two double arguments in the LatLng class.
#-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
#
#-keep,allowobfuscation class * extends com.google.protobuf.GeneratedMessageLite { *; }
#
#-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }

# This rule tells ProGuard to keep the constructor that takes two double arguments in the LatLng class.
-keepclassmembers class com.google.android.gms.maps.model.LatLng {
    <init>(double, double);
}
-keep class com.google.firebase.firestore.GeoPoint {
    <init>(double, double);
}




# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile