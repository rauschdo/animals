[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# animals 🐼 🐶 🐢 🐦 🦦

A learning project originally written at the end of 2021. This project primarily looked at Databinding, Hilt and Room.
This Project uses the Android View System. It also provides an [AppWidget](https://developer.android.com/develop/ui/views/appwidgets/overview).

❗Datasets and texts are only in German language

See [Releases](https://github.com/rauschdo/animals/releases) for APK

![Preview](/assets/collage.jpg "Preview Collage")

## 📋 Requirements

This Project uses Google Maps with the [Google Maps Secret Plugin](https://developers.google.com/maps/documentation/android-sdk/secrets-gradle-plugin) and requires that you add your own Google Maps API key. For Instructions how to Get an API Key [check the Documentation](https://developers.google.com/maps/documentation/android-sdk/get-api-key)<br/>
If you don't have a key you must at least define the required constant in your <b>local.properties</b> file with some value
```
    MAPS_API_KEY=someValue
```

## 🛠️ Used Dependencies/Technologies

- Various [AndroidX Libraries](https://developer.android.com/jetpack/androidx/versions#version-table)
<details>
  <summary>Click me to see used AndroidX Libraries</summary>

- appcompat
- activity
- browser
- core
- constraint
- fragment
- lifecycle
- navigation
- preference
- recyclerview (selection)
- room
- transition
- viewpager2
- work
</details>

- [Hilt](https://developer.android.com/jetpack/androidx/releases/hilt) for Dependency Injection
- [Google Maps](https://developers.google.com/maps/documentation/android-sdk/)
- [Material Components](https://github.com/material-components/material-components-android)
- [Gson](https://github.com/google/gson)
- [Realm java](https://github.com/realm/realm-java) as example for persistance
- [Glide](https://bumptech.github.io/glide/) for image loading
- [Lottie](https://github.com/airbnb/lottie-android) for some animations
- [ThreeTen ABP](https://github.com/JakeWharton/ThreeTenABP) for time handling
- [Skydoves Bindables](https://github.com/skydoves/Bindables)
- [Skydoves TransformationLayout](https://github.com/skydoves/TransformationLayout)
- [Shimmer](https://github.com/facebookarchive/shimmer-android)
- [DotsIndicator](https://github.com/tommybuonomo/dotsindicator)
- [Konfetti](https://github.com/DanielMartinus/Konfetti)
- [Zoomage](https://github.com/jsibbold/zoomage)

Additionally this project used the [Google Plugin for Open Source Notices](https://developers.google.com/android/guides/opensource)