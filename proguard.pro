# ===== ОСНОВНЫЕ НАСТРОЙКИ =====
# Не удалять неиспользуемый код (важно для рефлексии и событий)
-dontshrink
# Не оптимизировать (оптимизация может сломать логику)
-dontoptimize

# ===== СОХРАНЕНИЕ АТРИБУТОВ =====
# Без этого Mixin и игра могут не найти нужные классы/методы
-keepattributes *Annotation*, Exceptions, InnerClasses, Signature, Deprecated, LineNumberTable, LocalVariableTable, SourceFile, EnclosingMethod, Synthetic

# ===== ПРАВИЛА ДЛЯ MINECRAFT / FABRIC =====
# Не трогать классы самой игры и Fabric API (они уже скомпилированы)
-keep class net.minecraft.** { *; }
-keep class net.fabricmc.** { *; }

# ===== ПРАВИЛА ДЛЯ ВАШЕГО МОДА =====
# 1. Сохранить главный класс мода (точку входа)
-keep class com.biwer.biwervisuals.BiwerVisuals { *; }

# 2. СОХРАНИТЬ ВСЕ MIXIN-КЛАССЫ ЦЕЛИКОМ (иначе игра упадет)
-keep class com.biwer.biwervisuals.mixin.** { *; }

# 3. Сохранить имена методов/полей, которые используются в @Inject, @Redirect и т.д.
# ProGuard не понимает аннотации Mixin, поэтому мы сохраняем ВСЕ методы в классах, на которые ссылаются Mixin'ы.
# Это "ядерный" вариант, но самый безопасный для старта.
-keepclassmembers class com.biwer.biwervisuals.** {
    <methods>;
    <fields>;
}

# 4. Сохранить классы, которые используются в конфигах (Gson)
-keep class com.biwer.biwervisuals.ModConfig { *; }

# ===== ПЕРЕИМЕНОВАНИЕ =====
# Переименовывать в короткие имена (a, b, c...)
-repackageclasses 'com.biwer.biwervisuals.obf'

-dontwarn net.minecraft.**
-dontwarn net.fabricmc.**
-dontwarn com.mojang.**
-dontwarn org.lwjgl.**
-dontwarn com.google.gson.**
-dontwarn net.fabricmc.fabric.api.client.command.v2.**
-dontwarn java.**
-dontwarn javax.**
-dontwarn jdk.**
-dontwarn sun.**
-dontwarn com.sun.**
-dontwarn org.lwjgl.**
-dontwarn com.google.gson.**
-dontwarn org.slf4j.**