package com.cogitator.livewall


/**
 * @author Ankit Kumar on 08/11/2018
 */
class Boids constructor(val mTotal: Int, val mWidth: Int, val mHeight: Int, val mStates: Int) {
    var mBoidDist = 50.0f
    var mMaxBoidSpeed = 12.0f
    var mBoidList: MutableList<Boid> = mutableListOf()
    var mCount = 5
    var mMaxCount = 5
    var mPlace = Position()
    var mPlaceFactor = 1.0f
    var mBoxMinX = 1000f
    var mBoxMaxX = 0f
    var mBoxMinY = 1000f
    var mBoxMaxY = 0f


    init {

        for (i in 0 until mTotal) {
            mBoidList[i] = Boid(mStates)

            mBoidList[i].mPosition?.x = (200 * Math.random()).toFloat()
            mBoidList!![i].mPosition.y = (300 * Math.random()).toFloat()

            while (0f == mBoidList!![i].mVelocity.x) {
                mBoidList!![i].mVelocity.x = (2.0 * mMaxBoidSpeed.toDouble() *
                        Math.random()).toInt() - mMaxBoidSpeed
            }

            while (0f == mBoidList!![i].mVelocity.y)
                mBoidList!![i].mVelocity.y = (2.0 * mMaxBoidSpeed.toDouble() *
                        Math.random()).toInt() - mMaxBoidSpeed
        }
    }

    fun moveToNext() {
        for (i in 0 until mTotal) {
            val v1: Velocity
            val v2: Velocity
            if (mCount <= mMaxCount) {
                v1 = ruleTendToPlace(i, mPlaceFactor)
                v2 = Velocity()
            } else {
                v1 = ruleFlyToCentroid(i, 0.05f)
                v2 = ruleKeepSmallDistance(i, mBoidDist)
            }
            val w1 = 1.0f
            val w2 = 1.0f

            val v3 = ruleMatchNearVelocity(i, 0.125f)
            val w3 = 1.0f

            mBoidList!![i].mVelocity.x += w1 * v1.x + w2 * v2.x + w3 * v3.x
            mBoidList!![i].mVelocity.y += w1 * v1.y + w2 * v2.y + w3 * v3.y

            ruleLimitVelocity(i, mMaxBoidSpeed)

            mBoidList!![i].mPosition.x += mBoidList!![i].mVelocity.x
            mBoidList!![i].mPosition.y += mBoidList!![i].mVelocity.y

            ruleBoundPosition(
                i, (0.7 * mMaxBoidSpeed.toDouble() *
                        Math.random() + 0.3f * mMaxBoidSpeed).toFloat()
            )

            mBoidList!![i].changeState()
        }

        if (mCount <= mMaxCount)
            mCount++
    }

    private fun getBoundingBox() {
        mBoxMinX = mWidth.toFloat()
        mBoxMaxX = 0f
        mBoxMinY = mHeight.toFloat()
        mBoxMaxY = 0f
        for (i in 0 until mTotal) {
            if (mBoidList!![i].mPosition.x < mBoxMinX)
                mBoxMinX = mBoidList!![i].mPosition.x
            if (mBoidList!![i].mPosition.x > mBoxMaxX)
                mBoxMaxX = mBoidList!![i].mPosition.x
            if (mBoidList!![i].mPosition.y < mBoxMinY)
                mBoxMinY = mBoidList!![i].mPosition.y
            if (mBoidList!![i].mPosition.y > mBoxMaxY)
                mBoxMaxY = mBoidList!![i].mPosition.y
        }
    }

    // Set a target place
    fun setTargetPlace(x: Float, y: Float, f: Float) {
        mPlace!!.x = x
        mPlace!!.y = y

        // If the touched point is within the boids, disperse
        // them away.
        // If it is outside the boids, make them follow it.
        getBoundingBox()
        if (x >= mBoxMinX && x <= mBoxMaxX && y >= mBoxMinY
            && y <= mBoxMaxY
        ) {
            mPlaceFactor = -f
            mCount = 0
            mMaxCount = 50
        } else {
            mPlaceFactor = f
            mCount = 0
            mMaxCount =
                    (Math.abs((mBoxMinX + mBoxMaxX) / 2.0f - x) + Math.abs((mBoxMinY + mBoxMaxY) / 2.0f - y)).toInt()
        }
    }

    fun setTargetNone() {
        // If direction is "following", stop it.
        if (mPlaceFactor > 0) {
            mCount = mMaxCount - 10
        }
    }

    fun getTotal(): Int {
        return mTotal
    }

    fun getBoids(): List<Boid> {
        return mBoidList
    }

    // RULE: fly to center of other boids
    private fun ruleFlyToCentroid(id: Int, factor: Float): Velocity {
        val v = Velocity()
        val p = Position()

        for (i in 0 until mTotal) {
            if (i != id) {
                p.x += mBoidList!![i].mPosition.x
                p.y += mBoidList!![i].mPosition.y
            }
        }

        if (mTotal > 2) {
            p.x /= mTotal - 1
            p.y /= mTotal - 1
        }

        v.x = (p.x - mBoidList!![id].mPosition.x) * factor
        v.y = (p.y - mBoidList!![id].mPosition.y) * factor

        return v
    }

    // RULE: keep a small distance from other boids
    private fun ruleKeepSmallDistance(id: Int, dist: Float): Velocity {
        val v = Velocity()

        for (i in 0 until mTotal) {
            if (i != id) {
                if (Math.abs(mBoidList!![id].mPosition.x - mBoidList!![i].mPosition.x) + Math.abs(mBoidList!![id].mPosition.y - mBoidList!![i].mPosition.y) < dist) {
                    v.x -= mBoidList!![i].mPosition.x - mBoidList!![id].mPosition.x
                    v.y -= mBoidList!![i].mPosition.y - mBoidList!![id].mPosition.y
                }
            }
        }

        return v
    }

    // RULE: match velocity with near boids
    private fun ruleMatchNearVelocity(id: Int, factor: Float): Velocity {
        val v = Velocity()

        for (i in 0 until mTotal) {
            if (i != id) {
                v.x += mBoidList!![i].mVelocity.x
                v.y += mBoidList!![i].mVelocity.y
            }
        }

        if (mTotal > 2) {
            v.x /= mTotal - 1
            v.y /= mTotal - 1
        }

        v.x = (v.x - mBoidList!![id].mVelocity.x) * factor
        v.y = (v.y - mBoidList!![id].mVelocity.y) * factor

        return v
    }

    // RULE: consider wind speed
    private fun ruleReactToWind(): Velocity {
        val v = Velocity()

        v.x = 1.0f
        v.y = 0.0f

        return v
    }

    // RULE: tend to a place
    private fun ruleTendToPlace(id: Int, factor: Float): Velocity {
        val v = Velocity()

        v.x = (mPlace!!.x - mBoidList!![id].mPosition.x) * factor
        v.y = (mPlace!!.y - mBoidList!![id].mPosition.y) * factor

        return v
    }

    // RULE: limit the velocity
    private fun ruleLimitVelocity(id: Int, vmax: Float) {

        val vv =
            Math.sqrt((mBoidList!![id].mVelocity.x * mBoidList!![id].mVelocity.x + mBoidList!![id].mVelocity.y * mBoidList!![id].mVelocity.y).toDouble())

        if (vv > vmax) {
            mBoidList[id].mVelocity.x = (mBoidList!![id].mVelocity.x / vv * vmax).toFloat()
            mBoidList!![id].mVelocity.y = (mBoidList!![id].mVelocity.y / vv * vmax).toFloat()
        }
    }

    // RULE: bound the position
    private fun ruleBoundPosition(id: Int, initv: Float) {
        val pad = 10.0f

        if (mBoidList!![id].mPosition.x < pad) {
            mBoidList!![id].mVelocity.x = initv
        } else if (mBoidList!![id].mPosition.x > mWidth - 2.0f * pad) {
            mBoidList!![id].mVelocity.x = -initv
        }

        if (mBoidList!![id].mPosition.y < pad) {
            mBoidList!![id].mVelocity.y = initv
        } else if (mBoidList!![id].mPosition.y > mHeight - 5.0f * pad) {
            mBoidList!![id].mVelocity.y = -initv
        }
    }

    // TODO: scatter the flock b, negating three previous rules

    // TODO: add mPerching status when below the threshold height
}