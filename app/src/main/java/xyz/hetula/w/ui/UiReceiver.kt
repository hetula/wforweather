package xyz.hetula.w.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.lang.ref.WeakReference

abstract class UiReceiver<T>(instance: T) : BroadcastReceiver() {
    private val mInstanceRef = WeakReference(instance)
    private var mRegistered: Boolean = false
    private lateinit var mIntentFilter: IntentFilter

    abstract fun intentFilter(): IntentFilter

    abstract fun onReceiveIntent(context: Context, intent: Intent, instance: T)

    fun register(context: Context) {
        if (!mRegistered) {
            mRegistered = true
            mIntentFilter = intentFilter()
            LocalBroadcastManager.getInstance(context).registerReceiver(this, mIntentFilter)
        }
    }

    fun unregister(context: Context) {
        if (mRegistered) {
            mRegistered = false
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val instance = mInstanceRef.get()
        if (instance == null) {
            Log.e(TAG, "Holding instance has died!")
        } else if (context == null) {
            Log.w(TAG, "Received context is null!")
        } else if (intent == null) {
            Log.w(TAG, "Received intent is null!")
        } else if (!mIntentFilter.matchAction(intent.action)) {
            Log.w(TAG, "Intent with wrong action received: $intent")
        } else {
            onReceiveIntent(context, intent, instance)
        }
    }

    companion object {
        private const val TAG = "UiReceiver"
    }
}
