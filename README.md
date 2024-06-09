# Login Screen with Registration System and Injection Capability

This project provides a complete login screen with an optional registration system, integrated Discord page, and additional security measures.

## Injection

This project is suitable for those who wish to perform code injection. The injection logic in the target application is contained in the `HomeActivity.kt` file. The code in this file utilizes the [project](https://github.com/Vitor-VX/FakeLib-Inject).

## Requirements

To use this application, you will need a server/API for user authentication. The registration system is optional and can be disabled as needed. Example code for redirection:

```javascript
return res.redirect(`vxinjector://login-success?token=${tokenUser}`)
```

## Features

- **Login**: Users can securely log in.
- **Optional Registration**: A registration system is available but optional and can be disabled as needed.
- **Integrated Discord Page**:  Place your Discord community within the app.
- **Device Choice**: Choose between mobile devices and emulators to test the app.
- **Basic Security Measures**: Includes basic Frida detection and internal library detection.

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- Android Studio
- Emulator or Android device

### Installation

1. Clone the repository:

```
https://github.com/Vitor-VX/page-login-vx.git
```

2. Open the project in Android Studio.

3. Compile and run the project on your emulator or device.

## Important

If you wish to add another library, it is necessary to increase the `#define MAX_LIB` in the `libdetect.h` file in the `vx/` folder. Otherwise, the application will consider it an internal library (possible detection).

It is advisable to use a rooted device. If it is not a rooted device, set the `isRoot` variable in the `libdetect.cpp` file to `false` to avoid issues.
