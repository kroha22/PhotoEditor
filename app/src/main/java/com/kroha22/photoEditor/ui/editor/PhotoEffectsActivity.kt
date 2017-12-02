package com.kroha22.photoEditor.ui.editor

import android.media.effect.EffectContext
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created by Olga
 * on 10.11.2017.
 */
//---------------------------------------------------------------------------------------------
@StateStrategyType(AddToEndSingleStrategy::class)
interface PhotoEffectsView : MvpView {

    fun hidePlaceholder()

    fun showPlaceholder()

    fun showPhoto()

    fun savePhoto()

    fun applyEffects()

    fun showToast(message: String)

}
//---------------------------------------------------------------------------------------------
abstract class PhotoEffectsActivity : MvpAppCompatActivity(), GLSurfaceView.Renderer, PhotoEffectsView {

    private var effectContext: EffectContext? = null
    private var needSave = false
    private var needInit = true

    private lateinit var effectView: GLSurfaceView

    @InjectPresenter
    lateinit var presenter: PhotoEffectsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        effectView = createSurfaceView()
        initEffectView()
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onPause() {
        super.onPause()
        effectView.onPause()
    }

    override fun onResume() {
        super.onResume()
        effectView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onDrawFrame(gl: GL10) {
        if (needInit) {
            effectContext = EffectContext.createWithCurrentGlContext()
            presenter.initTextures()
            needInit = false
        }

        presenter.userCreateEffect(effectContext!!)

        if (needSave) {
            presenter.userSavePhoto(effectView, gl, contentResolver)
            needSave = false
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        presenter.updateViewSize(width, height)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {/**/}

    override fun showPhoto() {
        hidePlaceholder()
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

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun selectPhoto(selectedImage: Uri) {
        presenter.userSelectPhoto(contentResolver, selectedImage)
    }

    fun setPhotoName(string: String) {
        presenter.userEnterPhotoName(string)
    }

    fun initEffectView() {
        effectView.setEGLContextClientVersion(2)
        effectView.setRenderer(this)
        effectView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    internal abstract fun createSurfaceView(): GLSurfaceView

}

