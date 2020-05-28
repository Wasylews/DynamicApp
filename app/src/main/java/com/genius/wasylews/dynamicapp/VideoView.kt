package com.genius.wasylews.dynamicapp

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLES20
import android.opengl.Matrix
import android.view.Surface
import android.view.TextureView
import com.genius.wasylews.dynamicapp.gles.*


class VideoViewManager(private val context: Context) {
    private val views = mutableListOf<VideoView>()

    private lateinit var eglCore: EglCore
    private lateinit var playerSurface: OffscreenSurface
    private lateinit var rect: FullFrameRect
    private var textureId: Int = 0
    private lateinit var surfaceTexture: SurfaceTexture

    private lateinit var player: MediaPlayer

    fun init() {
        eglCore = EglCore()
        playerSurface = OffscreenSurface(eglCore, 200, 200).apply {
            makeCurrent()
        }

        rect = FullFrameRect(Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT))
        textureId = rect.createTextureObject()
        surfaceTexture = SurfaceTexture(textureId).apply {
            setOnFrameAvailableListener {
                playerSurface.makeCurrent()
                updateTexImage()

                for (view in views) {
                    view.draw(this, rect, textureId)
                }
            }
        }

        player = MediaPlayer().apply {
            isLooping = true
            setSurface(Surface(surfaceTexture))
        }
    }

    fun addView(): VideoView {
        val view = VideoView(context, eglCore)
        views.add(view)
        return view
    }

    fun play(url: String) {
        player.setDataSource(context, Uri.parse(url))
        player.prepare()
        player.start()
    }

    fun release() {
        playerSurface.release()
        for (view in views) {
            view.release()
        }
        eglCore.release()
        player.release()
        rect.release(false)
        surfaceTexture.release()
    }
}

class VideoView(context: Context, private val eglCore: EglCore) : RoundView(context),
    TextureView.SurfaceTextureListener {

    private lateinit var surface: WindowSurface
    private val identityMatrix = FloatArray(16).apply {
        Matrix.setIdentityM(this, 0)
    }

    init {
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {

    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        return true
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture?, p1: Int, p2: Int) {
        surface = WindowSurface(eglCore, texture)
    }

    fun draw(input: SurfaceTexture, rect: FullFrameRect, textureId: Int) {
        if (isAvailable) {
            surface.makeCurrent()
            input.getTransformMatrix(identityMatrix)
            GLES20.glViewport(0, 0, 200, 200)
            rect.drawFrame(textureId, identityMatrix)
            surface.swapBuffers()
        }
    }

    fun release() {
        surface.release()
    }
}