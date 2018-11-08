package com.cogitator.livewall

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * @author Ankit Kumar on 08/11/2018
 */
class TutorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val i = Intent()
        i.action = WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
        i.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(
                LiveWallpaperService::class.java.getPackage().name,
                LiveWallpaperService::class.java.canonicalName
            )
        )

        startActivity(i)
    }
}