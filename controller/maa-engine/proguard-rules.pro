# MAA RemoteControllerEngineFactory — keep for ServiceLoader
-keep class com.aliothmoon.maameow.controller.maa.engine.MaaRemoteControllerEngineFactory { *; }
-keep class * implements com.aliothmoon.maameow.automation.remote.engine.RemoteControllerEngineFactory { *; }
-keep class com.aliothmoon.maameow.controller.maa.engine.* { *; }

# JNA
-keep class com.sun.jna.** { *; }
-keep class * extends com.sun.jna.Library { *; }
-keep class * implements com.sun.jna.Callback { *; }
