## Android client for [Swing Music](https://)

This project is currently in its early development stages as the Swing Music team is diligently working to deliver an MVP as soon as possible.

### What do you need to run this app?

Anyone should be able to clone this repo and open it with Android Studio. You need at least:

```kt
Android Studio Hedgehog | 2023.1.1 Patch 1
```

Android Studio will automatically download the following core components if you don't already have them:

```kt
Kotlin version 1.9.0
```

```kt
Compose version 1.5.1
```

```gradle
Gradle version 8.2
```

## Development Plan

Swing Music is poised to be a sophisticated app expected to scale seamlessly. To facilitate this, the project is `modularized by feature` and utilizes an MVVM architecture.

#### 1. `:folder` Module

This is a feature module with access to the `core`,`uicomponent` and `network` module.

It covers the following tasks:

| Task                               | Status |
|------------------------------------|--------|
| List Folders                       | ✔ Done |
| List Tracks within a folder        | ✔ Done |
| Implement breadcrumb navigation    | ✔ Done |

#### Screenshots

<table>
  <tr>
    <td>
      <img src="https://github.com/swing-opensource/android/assets/54077752/3ff804e0-9a06-4352-8bb0-463e7e1c3bbf" width="320" alt="image" />
    </td>
    <td>
      <img src="https://github.com/swing-opensource/android/assets/54077752/2ff2a86c-c1ad-4dd1-901a-e18b4103b420" width="320" alt="image" />
    </td>
  </tr>
</table>

#### `:folder` Known Issues

- The first screen should display `$home` directories rather than manually crafted directories. `status:` `PENDING`
- Track images are a bit blurry. `status:` `PENDING`

### Important Tips

Currently, we are using [Mungaist](https://music.mungaist.com/) as our test server. However, you can set the base URL to your desired server, including your local Swing Music server.
