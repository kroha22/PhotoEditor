package com.kroha22.photoEditor.ui.editor

import android.graphics.Bitmap
import android.media.effect.Effect
import android.media.effect.EffectContext
import com.kroha22.photoEditor.photoEffects.*
import java.util.*

/**
 * Created by Olga
 * on 10.11.2017.
 */
class PhotoEffectsMaker {

    private val texRenderer: TextureRenderer
    private var textures: IntArray
    private var resultTexture: Int

    private val changedProperties: ArrayList<Property>
    private var currentProperty: Property? = null
    private var currentFilter: Filter? = null
    private var flip = IntArray(2)

    private var imageWidth: Int = 0
    private var imageHeight: Int = 0

    init {
        currentFilter = Filter.NONE
        currentProperty = null
        changedProperties = ArrayList()
        texRenderer = TextureRenderer()
        textures = IntArray(5)
        resultTexture = 0
    }

    fun reset() {
        flip[0] = 0
        flip[1] = 0
        changedProperties.clear()
        currentFilter = Filter.NONE
    }

    fun start() {
        texRenderer.init()
    }

    fun close() {
        texRenderer.tearDown()
    }

    fun setFlip(modify: Modify) {
        when (modify) {
            Modify.FLIPVERT -> ++flip[1]

            Modify.FLIPHOR -> ++flip[0]
        }
    }

    fun setProperty(property: Property): Boolean {
        if (currentProperty != property) {
            currentProperty = property
            return true
        }
        return false
    }

    fun resetProperty() {
        currentProperty = null
    }

    fun setFilter(filter: Filter): Boolean {
        if (currentFilter != filter) {
            currentFilter = filter
            return true
        }
        return false
    }

    fun getCurrentFilter(): Filter? {
        return currentFilter
    }

    fun changePropertiesValue(property: Property) {
        if (changedProperties.size != 0) {
            for (i in changedProperties.indices) {
                if (changedProperties[i].name == property.name) {
                    changedProperties[i].currentValue = property.currentValue
                } else {
                    changedProperties.add(property)
                }
            }
        } else {
            changedProperties.add(property)
        }
    }

    fun initPhoto(photo: Bitmap) {
        texRenderer.init()

        PhotoUtils.loadPhoto(photo, textures, texRenderer)
        imageHeight = photo.width
        imageWidth = photo.height
        setResultTexture(0)
    }

    fun updateViewSize(width: Int, height: Int) {
        texRenderer.updateViewSize(width, height)
    }

    fun applyEffects(effectContext: EffectContext) {
        var effect: Effect
        val effects: List<Effect>
        var resultTexture = 0

        //apply flip
        if (flip[0] % 2 == 1) {
            effect = PhotoUtils.EffectCreator.createEffect(effectContext, Modify.FLIPHOR)
            applyEffect(resultTexture, ++resultTexture, effect, imageHeight, imageWidth)
            releaseEffect(effect)
        }

        if (flip[1] % 2 == 1) {
            effect = PhotoUtils.EffectCreator.createEffect(effectContext, Modify.FLIPVERT)
            applyEffect(resultTexture, ++resultTexture, effect, imageHeight, imageWidth)
            releaseEffect(effect)
        }

        //apply properties new value
        if (changedProperties.size != 0) {
            effects = PhotoUtils.EffectCreator.createEffects(effectContext, changedProperties)
            applyEffect(resultTexture, ++resultTexture, effects[0], imageHeight, imageWidth) //apply first effect
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
                applyEffect(sourceTexture, destinationTexture, effects[n], imageHeight, imageWidth)
                //save textures
                setTexture(resultTexture, destinationTexture + 1)
                setTexture(resultTexture + 1, sourceTexture + 1)
                textures[0] = destinationTexture
                textures[1] = sourceTexture
                releaseEffect(effects[n])
            }
        }

        //apply filters
        if (currentFilter != null && currentFilter != Filter.NONE) {
            effect = PhotoUtils.EffectCreator.createEffect(effectContext, currentFilter!!)
            applyEffect(resultTexture, ++resultTexture, effect, imageHeight, imageWidth)
            releaseEffect(effect)
        }

        setResultTexture(resultTexture)

        renderResult(resultTexture)
    }

    private fun applyEffect(sourceTexture: Int, destinationTexture: Int, effect: Effect, height: Int, width: Int) {
        effect.apply(textures[sourceTexture], width, height, textures[destinationTexture])
    }

    private fun releaseEffect(effect: Effect?) {
        effect?.release()
    }

    private fun setResultTexture(texture: Int) {
        resultTexture = texture
    }

    private fun renderResult(number: Int) {
        texRenderer.renderTexture(textures[number])
    }

    private fun setTexture(number: Int, data: Int) {
        textures[number] = data
    }

}