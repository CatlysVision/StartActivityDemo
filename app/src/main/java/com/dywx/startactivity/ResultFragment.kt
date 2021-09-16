package com.dywx.startactivity

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * @author yangmingcan@dayuwuxian.com
 * @date 2021/9/7
 */
class ResultFragment : Fragment() {

    companion object {
        const val TAG = "com.dywx.larkplayer.ResultFragment"
        const val REQUEST_CODE = "request_code"
        const val KEY_INTENT = "intent"
    }

    var onSuccess: (Uri?) -> Unit = {}
    var onFail: () -> Unit = {}
    var targetClazz: Class<out Any>? = null

    private var code = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        code = arguments?.getInt(REQUEST_CODE, -1) ?: -1
        val intent = arguments?.getParcelable<Intent>(KEY_INTENT)
        try {
            startActivityForResult(intent, code)
        } catch (e: Exception) {
            onFail.invoke()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == code) {
            if (resultCode == Activity.RESULT_OK) {
                onSuccess.invoke(data?.data)
            } else {
                onFail.invoke()
            }
        }
    }

}

private fun AppCompatActivity.internalStartActivityForResult(
    clazz: Class<out Any>? = null,
    bundle: Bundle,
    onSuccess: (Uri?) -> Unit = {},
    onFail: () -> Unit = {}
) {
    val fragment = supportFragmentManager.findFragmentByTag(ResultFragment.TAG) as ResultFragment?
        ?: ResultFragment()
    fragment.targetClazz = clazz
    fragment.arguments = bundle
    fragment.onSuccess = onSuccess
    fragment.onFail = onFail
    supportFragmentManager.beginTransaction().apply {
        if (fragment.isAdded) {
            remove(fragment)
        }
        add(fragment, ResultFragment.TAG)
    }.commitNowAllowingStateLoss()
}

fun AppCompatActivity.startForResultWithIntent(
    code: Int,
    intent: Intent,
    onResult: (Response) -> Unit = { }
) {

    val bundle = Bundle().apply {
        putInt(ResultFragment.REQUEST_CODE, code)
        putParcelable(ResultFragment.KEY_INTENT, intent)
    }
    this.internalStartActivityForResult(null, bundle, onSuccess = {
        onResult.invoke(Response(it))
    }, onFail = {
        onResult.invoke(Response(null))
    })
}

suspend fun AppCompatActivity.suspendStartForResultWithIntent(
    code: Int,
    intent: Intent
): Response {
    val bundle = Bundle().apply {
        putInt(ResultFragment.REQUEST_CODE, code)
        putParcelable(ResultFragment.KEY_INTENT, intent)
    }
    return withContext(Dispatchers.Main) {
        val result = suspendCancellableCoroutine<Response> { continuation ->
            this@suspendStartForResultWithIntent.internalStartActivityForResult(null, bundle,
                onSuccess = {
                    continuation.resume(Response(it), null)
                }, onFail = {
                    continuation.resume(Response(null), null)
                })
        }
        result
    }
}