---
description: "Rules for ads config implementation: NativeView, BannerView, AdsConfigProvider, RemoteConfig, BuildConfig"
globs: "**/ads/**/*.kt,**/ui/screen/**/*.kt,**/res/raw/ads_config.json"
---

# Ads Config Implementation Rules

## Config Flow

```
App init → AdsConfigProvider (loadFromSharedPrefs only, NO local load)
         → RemoteConfigManager fetch RC
         → RC done: loop save all keys to SharedPrefs (key "ads_config")
         → loadFromSharedPrefs() to update config
         → loadFromLocalIfNeeded() if config is still null (fallback)
         → fetchedStateFlow = true
SplashScreen → wait for fetchedStateFlow → then load ads
```

## NativeView

- Use `nameSpace` + `defaultLayout` params. NEVER pass `layoutKey`/`layoutKeyMeta`/`layoutKeyCollapse`.
- `isCollapseType` and `reloadCollapse` are resolved internally from AdsConfigProvider by nameSpace, never passed from caller.

```kotlin
NativeView(
    modifier = ...,
    nativeAdUnit = adsManager.nativeXxx,
    nameSpace = "native_xxx",
    defaultLayout = R.layout.native_media_ctr_bot_big_filled,
)
```

## BannerView

- Use `bannerLayoutKey: String` (single param). NEVER use `bannerTypeConfig: Pair`.

```kotlin
BannerView(
    adUnit = adsManager.bannerInappUnitId,
    adUnitName = "banner_inapp",
    bannerLayoutKey = "layout_banner_add_stamp",
)
```

## InterBackGeneral

- Use `InterBackGeneral` composable for interstitial back-navigation ads.
- Replaces 25+ lines of manual load/show/dialog logic.

```kotlin
val handleInterBack = InterBackGeneral {
    backStack.pop(AppDestination.YourScreen::class.java)
}
onBack = handleInterBack
```

## AdsConfigProvider

- **Init**: Only `loadFromSharedPrefs()` — never load local at init.
- **loadFromSharedPrefs()**: Reads `prefs.getString("ads_config", "")` → `updateFromJson()`.
- **loadFromLocalIfNeeded()**: If `config == null`, loads from `res/raw/ads_config.json`.
- **getLayoutResForNameSpace(nameSpace, key, defaultRes)**: Resolves layout from position config.
- **getNativeCollapseType(nameSpace, default)**: Returns `nativeSplashCollapse`.
- **getReloadNativeCollapse(nameSpace, default)**: Returns `reloadNativeSplashCollapse`.
- **Test flavor**: `getUnitId()` returns `AdTest.getTestUnitId(name)` when `BuildConfig.IS_TEST_FLAVOR`.

## RemoteConfigManager

- RC key: `"ads_config"` (Firebase Remote Config).
- After fetch: loop save all RC keys to SharedPrefs.
- After loop: `adsConfigProvider.loadFromSharedPrefs()`.
- Then: `adsConfigProvider.loadFromLocalIfNeeded()` → `_fetchedFlow.value = true`.
- **addOnFailureListener**: Also calls `loadFromLocalIfNeeded()` then sets fetched.

## SplashScreen

- Must wait for `remoteConfigManager.fetchedStateFlow` before loading ads.
- Use `combine(consentFinished, fetchedStateFlow, billingState)`.

## BuildConfig

- Only `ADMOB_APP_ID` and `IS_TEST_FLAVOR` in BuildConfig.
- Ad unit IDs come from AdsConfigProvider, NOT BuildConfig.
- Test flavor: AdsConfigProvider.getUnitId → AdTest.getTestUnitId.

## unitIds Format

```json
"unitIds": [
  {"role": "high", "id": "ca-app-pub-xxx/123"},
  {"role": "allprice", "id": "ca-app-pub-xxx/456"}
]
```

- `role`: "high" (priority) or "allprice" (fallback).
- Single ID: use `{"role": "allprice", "id": "..."}`.
- Legacy `["id1","id2"]` still parseable (index 0=high, 1=allprice).

## getUnitId Resolution Order

1. `unitIdOverrides` — takes priority if present
2. Native/Banner: lookup by role in `unitIds`
3. Conventions: `inter_splash*`, `app_open*`, `open_*`, `reward_*`, `inter_inapp*`

## Screen Tracking

- Add `TrackingScreen` composable in each screen for back navigation ad interval counting.

## App Resume Ad — Skip khi app tự phát intent ra ngoài

Rule: App Open Resume ad CHỈ được show khi user tự thao tác background app (home/recents/overview). Bất kỳ khi nào APP tự phát intent ra ngoài (settings, share, mail, market, permission system dialog, file picker, external browser…), PHẢI gọi `adsManager.disableAdResumeOnTime()` ngay TRƯỚC `startActivity(...)` hoặc `launcher.launch(...)`. Flag `disableAdResumeOneTime` được `BaseAds.onActivityPaused/Resumed` check → skip show + auto reset về false.

### Pattern

Trong util function (Context extension) — lấy qua Koin:
```kotlin
import com.example.a036_remote_tv_inhouse.ads.AdsManager
import org.koin.java.KoinJavaComponent.getKoin

fun Context.openWifiSetting() {
    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
    getKoin().get<AdsManager>().disableAdResumeOnTime()
    this.startActivity(intent)
}
```

Trong Composable / Screen — dùng `adsManager` đã inject qua `koinInject()`:
```kotlin
val adsManager: AdsManager = koinInject()
...
adsManager.disableAdResumeOnTime()
permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
```

### Bắt buộc áp dụng

- Mọi `startActivity(intent)` phát ra ngoài app: ACTION_VIEW URL, ACTION_SENDTO, ACTION_SEND share, Settings.ACTION_*, market://, Intent.createChooser…
- Mọi `ActivityResultLauncher.launch(...)` — notification/mic/camera/storage permission, file picker, document picker, image picker…
- Mọi external WebView URL mở qua Intent

### KHÔNG cần

- Navigation nội bộ trong app (backStack/navigateTo/pop)
- Các route đã nằm trong `screenSkipAdResume` (Splash, Settings, LanguageSetting, WebView) — đã skip sẵn
- Inter/Reward ads — `isShowingAds` flag đã bảo vệ

### Anti-pattern

KHÔNG tạo wrapper `Context.startActivitySkipResumeAd()` — inline 1 dòng `disableAdResumeOnTime()` trước `startActivity` để grep dễ, review rõ ý định.
