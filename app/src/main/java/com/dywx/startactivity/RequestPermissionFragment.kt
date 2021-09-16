package com.dywx.startactivity

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * @author yangmingcan@dayuwuxian.com
 * @date 2021/9/7
 */
class RequestPermissionFragment : Fragment() {

    companion object {
        const val TAG = "com.dywx.larkplayer.RequestPermissionFragment"
        const val REQUEST_CODE = "request_code"
        const val KEY_PERMISSIONS = "permissions"
    }

    var onSuccess: () -> Unit = {}
    var onFail: () -> Unit = {}

    private var code = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        code = arguments?.getInt(REQUEST_CODE, -1) ?: -1
        val permissions = arguments?.getStringArray(KEY_PERMISSIONS)?.let {
            try {
                requestPermissions(it, code)
            } catch (e: Exception) {
                onFail.invoke()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == code && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onSuccess.invoke()
        } else {
            onFail.invoke()
        }
    }

}

private fun AppCompatActivity.internalRequestPermissions(
    bundle: Bundle,
    onSuccess: () -> Unit = {},
    onFail: () -> Unit = {}
) {
    val fragment =
        supportFragmentManager.findFragmentByTag(RequestPermissionFragment.TAG) as RequestPermissionFragment?
            ?: RequestPermissionFragment()
    fragment.arguments = bundle
    fragment.onSuccess = onSuccess
    fragment.onFail = onFail
    supportFragmentManager.beginTransaction().apply {
        if (fragment.isAdded) {
            remove(fragment)
        }
        add(fragment, RequestPermissionFragment.TAG)
    }.commitNowAllowingStateLoss()
}

fun AppCompatActivity.requestPermissions(
    code: Int,
    permissions: Array<out String>,
    onSuccess: () -> Unit = {},
    onFail: () -> Unit = {}
) {
    val bundle = Bundle().apply {
        putInt(RequestPermissionFragment.REQUEST_CODE, code)
        putStringArray(RequestPermissionFragment.KEY_PERMISSIONS, permissions)
    }
    this.internalRequestPermissions(bundle, onSuccess, onFail)
}

suspend fun AppCompatActivity.suspendRequestPermission(
    code: Int,
    permissions: Array<out String>
): Boolean {
    val bundle = Bundle().apply {
        putInt(RequestPermissionFragment.REQUEST_CODE, code)
        putStringArray(RequestPermissionFragment.KEY_PERMISSIONS, permissions)
    }
    return withContext(Dispatchers.Main) {
        val result = suspendCancellableCoroutine<Boolean> { continuation ->
            this@suspendRequestPermission.internalRequestPermissions(bundle,
                onSuccess = {
                    continuation.resume(true, null)
                }, onFail = {
                    continuation.resume(false, null)
                })
        }
        result
    }
}