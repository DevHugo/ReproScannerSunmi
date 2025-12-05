package com.example.myapplication

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.myapplication.databinding.ActivityMainBinding
import com.sunmi.scanner.IScanInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Scan-Test"
        const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"
        const val DATA = "data"
        const val SOURCE = "source_byte"
        const val NONE = 100
    }

    private lateinit var mBinding: ActivityMainBinding
    private var mHandler = Handler(Looper.getMainLooper())
    private var scanInterface: IScanInterface? = null
    private val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_DATA_CODE_RECEIVED -> {
                    val code = intent.getStringExtra(DATA)
                    val source = intent.getByteArrayExtra(SOURCE)
                    Log.d(TAG, "onReceive: $code")
                    Toast.makeText(context,"广播接收扫码内容："+code.toString(),Toast.LENGTH_LONG).show()
                }

                else -> {
                }
            }
        }
    }

    private val conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            scanInterface = IScanInterface.Stub.asInterface(service)
            Log.i(TAG, "Scanner Service Connected!")
            println("Test chen onServiceConnected")

        }

        @SuppressLint("CheckResult")
        override fun onServiceDisconnected(name: ComponentName) {
            Log.e(TAG, "Scanner Service Disconnected!")
            scanInterface = null
        }
    }

    private fun bindScannerService() {
        println("Test chen bindScannerService 0000")
        if (scanInterface == null) {
            println("Test chen bindScannerService 1111")
            val intent = Intent()
            intent.setPackage("com.sunmi.scanner")
            intent.action = "com.sunmi.scanner.IScanInterface"
            startService(intent)
            bindService(intent, conn, BIND_AUTO_CREATE)
        }
    }

    private fun unbindScannerService() {
        mHandler.removeCallbacksAndMessages(null)
        try {
            if (scanInterface != null) {
                unbindService(conn)
                scanInterface = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun doScanInterface(runnable: Runnable) {
        if (scanInterface == null) {
            bindScannerService()
            mHandler.postDelayed(object : Runnable {
                override fun run() {
                    if (scanInterface != null) {
                        runnable.run()
                    } else {
                        mHandler.postDelayed(this, 200)
                    }
                }
            }, 200)
        } else {
            runnable.run()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindScannerService()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        println("Test chen onResume onResume()")
        registerReceiver(br, IntentFilter(ACTION_DATA_CODE_RECEIVED))
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver(br)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        GlobalScope.launch(Dispatchers.IO) {
            scanInterface?.scan()
            scanInterface?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindScannerService()
    }

    fun onScan(view: View) {
        GlobalScope.launch(Dispatchers.IO) {
            scanInterface?.scan()
        }
        /*doScanInterface(object : Runnable {
            override fun run() {
                if (scanInterface != null) {
                    scanInterface?.scan()
                } else {
                    mHandler.postDelayed(this, 200)
                }
            }
        })*/
    }

    fun onStop(view: View) {
        GlobalScope.launch(Dispatchers.IO) {
            scanInterface?.stop()
        }
        /*doScanInterface(object : Runnable {
            override fun run() {
                if (scanInterface != null) {
                    scanInterface?.stop()
                } else {
                    mHandler.postDelayed(this, 200)
                }
            }
        })*/
    }
}