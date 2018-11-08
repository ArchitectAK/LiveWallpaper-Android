package com.cogitator.livewall

/**
 * @author Ankit Kumar on 08/11/2018
 */
class Boid constructor(s: Int) {
    var mPosition: Position = Position()
    var mVelocity: Velocity = Velocity()
    var mPerching = false
    var mState = 0
    var mStates = 4

    init {
        mStates = s
        mState = (mStates * Math.random()).toInt()
    }

    fun changeState() {
        mState = (mState + 1) % mStates
    }
}

class Position {
    var x = 0f
    var y = 0f
}

class Velocity {
    var x = 0f
    var y = 0f
}