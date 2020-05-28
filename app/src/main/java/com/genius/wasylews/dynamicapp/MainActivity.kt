package com.genius.wasylews.dynamicapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        width = 200
        height = 200
    }
    private var maxX = 0;
    private var maxY = 0;
    private lateinit var cameraView: CameraPreviewView
    private val videoViewManager: VideoViewManager = VideoViewManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layout_parent.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (layout_parent.viewTreeObserver.isAlive) {
                    layout_parent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    maxX =  layout_parent.measuredWidth
                    maxY = layout_parent.measuredHeight

                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(
                        Manifest.permission.CAMERA
                    ), REQUEST_CODE)
                }
            }
        })

        layout_parent.setOnTouchListener { _, motionEvent ->
            if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                cameraView.moveToAnimated(motionEvent.x, motionEvent.y)
            }
            return@setOnTouchListener true
        }

        btn_add.setOnClickListener {
            val view = makeVideoView()
            layout_parent.addView(view, layoutParams)
        }
    }

    private fun initView() {
        videoViewManager.init()
        for (i in 0..10) {
            val view = makeVideoView()
            layout_parent.addView(view, layoutParams)
        }

        videoViewManager.play(URL)

        cameraView = makeCameraView()
        layout_parent.addView(cameraView, layoutParams)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initView()
        }
    }

    private fun makeVideoView(): VideoView {
        val view = videoViewManager.addView()
        view.x = Random.nextInt(maxX).toFloat()
        view.y = Random.nextInt(maxY).toFloat()

        return view
    }

    private fun makeCameraView(): CameraPreviewView {
        val view = CameraPreviewView(this)
        view.x = Random.nextInt(maxX).toFloat()
        view.y = Random.nextInt(maxY).toFloat()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        videoViewManager.release()
    }

    companion object {
        const val REQUEST_CODE = 0
        const val URL = "https://i.giphy.com/media/yPRo73ILrGjny/giphy.mp4"
    }
}
