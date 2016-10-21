# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/qin/android-sdk-linux/tools/proguard/proguard-android.txt
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

#### For GSON
-dontwarn com.google.**
-keep class com.google.**{*;}
-keepclassmembers class * implements java.io.Serializable {
	static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# models
-keep class com.android.network.proxy.bean.**{
   	public *;
}
-keep public class com.trilead.ssh2.compression.*
-keep public class com.trilead.ssh2.crypto.** { *; }