package com.kroha22.photoEditor.photoEffects

import android.media.effect.EffectFactory
import com.kroha22.photoEditor.R

/**
 * Created by Olga
 * on 08.10.2017.
 */
//--------------------------------------------------------------------------------------------

enum class EffectParameter(val parameterName: String) {

    SCALE("scale"),
    STRENGTH("strength"),
    WHITE("white"),
    BLACK("black"),
    CONTRAST("contrast"),
    BRIGHTNESS("brightness"),
    VERTICAL("vertical"),
    HORIZONTAL("horizontal")

}
//--------------------------------------------------------------------------------------------

enum class Modify(val effectName: String) {
    FLIPVERT(EffectFactory.EFFECT_FLIP),
    FLIPHOR(EffectFactory.EFFECT_FLIP)
}
//--------------------------------------------------------------------------------------------

enum class Filter(val filterName: String,
                  val effectName: String?,
                  val iconId: Int) {
    NONE("Без фильтра", null, R.drawable.img_none),
    CROSSPROCESS("Пленка", EffectFactory.EFFECT_CROSSPROCESS, R.drawable.img_crossprocess),
    DOCUMENTARY("Документальный", EffectFactory.EFFECT_DOCUMENTARY, R.drawable.img_documentary),
    GRAYSCALE("Оттенки серого", EffectFactory.EFFECT_GRAYSCALE, R.drawable.img_grayscale),
    LOMOISH("Ломография", EffectFactory.EFFECT_LOMOISH, R.drawable.img_lomoish),
    NEGATIVE("Негатив", EffectFactory.EFFECT_NEGATIVE, R.drawable.img_negative),
    POSTERIZE("Постеризация", EffectFactory.EFFECT_POSTERIZE, R.drawable.img_posterize),
    SEPIA("Сепия", EffectFactory.EFFECT_SEPIA, R.drawable.img_sepia)
}

//--------------------------------------------------------------------------------------------
enum class Property(val propertyName: String,
                    val effectName: String,
                    private val mMinValue: Float,
                    private val mMaxValue: Float,
                    private val defaultValue: Float,
                    var currentValue: Float,
                    val iconId: Int) {

    BRIGHTNESS("Яркость", EffectFactory.EFFECT_BRIGHTNESS, 1.0f, 2.0f, 1.0f, 1.0f, R.drawable.ic_brightness),
    CONTRAST("Контрастность", EffectFactory.EFFECT_CONTRAST, 1.0f, 2.0f, 1.0f, 1.0f, R.drawable.ic_contrast),
    SATURATE("Насыщенность", EffectFactory.EFFECT_SATURATE, -1.0f, 1.0f, 0.0f, 0.0f, R.drawable.ic_saturate),
    SHARPEN("Резкость", EffectFactory.EFFECT_SHARPEN, 0.0f, 1.0f, 0.0f, 0.0f, R.drawable.ic_sharpen),
    AUTOFIX("Автокоррекция", EffectFactory.EFFECT_AUTOFIX, 0.0f, 1.0f, 0.0f, 0.0f, R.drawable.ic_fix),
    BLACKWHITE("Уровень черного/белого", EffectFactory.EFFECT_BLACKWHITE, -1.0f, 1.0f, 0.0f, 0.0f, R.drawable.ic_filter_b_and_w),
    FILLIGHT("Заполняющий свет", EffectFactory.EFFECT_FILLLIGHT, 0.0f, 1.0f, 0.0f, 0.0f, R.drawable.ic_fillight),
    GRAIN("Зернистость", EffectFactory.EFFECT_GRAIN, 0.0f, 1.0f, 0.0f, 0.0f, R.drawable.ic_grain),
    TEMPERATURE("Температура", EffectFactory.EFFECT_TEMPERATURE, 0.0f, 1.0f, 0.5f, 0.5f, R.drawable.ic_sunny),
    FISHEYE("Объектив", EffectFactory.EFFECT_FISHEYE, 0.0f, 1.0f, 0.0f, 0.0f, R.drawable.ic_camera);

    var value: Int
        get() = ((currentValue - mMinValue) * 100 / (mMaxValue - mMinValue)).toInt()
        set(currentValue) {
            this.currentValue = currentValue * (mMaxValue - mMinValue) / 100 + mMinValue
        }

    fun clear() {
        currentValue = defaultValue
    }
}
//--------------------------------------------------------------------------------------------
