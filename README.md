# Always In Sight

**Your day and your week, written by hand, always in sight on your home screen.**

A minimal home-screen widget for Android. Your **Today** and **This week** lists sit on the home screen in your own handwriting. Tap the widget, write with a pen, press Done. No app to open, no app to close.

I only remember my tasks when I can see them. That's why this exists.

[⬇️ Download the APK](https://github.com/agrani58/Always_In_Sight/releases/latest). No Play Store needed.

## What it does

- **Two lists on the home screen:** Today and This week, as big ruled rows
- **Tap the widget** → a see-through writing sheet opens over your home screen
- **Write with a stylus** (a finger works too, until the first stylus touch; after that fingers are ignored so your palm doesn't scribble)
- **Pen / Eraser**, **Undo**, **Clear** (asks first), **Done**
- Press **Done** and the widget shows your handwriting
- Resize the widget to any width and height
- Translucent glass look, so your wallpaper shows through

## Features

- Tiny, pure Java, zero dependencies
- No ads, no trackers, no analytics, **no internet permission**
- No background services, no battery impact
- Handwriting is saved to your device's storage (private app files), so it survives reboots
- No permissions at all

## Install

1. Download `Always_In_Sight-v1.0.0.apk` from the [Releases page](https://github.com/agrani58/Always_In_Sight/releases/latest)
2. Open the APK. If Android asks, allow "Install unknown apps" for your browser or file manager
3. Long-press your home screen → **Widgets** → find **Always In Sight** → drag it anywhere
4. Tap it and start writing

Note: the app has no icon of its own. It lives only as a widget. To remove it, delete the widget, then uninstall from Settings → Apps.

## How it works

Android widgets can only show things, they can't hold a drawing surface. So the widget shows a picture of your handwriting, and tapping it opens a transparent writing sheet on top of your home screen. When you press Done the sheet closes and the widget picture is refreshed. Your handwriting is stored as two small PNG files in the app's private storage.

## Requirements

- Android 8.0+ (API 26)
- A home screen that supports widgets (all major launchers do)

## Privacy

This app has no internet permission and collects nothing. Your handwriting never leaves your device.

## Change the look

Open `app/src/main/res/values/colors.xml`:

- `widget_bg`: the glass fill of the widget. The first two hex digits are transparency (`00` invisible, `FF` solid).
- `scrim`: how much the home screen is dimmed while you write.

Pen thickness and ink colour are in `DrawView.java`. Number of rows is `ROWS` in `Sheet.java`.

## Build from source

The easy way is to let GitHub build it. Every push to `main` runs the **Build APK** workflow (Actions tab → latest run → Artifacts), and every tag like `v1.0.0` also attaches the APK to a Release.

To build locally you need JDK 17, the Android SDK and Gradle 8.9+:

```
gradle assembleRelease
```

Or open the folder in Android Studio.

The APK is signed with the standard debug key unless you provide your own key. To use your own key, add these repository secrets (Settings → Secrets and variables → Actions): `KEYSTORE_BASE64` (your `.jks` file, base64-encoded), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Keep the same key for every release so updates install over older versions.

## Release a new version

1. Change `versionCode` and `versionName` in `app/build.gradle`
2. Commit and push
3. Tag it: `git tag v1.1.0 && git push origin v1.1.0`

## Ideas for later

- Done checkboxes and strike-through
- Notes under each row
- Transparency slider
- F-Droid listing

## License

MIT, see [LICENSE](LICENSE).
