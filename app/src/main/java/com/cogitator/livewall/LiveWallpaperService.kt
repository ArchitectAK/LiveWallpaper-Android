package com.cogitator.livewall

import android.app.WallpaperManager
import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder


/**
 * @author Ankit Kumar on 08/11/2018
 */
class LiveWallpaperService : WallpaperService() {
    var mWallpaperWidth: Int = 0
    var mWallpaperHeight: Int = 0
    var mViewWidth: Int = 0
    var mViewHeight: Int = 0
    private var mSceneBitmap: Bitmap? = null
    private val mFrameRate = 20
    private val mBoidCount = 30
    private var mBoidSpriteSheet: Bitmap? = null
    private var mSpriteWidth = 1
    private var mSpriteHeight = 1
    private var mSpriteRow = 1
    private var mSpriteCol = 1

    override fun onCreate() {
        super.onCreate()

        // Get wallpaper width and height
        val wpm = WallpaperManager.getInstance(applicationContext)
        mWallpaperWidth = wpm.desiredMinimumWidth
        mWallpaperHeight = wpm.desiredMinimumHeight
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    fun setSceneBackground() {
        val b = BitmapFactory.decodeResource(resources, R.drawable.bg)

        if (null != mSceneBitmap)
            mSceneBitmap!!.recycle()

        val m = Matrix()
        m.setScale(
            mWallpaperWidth.toFloat() / b.width.toFloat(),
            mWallpaperHeight.toFloat() / b.height.toFloat()
        )
        mSceneBitmap = Bitmap.createBitmap(
            b, 0, 0,
            b.width, b.height, m, true
        )

        b.recycle()
    }

    fun setSprites() {
        mBoidSpriteSheet = BitmapFactory.decodeResource(resources, R.drawable.bats)
        mSpriteWidth = 100
        mSpriteHeight = 50
        mSpriteRow = mBoidSpriteSheet!!.height / mSpriteHeight
        mSpriteCol = mBoidSpriteSheet!!.width / mSpriteWidth
    }

    override fun onCreateEngine(): Engine {
        return LiveEngine()
    }

    internal inner class LiveEngine : WallpaperService.Engine() {
        private val mHandler = Handler()
        private var mOffset = 0.0f
        private val mPaint = Paint()
        private var mBoids: Boids? = null
        private val mDrawThread = Runnable {
            drawFrame()

            if (mBoids != null)
                mBoids!!.moveToNext()

            try {
                Thread.sleep(50)
            } catch (e: Exception) {
            }
        }
        private var mVisible: Boolean = false

        init {
            setSceneBackground()
            setSprites()

            mBoids = Boids(
                mBoidCount, mWallpaperWidth,
                mWallpaperHeight, mSpriteRow * mSpriteCol
            )
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
        }

        override fun onDestroy() {
            super.onDestroy()
            mHandler.removeCallbacks(mDrawThread)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            mVisible = visible
            if (visible) {
                drawFrame()
            } else {
                mHandler.removeCallbacks(mDrawThread)
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder,
            format: Int, width: Int, height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)

            mViewWidth = width
            mViewHeight = height

            drawFrame()
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            mVisible = false
            mHandler.removeCallbacks(mDrawThread)
        }

        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float,
            xStep: Float, yStep: Float, xPixels: Int, yPixels: Int
        ) {
            super.onOffsetsChanged(
                xOffset, yOffset, xStep, yStep,
                xPixels, yPixels
            )

            mOffset = 1.0f * xPixels

            drawFrame()
        }

        override fun onTouchEvent(event: MotionEvent) {
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
                mBoids!!.setTargetPlace(
                    event.x, event.y,
                    0.50f
                )
            } else {
                mBoids!!.setTargetNone()
            }

            super.onTouchEvent(event)
        }

        fun drawFrame() {
            val holder = surfaceHolder
            val frame = holder.surfaceFrame
            val width = frame.width()
            val height = frame.height()
            var c: Canvas? = null

            try {
                c = holder.lockCanvas()
                if (c != null) {

                    c!!.drawBitmap(mSceneBitmap, mOffset, 00.0f, null)

                    if (mBoids != null && mBoidSpriteSheet != null) {
                        val bb = mBoids!!.getBoids()
                        for (i in 0 until mBoids!!.getTotal()) {
                            val yy = bb[i].mState / mSpriteCol * mSpriteHeight
                            val xx = bb[i].mState % mSpriteCol * mSpriteWidth
                            val src = Rect(
                                xx, yy, xx + mSpriteWidth,
                                yy + mSpriteHeight
                            )
                            val dest = Rect(
                                bb[i].mPosition.x.toInt(),
                                bb[i].mPosition.y.toInt(),
                                bb[i].mPosition.x.toInt() + mSpriteWidth,
                                bb[i].mPosition.y.toInt() + mSpriteHeight
                            )
                            c!!.drawBitmap(mBoidSpriteSheet, src, dest, null)
                        }
                    }
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c)
            }

            mHandler.removeCallbacks(mDrawThread)
            if (mVisible) {
                mHandler.postDelayed(mDrawThread, (1000 / mFrameRate).toLong())
            }
        }
    } // MyEngine

}