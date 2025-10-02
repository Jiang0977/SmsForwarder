# SmsForwarder Project Wiki Documentation

## Project Overview

SmsForwarder is an Android application that can monitor SMS, incoming calls, and app notifications on your phone, and forward these messages based on user-defined rules to other platforms such as DingTalk, WeCom, Feishu, email, Telegram, etc.

## Key Features

1. **SMS Forwarding**: Listen to and forward received SMS
2. **Call Forwarding**: Listen to and forward incoming call information
3. **Notification Forwarding**: Listen to and forward app notifications
4. **Multiple Forwarding Channels**:
   - DingTalk Group Bot
   - WeCom Group Bot
   - Feishu Group Bot
   - Email
   - Telegram Bot
   - Webhook
   - ServerChan
   - PushPlus
   - SMS, etc.
5. **Remote Control**: Support active control of server and client, enabling remote SMS sending, SMS checking, call checking, etc.
6. **Automated Tasks**: Support quick commands for automation

## Technical Architecture

### Core Components

- **Kotlin**: Main development language
- **Android Room**: Local database storage
- **AndServer**: Built-in HTTP server
- **WorkManager**: Background task processing
- **XXPermissions**: Permission management
- **XUI**: UI component library

### Project Structure

```
app/src/main/
├── java/                    # Java/Kotlin source code
│   └── com/idormy/sms/forwarder/
│       ├── activity/        # Activity components
│       ├── adapter/         # Adapters
│       ├── core/            # Core components
│       ├── database/        # Database related
│       ├── entity/          # Data models
│       ├── fragment/        # Fragment components
│       ├── receiver/        # Broadcast receivers
│       ├── server/          # Server related
│       ├── service/         # Service components
│       ├── utils/           # Utility classes
│       ├── widget/          # Custom widgets
│       ├── workers/         # Background workers
│       └── App.kt           # Application entry point
├── res/                     # Resource files
└── assets/                  # Static assets
```

## Build and Package

### Requirements

- Windows 11
- Android Studio or IntelliJ IDEA
- JDK 8 or higher
- Android SDK

### Build Steps

1. Clone the project code
2. Open the project with Android Studio
3. Sync Gradle dependencies
4. Build the APK

### Command Line Build

```bash
# Execute on Windows environment
gradlew.bat assembleRelease
```

The generated APK file is located at: `app/build/outputs/apk/release/`

## Configuration

### Signing Configuration

Configure signing information in the `keystore/keystore.properties` file:

```properties
keyAlias=your_key_alias
keyPassword=your_key_password
storeFile=your_keystore_file_path
storePassword=your_store_password
```

### Multi-channel Packaging

Support for packaging by CPU architecture:
- armeabi-v7a
- arm64-v8a
- x86
- x86_64

## User Guide

### Basic Setup

1. After installing the app, grant necessary permissions (SMS, phone, notifications, etc.)
2. Add forwarding rules in "Forwarding Rules"
3. Configure forwarding destinations (such as DingTalk bot, email, etc.)
4. Enable corresponding listening functions

### Forwarding Rule Configuration

Forwarding rules include the following elements:
- **Trigger Conditions**: What events trigger forwarding (SMS, calls, notifications)
- **Filter Conditions**: Filter based on keywords, numbers, etc.
- **Forwarding Destination**: Which platform to forward to
- **Message Template**: Custom format for forwarded content

### Remote Control

Through the built-in HTTP server, remote control functions can be achieved:
- Send SMS
- Query SMS
- Query call logs
- Query contacts
- Query battery information

## FAQ

1. **Cannot receive SMS**: Check if SMS permissions are granted and if it's set as the default SMS app
2. **Forwarding failure**: Check network connection and verify that forwarding destination configuration is correct
3. **Background operation**: Ensure that background operation permissions for the app are enabled and add it to the battery optimization whitelist

## Development Guide

### Adding New Forwarding Channels

1. Create a new sender entity class in the `entity/sender` package
2. Create the corresponding configuration interface in the `fragment/senders` package
3. Implement the specific sending logic

### Extending Listening Functions

1. Create a new `BroadcastReceiver` to listen for specific events
2. Register the broadcast receiver in `AndroidManifest.xml`
3. Add corresponding switch options in the settings interface

## Contributing

Feel free to submit Issues and Pull Requests to improve SmsForwarder.

## License

This project is licensed under the BSD License. See the [LICENSE](LICENSE) file for details.