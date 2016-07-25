# Mobile-Reuse-Hybrid-NTLMAuth

## Version
Reuse Repo - 1.0.0

## About
This is a Cordova plugin for iOS and Android that allows developers to use HTTP methods through NTLM Authentication. Features include:

- Keychains (iOS)
- NTLM Authentication

## Setup
To add the NTLM Auth Plugin, simply write:
```
cordova plugin add https://github.com/pfizer/Mobile-Reuse-Hybrid-NTLMAuth/tree/master/NTLMAuth
```

**Note (for Android):**
This plugin is compatible with Java 1.7 and up, Android API 23, and Java SDK 8. If you have Java compatibility error, please follow these steps:

1. Go to `./platforms/android` and create a file called `build-extras.gradle`.
2. Copy and paste this into `build-extras.gradle`:
```
ext.postBuildExtras = {
    android {
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_7
            targetCompatibility JavaVersion.VERSION_1_7
        }
    }
}
```

If you get `DuplicateFileException: Duplicate files copied in APK META-INF/NOTICE.txt` error, please follow these steps:

1. Open `build.gradle` file that is located in `./platforms/android`
2. Copy and paste this into `build.gradle`:
```
 android {
     packagingOptions { 
         exclude 'META-INF/LICENSE.txt' 
         exclude 'META-INF/NOTICE.txt' 
     }
 }  
 ```

## Implementation
To connect with the plugin, you have to call the Javascript interface. Here is how you call it:
```
ntlmAuth.callNtlmMethods(
        //an array of inputs
        values, 
        //success callback
        function callback(data) {
            alert("Response from plugin: " + data);
        },
        //error callback
        function errorHandler(err) {
            alert("Response from plugin (error): " + err);
        }
    );
```
`values` array must contain:

1. User name (required)
2. Password (required)
3. URL (required)
4. URL Path (not required)
5. Method Name (required) (only GET, POST, PUT, HEAD, and DELETE)
6. Header (not required) (Please enter in this format: `Accept:application/json`)
7. Data (only required for POST, PUT, and DELETE) (Please enter in this format: `Example:Example`)

*Note:* Please refer to the `NTLMAuthTestApp` for further implementation example

## Contact
If you have any questions about this plugin or it's implementation, feel free to contact me via email at BaoThien.Pham@Pfizer.com




