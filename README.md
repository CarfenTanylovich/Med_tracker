# 💊 Учет таблеточек

Навайбкодил для себя удобное приложение для учета таблеток. Пейте таблетки, шизы!

[![Latest Release](https://img.shields.io/github/v/release/CarfenTanylovich/Med_tracker?color=blue&label=Релиз)](https://github.com/CarfenTanylovich/Med_tracker/releases/latest)

---
**Требования:** минимальная версия Android — 8.0 (API 26), целевая — Android 15 (API 35)

## 📥 Скачать приложение

Готовый установочный файл APK доступен на странице релизов:

👉 **[Перейти к странице релизов и скачать APK](https://github.com/CarfenTanylovich/Med_tracker/releases)**  
Прямая ссылка на последний релиз: **[Скачать актуальную версию](https://github.com/CarfenTanylovich/Med_tracker/releases/latest)**

# В НАСТРОЙКАХ ВЫДАЙТЕ ВСЕ РАЗРЕШЕНИЯ

Для бумеров:
Скачать APK на телефон → разрешить установку из неизвестных источников (Settings → Security → «Установка неизвестных приложений») → открыть файл → установить.
---

## ✨ Основные возможности

- ⏰ **Точные напоминания:** планирование приемов через `AlarmManager` с поддержкой системных прав на точные будильники.
- 📋 **Управление расписанием:** гибкая настройка схем приема, дозировок и времени суток.
- 📦 **Аптечка и контроль остатков:** автоматический пересчет оставшихся таблеток и предупреждения о необходимости пополнения запаса.
- 📊 **История приемов:** фиксация статусов (принято, пропущено) с отображением в календаре.
- 🎨 **Material You UI:** адаптивный интерфейс на Jetpack Compose с поддержкой системных тем (включая AMOLED Dark).
- 🔒 **Полная автономность:** локальное хранилище без сторонних серверов и трекеров с поддержкой системного бэкапа.

---

## 🛠 Стек технологий

- **Язык:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Архитектура:** Clean Architecture / MVVM
- **База данных:** Room (SQLite)
- **Хранилище настроек:** Jetpack DataStore Preferences
- **DI:** Dagger Hilt
- **Асинхронность:** Coroutines & Flow

---

## ⚙️ Сборка из исходников

Прожэкт требует сборки через Android Studio или Gradle:

1. Установите **Android Studio** (желательно актуальной версии с поддержкой Kotlin и Compose).
- Требуемый JDK 21
- Версии: Kotlin 2.1.10, AGP 8.8.0, compileSdk/targetSdk 35, minSdk 26

Открыть проект в Android Studio и дать Gradle синхронизироваться — или собрать из консоли:
./gradlew assembleDebug
**Сборка debug APK**:
./gradlew installDebug       
**сборка + установка на подключённое устройство/эмулятор**:
./gradlew assembleRelease    
**release-сборка**
- На Windows — gradlew.bat вместо ./gradlew.
**Где искать собранный APK :**(app/build/outputs/apk/...).
Клонируйте репозиторий на компьютер:
git clone [https://github.com/CarfenTanylovich/Med_tracker.git](https://github.com/CarfenTanylovich/Med_tracker.git)
