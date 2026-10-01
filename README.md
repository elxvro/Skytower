# SkyTower

SkyTower, Google Play için geliştirilen çevrimdışı ve tek dokunuşla oynanan 2D kule oyunudur. Hareket eden bloğu kuleye bırak; taşan kısım kesilir, kusursuz hizalamalarda combo artar ve tam kaçırmada oyun biter.

## v0.1 özellikleri

- Tek dokunuşla blok bırakma.
- Gerçek overlap/kesme matematiği.
- Perfect placement ve combo geri bildirimi.
- Skor ve cihazda kalıcı en iyi skor.
- Pause, retry ve ana menü akışları.
- Ses ve titreşim aç/kapat seçenekleri.
- 3 yerel gökyüzü teması.
- Procedural bulutlar, uçan adalar ve parlak blok çizimi.
- İnternet, hesap, reklam ve backend gerektirmez.

## Android yapılandırması

- Package: `com.elxvro.skytower`
- minSdk: 26
- compileSdk: 36
- targetSdk: 36
- Kotlin: 2.2.10
- Android Gradle Plugin: 8.13.2
- Gradle: 8.13
- JDK: 17

Yerel sistemde Gradle 8.13 ve Android SDK 36 kuruluysa:

```bash
gradle testDebugUnitTest lintDebug assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions

`.github/workflows/android-build.yml` şu adımları çalıştırır:

1. Unit tests
2. Android lint
3. Debug APK
4. Release APK
5. Release AAB

İmzalama sırrı yoksa debug APK kurulabilir test çıktısıdır. Release APK/AAB imzasız üretilir. Google Play için imzalı release oluşturmak üzere GitHub Secrets içine şunlar eklenebilir:

- `SKYTOWER_KEYSTORE_BASE64`
- `SKYTOWER_KEYSTORE_PASSWORD`
- `SKYTOWER_KEY_ALIAS`
- `SKYTOWER_KEY_PASSWORD`

Keystore dosyası veya parolalar hiçbir zaman repoya eklenmemelidir.
