package com.kroha22.photoEditor.ui.editor

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.effect.EffectContext
import android.opengl.GLSurfaceView
import android.os.Build
import android.view.View
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by Olga
 * on 10.11.2017.
 */
//---------------------------------------------------------------------------------------------
interface PhotoEditor {

    fun initPhoto()

    fun applyPhotoEffects(effectContext: EffectContext)

    fun updatePhoto(width: Int, height: Int)

    fun savePhoto(glSurfaceView: GLSurfaceView, gl: GL10, contentResolver: ContentResolver)
}
//---------------------------------------------------------------------------------------------
interface PhotoViewContainer {

    fun onPause()

    fun onResume()

    fun showPhoto(photo: Bitmap)

    fun savePhoto()

    fun applyEffects()
}
//---------------------------------------------------------------------------------------------
class PhotoContainer(context: Context,
                     private val photoEditor: PhotoEditor,
                     private val contentResolver: ContentResolver) : GLSurfaceView.Renderer, PhotoViewContainer {

    private val effectView: GLSurfaceView = GLSurfaceView(context)
    private var effectContext: EffectContext? = null

    private var needSave = false
    private var needInit = true

    private var photo: Bitmap?

    init {
        if (isProbablyEmulator()) {
            // Avoids crashes on startup with some emulator images.
            effectView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        }
        effectView.setEGLContextClientVersion(2)
        effectView.setRenderer(this)
        effectView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        photo = null
    }

    fun getView(): View {
        return effectView
    }

    override fun onPause() {
        effectView.onPause()
    }

    override fun onResume() {
        effectView.onResume()
    }

    override fun showPhoto(photo: Bitmap) {
        this.photo = photo
        needInit = true
        effectView.requestRender()
    }

    override fun savePhoto() {
        needSave = true
        effectView.requestRender()
    }

    override fun applyEffects() {
        effectView.requestRender()
    }

    override fun onDrawFrame(gl: GL10) {
        if (needInit) {
            effectContext = EffectContext.createWithCurrentGlContext()
            photoEditor.initPhoto()
            needInit = false
        }

        photoEditor.applyPhotoEffects(effectContext!!)

        if (needSave) {
            photoEditor.savePhoto(effectView, gl, contentResolver)
            needSave = false
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        photoEditor.updatePhoto(width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {/**/}

    private fun isProbablyEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))
    }
}