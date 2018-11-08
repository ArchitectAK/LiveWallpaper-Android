package com.cogitator.livewall

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AlertDialog.Builder(this@MainActivity)
            .setIcon(R.drawable.bats)
            .setTitle("Live Wallpaper Settings")
            .setMessage("All settings should be added here.")
            .setPositiveButton("OK") { _, _ -> finish() }
            .create().show()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}