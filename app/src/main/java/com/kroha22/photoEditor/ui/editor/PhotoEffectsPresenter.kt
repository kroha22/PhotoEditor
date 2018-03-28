package com.kroha22.photoEditor.ui.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.media.effect.EffectContext
import android.net.Uri
import android.opengl.GLSurfaceView
import android.provider.MediaStore
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.kroha22.photoEditor.photoEffects.Filter
import com.kroha22.photoEditor.photoEffects.Modify
import com.kroha22.photoEditor.photoEffects.PhotoUtils
import com.kroha22.photoEditor.photoEffects.Property
import javax.microedition.khronos.opengles.GL10


/**
 * Created by Olga
 * on 10.11.2017.
 */
@InjectViewState
class PhotoEffectsPresenter : MvpPresenter<PhotoEffectsView>(), PhotoEditor {

    private val photoEffectsMaker: PhotoEffectsMaker = PhotoEffectsMaker()

    private var photo: Bitmap? = null
    private var photoName: String? = null

    private lateinit var filters: Array<Filter>
    private lateinit var properties: Array<Property>

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        filters = Filter.values()
        properties = Property.values()
    }

    override fun onDestroy() {
        photoEffectsMaker.close()
    }

    override fun initPhoto() {
        val photo = photo
        if (photo != null) {
            photoEffectsMaker.initPhoto(photo)
        }
    }

    override fun applyPhotoEffects(effectContext: EffectContext) {
        photoEffectsMaker.applyEffects(effectContext)
    }

    override fun updatePhoto(width: Int, height: Int) {
        val photo = photo
        if (photo != null) {
            photoEffectsMaker.updateViewSize(width, height)
        }
    }

    override fun savePhoto(glSurfaceView: GLSurfaceView,
                           gl: GL10,
                           contentResolver: ContentResolver,
                           height: Int,
                           width: Int) {
        val resultMsg = PhotoUtils.savePhoto(gl, contentResolver, glSurfaceView, photoName!!, height, width)
        viewState.showToast(resultMsg)
    }

    fun start() {
        val photo = this.photo
        if (photo == null) {
            photoEffectsMaker.start()
            viewState.showPlaceholder()
        } else {
            viewState.showPhoto(photo)
        }
    }

    fun userEnterPhotoName(name: String) {
        photoName = name
        viewState.savePhoto()
    }

    fun userSelectPhoto(contentResolver: ContentResolver, selectedImage: Uri) {
        photo = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
        photoEffectsMaker.reset()

        viewState.hidePlaceholder()
        viewState.showPhoto(photo!!)
        showProperties()
    }

    fun userResetProperties() {
        properties.forEach { it.clear() }
    }

    fun userSelectProperties() {
        showProperties()
    }

    fun userSelectFilters() {
        viewState.hidePropertyDetail()
        viewState.showFilters(filters)
    }

    fun userSetFlip(modify: Modify) {
        photoEffectsMaker.setFlip(modify)
        viewState.applyEffects()
    }

    fun userCheckProperty(property: Property) {
        if (photoEffectsMaker.setProperty(property)) {
            viewState.showPropertyDetail(property)
        } else {
            photoEffectsMaker.resetProperty()
            viewState.hidePropertyDetail()
        }
    }

    fun userCheckFilter(filter: Filter) {
        if (photoEffectsMaker.setFilter(filter)) {
            viewState.highlightFilter(photoEffectsMaker.getCurrentFilter()!!, Color.TRANSPARENT)
            viewState.highlightFilter(filter, Color.WHITE)
            viewState.applyEffects()
        }
    }

    fun userChangePropertiesValue(property: Property) {
        photoEffectsMaker.changePropertiesValue(property)
        viewState.applyEffects()
    }

    fun userResetFilter() {
        photoEffectsMaker.setFilter(Filter.NONE)
    }

    private fun showProperties() {
        viewState.hidePropertyDetail()
        viewState.showProperties(properties)
    }

}
