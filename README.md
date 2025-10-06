# LifeLinePro - Personal Wellness Tracker

**LifeLinePro** is a comprehensive Android application designed to help users manage their daily health routines and promote personal wellness. Built with Kotlin and Android Studio, this app demonstrates modern Android development practices and effective use of various Android components.

## 📱 Features

### Core Features (Required)

1. **Daily Habit Tracker**
   - Add, edit, and delete daily wellness habits
   - Track completion progress for each habit
   - Visual progress bars showing daily completion percentage
   - Automatic daily reset of habit counts
   - Overall completion rate card

2. **Mood Journal with Emoji Selector**
   - Log mood entries with 5 different mood types (Very Happy, Happy, Neutral, Sad, Very Sad)
   - Add optional notes to mood entries
   - View mood history with timestamps
   - Beautiful emoji-based mood selection interface

3. **Hydration Reminder**
   - Track daily water intake with customizable goals
   - Add water intake in quick increments (100ml, 250ml, 500ml) or custom amounts
   - Visual progress indicator showing water consumption
   - WorkManager-based reminder system
   - Customizable reminder intervals (15-180 minutes)
   - Persistent reminders even after device reboot

### Advanced Features

4. **Home Screen Widget**
   - Displays today's habit completion percentage
   - Updates automatically every 30 minutes
   - One-tap access to open the app
   - Beautiful Material Design styling

5. **Mood Trend Visualization**
   - Interactive line chart using MPAndroidChart library
   - Visualizes mood trends over the last 7 days
   - Smooth cubic bezier curve interpolation
   - Color-coded mood levels

6. **Data Persistence with SharedPreferences**
   - All data stored locally using SharedPreferences
   - Efficient JSON serialization with Gson
   - No database required
   - Export functionality to share data

## 🏗️ Technical Architecture

### Architecture Components
- **Fragments**: Separate screens for Habits, Mood, Hydration, and Settings
- **Activities**: Single MainActivity hosting all fragments
- **Data Models**: Habit, MoodEntry, HydrationIntake, UserSettings
- **SharedPreferences Manager**: Singleton pattern for data persistence
- **Adapters**: RecyclerView adapters for displaying lists

### Key Technologies
- **Language**: Kotlin
- **UI**: Material Design 3, ViewBinding
- **Navigation**: Bottom Navigation View
- **Background Work**: WorkManager for periodic reminders
- **Charts**: MPAndroidChart for mood trend visualization
- **Notifications**: NotificationCompat with channels
- **Widgets**: AppWidgetProvider for home screen widget
- **Permissions**: Runtime permissions for notifications

### Data Persistence
- **Storage**: SharedPreferences (no database)
- **Serialization**: Gson for converting data models to/from JSON
- **Features**:
  - Automatic daily habit reset
  - Mood entry history
  - Hydration intake tracking
  - User settings persistence

## 🎨 UI/UX Design

### Design Principles
- **Clean & Intuitive**: Easy-to-navigate interface with clear visual hierarchy
- **Modern Material Design**: Material 3 components with custom theming
- **Responsive**: Adaptive layouts for phones, tablets, portrait & landscape
- **Accessible**: High contrast colors, proper touch targets, clear labels

### Color Scheme
- **Primary**: Purple (`#6C63FF`)
- **Accent**: Teal (`#00D4AA`)
- **Surface**: White with subtle variants
- **Background**: Light gray (`#F8F9FD`)
- **Dark Mode**: Full dark theme support

### Responsive Design
- **Phone Portrait**: Single column layouts
- **Phone Landscape**: Side-by-side layouts for better space utilization
- **Tablet (sw600dp)**: Larger text, spacing, and grid layouts
- **Adaptive Icons**: Vector drawables for all screen densities

## 📦 Project Structure

```
app/src/main/
├── java/com/ecotracker/lifelinepro/
│   ├── adapters/                 # RecyclerView adapters
│   │   ├── HabitAdapter.kt
│   │   ├── MoodAdapter.kt
│   │   ├── MoodSelectorAdapter.kt
│   │   └── HydrationAdapter.kt
│   ├── data/
│   │   ├── models/              # Data models
│   │   │   ├── Habit.kt
│   │   │   ├── MoodEntry.kt
│   │   │   ├── HydrationIntake.kt
│   │   │   └── UserSettings.kt
│   │   └── repository/          # Data persistence
│   │       └── SharedPreferencesManager.kt
│   ├── receivers/               # Broadcast receivers
│   │   ├── BootCompletedReceiver.kt
│   │   └── HydrationAlarmReceiver.kt
│   ├── ui/
│   │   └── fragments/           # App fragments
│   │       ├── HabitsFragment.kt
│   │       ├── MoodFragment.kt
│   │       ├── HydrationFragment.kt
│   │       └── SettingsFragment.kt
│   ├── utils/                   # Utility classes
│   │   └── HydrationReminderScheduler.kt
│   ├── widget/                  # Home screen widget
│   │   └── HabitProgressWidget.kt
│   ├── workers/                 # WorkManager workers
│   │   └── HydrationReminderWorker.kt
│   └── MainActivity.kt          # Main entry point
└── res/
    ├── layout/                  # Layouts
    ├── layout-land/             # Landscape layouts
    ├── layout-sw600dp/          # Tablet layouts
    ├── drawable/                # Icons and drawables
    ├── values/                  # Strings, colors, themes
    ├── values-night/            # Dark theme
    ├── values-sw600dp/          # Tablet dimensions
    └── xml/                     # Widget configuration
```

## 🔧 Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK API Level 25 or higher
- Kotlin 1.9 or newer
- Gradle 8.0 or newer

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd LifeLinePro
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the LifeLinePro folder

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the sync to complete

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button (▶️) or press Shift+F10
   - Grant notification permissions when prompted

## 📋 Assignment Requirements Checklist

### Functionality (3 Marks)
- ✅ **Daily Habit Tracker**: Add, edit, delete habits with progress tracking
- ✅ **Mood Journal with Emoji Selector**: Log moods with emojis and notes
- ✅ **Hydration Reminder**: WorkManager-based notification system

### Code Quality & Organization (2 Marks)
- ✅ Well-organized code with clear naming conventions
- ✅ Proper use of functions and classes
- ✅ No redundant or unnecessary code
- ✅ Comprehensive documentation and comments

### Creativity & User Interface Design (2 Marks)
- ✅ Clean, intuitive, and user-friendly design
- ✅ Modern Material Design 3 components
- ✅ Responsive layouts for different screen sizes and orientations
- ✅ Smooth animations and transitions

### Advanced Features & Data Persistence (3 Marks)
- ✅ **Home Screen Widget**: Shows daily habit completion percentage
- ✅ **Mood Trend Chart**: MPAndroidChart visualization
- ✅ **SharedPreferences**: Effective data persistence for all features
- ✅ **Additional Features**: Export data, settings management, dark mode support

## 🎯 Key Learning Outcomes

This project demonstrates proficiency in:

1. **Android Architecture**: Fragments, Activities, ViewBinding
2. **Data Persistence**: SharedPreferences without databases
3. **Background Work**: WorkManager for scheduled tasks
4. **UI/UX Design**: Material Design 3, responsive layouts
5. **Modern Android Development**: Kotlin, coroutines-ready architecture
6. **User Engagement**: Notifications, widgets, reminders
7. **Third-party Libraries**: MPAndroidChart, Gson

## 📸 Screenshots

### Phone (Portrait)
- Habits screen with completion card
- Mood journal with emoji selector
- Hydration tracker with reminder settings
- Settings screen with user preferences

### Tablet & Landscape
- Optimized layouts for larger screens
- Side-by-side views in landscape mode
- Grid layouts for better space utilization

## 🐛 Known Issues & Future Enhancements

### Potential Improvements
- Add more habit categories and icons
- Implement habit streaks and achievements
- Add more detailed statistics and insights
- Cloud backup and sync across devices
- Social features for sharing progress

## 📄 License

This project is created for educational purposes as part of an Android development assignment.

## 👨‍💻 Developer

Created with ❤️ for demonstrating Android development skills and principles.

---

**Note**: This app does not collect or transmit any personal data. All data is stored locally on your device.

