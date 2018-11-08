package com.cogitator.livewall

import android.graphics.Paint
import android.os.Handler
import android.service.wallpaper.WallpaperService

/**
 * @author Ankit Kumar on 08/11/2018
 */
class LiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return LiveEngine()
    }

    inner class LiveEngine : Engine() {
        val mHandler =  Handler()
        val mOffset = 0.0f
        val mPaint = Paint()
        private Boids mBoids = null
        private final Runnable mDrawThread = new Runnable() {
            public void run {
                drawFrame()

                if (mBoids != null)
                    mBoids.moveToNext()

                try {
                    Thread.sleep(50)
                } catch (Exception e) {
                }
            }
    }

}