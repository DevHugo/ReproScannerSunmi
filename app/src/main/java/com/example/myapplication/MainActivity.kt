package com.example.myapplication

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // See documentation at
        // https://github.com/kduma-autoid/capacitor-sunmi-scanhead/blob/main/android/src/main/java/com/sunmi/scanner/config/SunmiHelper.java
        const val EAN8: String = "scan0002"
        const val EAN13: String = "scan0003"
        const val UPC_A: String = "scan0005"
        const val UPC_E: String = "scan0004"
        const val EAN128: String = "scan0011"
        const val CODE39: String = "scan0008"
        const val CODE128: String = "scan0001"
        const val CODABAR: String = "scan0009"
        const val DATABAR: String = "scan0012"
        const val SUFFIX_ENABLED = "000"
        const val SUFFIX_CHECK_CHAR_MODE: String = "009"
        const val SUFFIX_CHECK_CHAR_TYPE: String = "002"

        const val ENABLE_TRIGGER_CONTROL = "com.sunmi.scanner.ACTION_TRIGGER_CONTROL"
        const val ENABLE_TRIGGER_CONTROL_ARG: String = "enable"

        const val SET_ENABLED_BROADCAST: String = "sunmi003001"
        const val SET_OUT_CODE_ACTION: String = "sunmi003006"
        const val SET_OUT_CODE_ACTION_DATA_KEY: String = "sunmi003007"

        const val C_1D_BARCODE: String = "scan9001"
        const val ISBT128: String = "scan0021"
        const val UPC_E1: String = "scan0028"
        const val CODE93: String = "scan0010"
        const val CODE11: String = "scan0015"
        const val ISBN: String = "scan0016"
        const val MSI_PLESSEY: String = "scan0020"
        const val ISSN_EAN: String = "scan0027"
        const val PDF417: String = "scan1001"
        const val QR_CODE: String = "scan1002"
        const val AZTEC: String = "scan1003"
        const val DATA_MATRIX: String = "scan1004"
        const val HANXIN_CODE: String = "scan1005"
        const val INT25: String = "scan0006"
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

    private val symbologiesEnabled = listOf(
        EAN8, EAN13, UPC_A, UPC_E, EAN128, CODE39, CODE128, CODABAR, DATABAR,
    )

    private val symbologiesDisabled = listOf(
        C_1D_BARCODE, ISBT128, UPC_E1, CODE93, CODE11, ISBN, MSI_PLESSEY, ISSN_EAN, PDF417,
        AZTEC, DATA_MATRIX, HANXIN_CODE, QR_CODE, INT25
    )

    fun changeConfiguration(view: View) {
        // Enabled all needed symbologies supported by the app
        val commands = StringBuffer()
        for (symbology in symbologiesEnabled) {
            commands.append("${symbology}$SUFFIX_ENABLED=1;")
        }

        // Enabled broadcast config
        commands.append("$SET_ENABLED_BROADCAST=1;")
        commands.append("$SET_OUT_CODE_ACTION=com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED;")
        commands.append("$SET_OUT_CODE_ACTION_DATA_KEY=data;")

        // Disabled symbologies
        for (symbology in symbologiesDisabled) {
            commands.append("${symbology}$SUFFIX_ENABLED=0;")
        }

        // Enable check digit
        commands.append("scan0004002=1;")
        commands.append("scan0005002=1;")
        commands.append("scan0008002=1;")

        scanInterface?.sendCommand(commands.toString())
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