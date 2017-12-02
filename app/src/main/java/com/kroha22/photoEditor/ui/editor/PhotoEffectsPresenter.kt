package com.kroha22.photoEditor.ui.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.net.Uri
import android.opengl.GLSurfaceView
import android.provider.MediaStore
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.kroha22.photoEditor.photoEffects.*
import com.kroha22.photoEditor.photoEffects.Modify.FLIPHOR
import com.kroha22.photoEditor.photoEffects.Modify.FLIPVERT
import java.io.IOException
import java.util.*
import javax.microedition.khronos.opengles.GL10


/**
 * Created by Olga
 * on 10.11.2017.
 */

@InjectViewState
class PhotoEffectsPresenter : MvpPresenter<PhotoEffectsView>(), PhotoEffectsListener {

    private val texRenderer = TextureRenderer()
    private var textures = IntArray(5)
    private var resultTexture: Int = 0
    private var bitmap: Bitmap? = null

    private var mImageWidth: Int = 0
    private var mImageHeight: Int = 0
    private var mPhotoName: String? = null

    private var mCurrentFilter: Filter? = null
    private val mChangedProperties = ArrayList<Property>()
    private var flip = IntArray(2)

    fun start() {

        texRenderer.init()

        if (bitmap == null) {
            viewState.showPlaceholder()
        } else {
            viewState.showPhoto()
        }
    }

    fun userSelectPhoto(contentResolver: ContentResolver, selectedImage: Uri) {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
            resetAllEffects()
            viewState.showPhoto()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun userSavePhoto(glSurfaceView: GLSurfaceView, gl: GL10, contentResolver: ContentResolver) {
        val resultMsg = PhotoUtils.savePhoto(gl, contentResolver, glSurfaceView, mPhotoName!!, mImageHeight, mImageWidth)
        viewState.showToast(resultMsg)
    }

    fun initTextures() {
        val photo = bitmap
        if (photo != null) {
            texRenderer.init()

            PhotoUtils.loadPhoto(photo, textures, texRenderer)

            mImageHeight = photo.height
            mImageWidth = photo.width
            setResultTexture(0)
        }
    }

    fun userCreateEffect(effectContext: EffectContext) {
        var effect: Effect
        val effects: List<Effect>
        var resultTexture = 0

        //apply flip
        if (flip[0] % 2 == 1) {
            effect = PhotoUtils.EffectCreator.createEffect(effectContext, FLIPHOR)
            applyEffect(resultTexture, ++resultTexture, effect, mImageHeight, mImageWidth)
            releaseEffect(effect)
        }

        if (flip[1] % 2 == 1) {
            effect = PhotoUtils.EffectCreator.createEffect(effectContext, FLIPVERT)
            applyEffect(resultTexture, ++resultTexture, effect, mImageHeight, mImageWidth)
            releaseEffect(effect)
        }

        //apply properties new value
        if (mChangedProperties.size != 0) {
            effects = PhotoUtils.EffectCreator.createEffects(effectContext, mChangedProperties)
            applyEffect(resultTexture, ++resultTexture, effects[0], mImageHeight, mImageWidth) //apply first effect
            var sourceTexture: Int
            var destinationTexture: Int
            val textures = IntArray(2)
            textures[0] = resultTexture
            textures[1] = resultTexture + 1
            releaseEffect(effects[0])

            for (n in 1 until effects.size) {
                sourceTexture = textures[0]
                destinationTexture = textures[1]
                //apply next effect
                applyEffect(sourceTexture, destinationTexture, effects[n], mImageHeight, mImageWidth)
                //save textures
                setTexture(resultTexture, destinationTexture + 1)
                setTexture(resultTexture + 1, sourceTexture + 1)
                textures[0] = destinationTexture
                textures[1] = sourceTexture
                releaseEffect(effects[n])
            }
        }

        //apply filters
        if (mCurrentFilter != null && mCurrentFilter != Filter.NONE) {
            effect = PhotoUtils.EffectCreator.createEffect(effectContext, mCurrentFilter!!)
            applyEffect(resultTexture, ++resultTexture, effect, mImageHeight, mImageWidth)
            releaseEffect(effect)
        }
        setResultTexture(resultTexture)

        renderResult(resultTexture)
    }

    fun userEnterPhotoName(name: String) {
        mPhotoName = name
        viewState.savePhoto()
    }

    fun updateViewSize(width: Int, height: Int) {
        val photo = bitmap
        if (photo != null) {
            texRenderer.updateViewSize(width, height)
        }
    }

    override fun onDestroy() {
        texRenderer.tearDown()
    }

    private fun resetAllEffects() {
        flip[0] = 0
        flip[1] = 0
        mChangedProperties.clear()
        mCurrentFilter = Filter.NONE
    }

    private fun releaseEffect(effect: Effect?) {
        effect?.release()
    }

    override fun setFilter(filter: Filter) {
        mCurrentFilter = filter
        viewState.applyEffects()
    }

    override fun setProperty(property: Property) {
        if (mChangedProperties.size != 0) {
            for (i in mChangedProperties.indices) {
                if (mChangedProperties[i].name == property.name) {
                    mChangedProperties[i].currentValue = property.currentValue
                } else {
                    mChangedProperties.add(property)
                }
            }
        } else {
            mChangedProperties.add(property)
        }
        viewState.applyEffects()
    }

    override fun setModify(modify: Modify) {
        when (modify) {
            FLIPVERT -> ++flip[1]

            FLIPHOR -> ++flip[0]
        }
        viewState.applyEffects()
    }

    private fun renderResult(number: Int) {
        texRenderer.renderTexture(textures[number])
    }

    private fun applyEffect(sourceTexture: Int, destinationTexture: Int, effect: Effect, height: Int, width: Int) {
        effect.apply(textures[sourceTexture], width, height, textures[destinationTexture])
    }

    private fun setTexture(number: Int, data: Int) {
        textures[number] = data
    }

    private fun setResultTexture(texture: Int) {
        resultTexture = texture
    }


}
