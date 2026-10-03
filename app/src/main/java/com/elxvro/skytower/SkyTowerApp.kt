package com.elxvro.skytower

import android.app.Application
import com.elxvro.skytower.ui.assets.UiAssetRuntime

class SkyTowerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        UiAssetRuntime.install(assets)
    }
}
