package com.dywx.startactivity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private val TAG = "MainActivity"
  private val ALBUM_REQUEST_CODE = 101
  private val PERMISSIONS = 102

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    findViewById<AppCompatButton>(R.id.btn_test1).setOnClickListener {
      callback()
    }
    findViewById<AppCompatButton>(R.id.btn_test2).setOnClickListener {
      coroutine()
    }
    findViewById<AppCompatButton>(R.id.btn_test3).setOnClickListener {
      api()
    }
    findViewById<AppCompatButton>(R.id.btn_test4).setOnClickListener {
      permissions()
    }
    findViewById<AppCompatButton>(R.id.btn_test5).setOnClickListener {
      suspendPermissions()
    }
  }

  private fun callback() {
    val gotoAlbumIntent = Intent(Intent.ACTION_PICK)
    gotoAlbumIntent.type = "image/*"
    startForResultWithIntent(
      ALBUM_REQUEST_CODE,
      gotoAlbumIntent,
      onResult = {
        Log.d(TAG, "uri=" + it.uri)
      }
    )
  }

  private fun coroutine() {
    val gotoAlbumIntent = Intent(Intent.ACTION_PICK)
    gotoAlbumIntent.type = "image/*"
    lifecycleScope.launch {
      val response = suspendStartForResultWithIntent(
        ALBUM_REQUEST_CODE,
        gotoAlbumIntent,
      )
      Log.d(TAG, "uri=" + response.uri)
    }
  }

  private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
    Log.d(TAG, "uri=$it")
  }

  private fun api() {
    getContent.launch("image/*")
  }

  private fun permissions() {
    val permissions = arrayOf(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    requestPermissions(PERMISSIONS, permissions, onSuccess = {
      Log.d(TAG, "success")
    })
  }

  private fun suspendPermissions() {
    val permissions = arrayOf(
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    lifecycleScope.launch {
      val result = suspendRequestPermission(PERMISSIONS, permissions)
      Log.d(TAG, "result=" + result)
    }
  }
}