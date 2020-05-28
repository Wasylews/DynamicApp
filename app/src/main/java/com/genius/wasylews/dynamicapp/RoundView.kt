package com.genius.wasylews.dynamicapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLES20
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewOutlineProvider


abstract class RoundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {
    init {
        outlineProvider = object: ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val rect = Rect(0, 0, view.measuredWidth, view.measuredHeight)
                outline.setRoundRect(rect, view.measuredWidth / 2f)
            }

        }
        clipToOutline = true
    }
}

class CameraPreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RoundView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private lateinit var cameraDevice: CameraDevice

    init {
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {}

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {}

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        cameraDevice.close()
        return true
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture?, p1: Int, p2: Int) {
        initCamera(Surface(texture))
    }

    fun moveToAnimated(x: Float, y: Float) {
        val newX = x - this.width / 2
        val newY = y - this.height / 2

        animate().x(newX)
            .y(newY)
            .setDuration(1000)
            .start()
    }

    @SuppressLint("MissingPermission")
    private fun initCamera(surface: Surface) {
        cameraManager.openCamera(cameraManager.cameraIdList[1], object :
            CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera

                camera.createCaptureSession(
                    listOf(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(captureSession: CameraCaptureSession) {

                        }

                        override fun onConfigured(captureSession: CameraCaptureSession) {
                            val request =
                                camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                                    addTarget(surface)
                                }.build()
                            captureSession.setRepeatingRequest(request, null, null)
                        }
                    },
                    null
                )
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, p1: Int) {
            }

        }, null)
    }
}