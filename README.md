# Spendwise 💰

**Spendwise** is a modern, feature-rich Android application designed to help users take full control of their personal finances. From tracking daily expenses with receipt photos to setting complex monthly budgets and goals, Spendwise makes financial management intuitive and accessible.

**Repository**: [https://github.com/MamaboloKhutjo/Spendwise](https://github.com/MamaboloKhutjo/Spendwise)

## 🚀 Key Features

### 🔐 Secure User Management
- **Multi-user Support**: Complete data isolation. Every user has their own private database entries.
- **Authentication**: Secure Login and Sign-up flows with session persistence.
- **Session Persistence**: The app remembers you! Skip the login screen after your first sign-in.

### 💸 Advanced Expense Tracking
- **Detailed Entries**: Specify date, start/end times, and category for every transaction.
- **Receipt Photography**: Attach photos of your receipts directly to your expenses using the built-in camera integration.
- **Interactive History**: View your transaction history with a user-selectable period filter (Start Date to End Date).
- **Full Details**: Click any transaction to view its full summary, notes, and receipt photo.

### 📊 Budgeting & Goals
- **Initial Monthly Budget**: Set your total monthly allowance and watch it deduct automatically as you spend.
- **Spending Goals**: Set specific **Minimum** and **Maximum** monthly spending goals.
- **Real-time Analytics**: A dynamic Dashboard shows your "Available Balance," total spent so far, and progress bars tracking your goals.
- **Goal Advice**: Receive smart feedback on your dashboard (warnings if over budget, praise if staying under your minimum goal).

### 🏷️ Category Management
- **Pre-defined Categories**: Comes with defaults like Food, Transport, and Shopping.
- **Custom Categories**: Create your own categories on the fly to match your lifestyle.
- **Budget by Category**: Set specific spending limits for individual categories.

## 🛠️ Tech Stack
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) (SQLite)
- **UI Architecture**: XML Layouts with Material Design 3 components
- **Concurrency**: Kotlin Coroutines & Lifecycle Scopes
- **Image Handling**: Android FileProvider for secure camera access
- **Build System**: Gradle Kotlin DSL (.kts)
- **Charts**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) for visual analytics

## 🎨 UI/UX Design
- **Modern Aesthetic**: Dark headers with 32dp rounded corners for a premium feel.
- **Animated Splash Screen**: Custom loading dots animation on startup.
- **Color Coding**: Visual cues (Red for expenses, Green for income/savings) to provide instant financial context.
- **Responsive Layouts**: Touch-optimized interface for all screen sizes.

## ✅ Feasibilities & Capabilities

### What Spendwise Can Do
- ✅ Track unlimited expenses with precise timestamps
- ✅ Capture and store receipt images for proof of purchase
- ✅ Support multiple user accounts with complete data privacy
- ✅ Generate real-time financial dashboards with visual progress indicators
- ✅ Set and monitor both minimum and maximum monthly spending goals
- ✅ Create custom spending categories tailored to user needs
- ✅ Generate detailed expense reports filtered by date range
- ✅ Provide automated budget warnings and financial advice
- ✅ Persist all data locally with encrypted Room database
- ✅ Work offline with full functionality (no internet required)

### Current Limitations
- Mobile-only (Android 8.0+)
- Single-device storage (no cloud sync)
- Manual data export not yet implemented
- Limited to local camera integration

## 📖 Usage Guide

### Getting Started
1. **Download & Install**: Get the app from your Android device or emulator
2. **Create Account**: Sign up with your credentials
3. **Set Monthly Budget**: Enter your total monthly spending limit on the dashboard
4. **Define Goals**: Set your minimum and maximum spending targets

### Daily Usage
1. **Track Expense**: 
   - Click "New Expense"
   - Select category, enter amount, and add optional notes
   - Attach a receipt photo using your camera

2. **View Dashboard**:
   - Check available balance at a glance
   - Monitor progress towards your spending goals
   - See category-wise breakdown of expenses

3. **Review History**:
   - Filter transactions by date range
   - Click any transaction for full details including receipt photos
   - Export or share expense summaries

4. **Manage Categories**:
   - Create custom categories for specialized expenses
   - Set category-specific budgets
   - Edit or delete categories as needed

## 📥 Installation

### Prerequisites
- **Android Studio** (Latest Version)
- **JDK 17** or higher
- **Android SDK 8.0** (API Level 26) or higher
- **Gradle** (Included with Android Studio)

### Step-by-Step Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/MamaboloKhutjo/Spendwise.git
   cd Spendwise
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - Select **File > Open** and navigate to the project folder
   - Click **OK**

3. **Configure Your Environment**:
   - Open `local.properties` and ensure `sdk.dir` points to your Android SDK location
   - If `local.properties` doesn't exist, Android Studio will create it automatically

4. **Sync Gradle**:
   - Wait for Android Studio to sync all dependencies
   - Resolve any dependency conflicts if prompted

5. **Build & Run**:
   - Connect an Android device via USB (with USB Debugging enabled) or use an emulator
   - Click the **Run** button (green play icon) or press `Shift + F10`
   - Select your target device/emulator and click **OK**
   - The app will build and install automatically

6. **First Launch**:
   - Sign up for a new account
   - Set your monthly budget and spending goals
   - Start tracking expenses!

### Troubleshooting Installation
- **Gradle Sync Issues**: Clean build with `File > Invalidate Caches > Invalidate and Restart`
- **Missing SDK**: Go to `Tools > SDK Manager` and install required Android SDK levels
- **JDK Version Error**: Ensure JDK 17 is selected in `File > Project Structure > SDK Location`

## 🤝 Contributing

We welcome contributions! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Developed with ❤️ by Mamabolo Khutjo*
