# IPTV Pro 📺

اپلیکیشن اندرویدی پخش آی‌پی‌تی‌وی (IPTV) با رابط کاربری مدرن — ساخته‌شده با Jetpack Compose و Kotlin.

## ✨ امکانات

- مدیریت لیست کانال‌ها با دیتابیس محلی (`IptvDatabase` + `IptvRepository`)
- پخش زنده کانال‌ها از طریق `VideoPlayerView`
- معماری MVVM با ViewModel و Compose
- تم و تایپوگرافی سفارشی (Material 3)

## 🧱 ساختار پروژه

```
app/src/main/java/com/example/
├── MainActivity.kt
├── data/
│   ├── IptvDatabase.kt
│   └── IptvRepository.kt
└── ui/
    ├── IptvApp.kt
    ├── IptvViewModel.kt
    ├── components/VideoPlayerView.kt
    └── theme/…
```

## 🛠 بیلد

پیش‌نیاز: [Android Studio](https://developer.android.com/studio)

1. پروژه را در Android Studio باز کن (**Open** ← انتخاب پوشه پروژه).
2. اجازه بده Android Studio ناسازگاری‌های import را خودکار اصلاح کند.
3. در صورت نیاز یک فایل `.env` در ریشه پروژه بساز و `GEMINI_API_KEY` را داخلش قرار بده.
4. روی **Run** بزن ▶️

این پروژه از [AI Studio](https://ai.studio/apps/211a2bf7-9c92-4ab9-8f98-2fe8417689b7) قابل دسترس نیز هست.

---
**Crafted with passion by Hellboy Coder ⚡**

