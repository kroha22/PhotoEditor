package com.kroha22.photoEditor.photoEffects

import android.content.ContentResolver
import android.graphics.Bitmap
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.os.Environment
import android.provider.MediaStore
import com.google.common.collect.Lists
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10


/**
 * Created by Olga
 * on 10.11.2017.
 */

object PhotoUtils {
    //---------------------------------------------------------------------------------------------
    class EffectCreator {
        companion object {
            /*
             Создание эффектов из примененных Property
             */
            fun createEffects(effectContext: EffectContext, properties: List<Property>): List<Effect> {
                val effectFactory = effectContext.factory
                return Lists.transform(properties, { prop -> prop?.let { createEffect(effectFactory, it) } })
            }
            /*
             Создание эффекта из Modify
             */
            fun createEffect(effectContext: EffectContext, modify: Modify): Effect {

                val effectFactory = effectContext.factory
                val effectName = modify.effectName
                val effect = effectFactory.createEffect(effectName)

                when (modify) {
                    Modify.FLIPVERT -> effect.setParameter(EffectParameter.VERTICAL, true)
                    Modify.FLIPHOR -> effect.setParameter(EffectParameter.HORIZONTAL, true)
                }

                return effect

            }
            /*
             Создание эффекта из Filter
             */
            fun createEffect(effectContext: EffectContext, filter: Filter): Effect {
                val effectFactory = effectContext.factory
                val effectName = filter.effectName

                return effectFactory.createEffect(effectName)
            }

            private fun createEffect(effectFactory: EffectFactory, property: Property): Effect {

                val effect = effectFactory.createEffect(property.effectName)
                val value = property.currentValue

                when (property) {

                    Property.BRIGHTNESS -> effect.setParameter(EffectParameter.BRIGHTNESS, value)

                    Property.CONTRAST -> effect.setParameter(EffectParameter.CONTRAST, value)

                    Property.BLACKWHITE -> {
                        val blackVal = if (value > 0.0f) value else 0.0f
                        val whiteVal = if (value < 0.0f) -value else 0.0f

                        effect.setParameter(EffectParameter.BLACK, blackVal)
                        effect.setParameter(EffectParameter.WHITE, whiteVal)
                    }

                    Property.FILLIGHT, Property.GRAIN -> effect.setParameter(EffectParameter.STRENGTH, value)

                    else -> effect.setParameter(EffectParameter.SCALE, value)
                }

                return effect
            }

            private fun Effect.setParameter(param: EffectParameter, value: Any) {
                this.setParameter(param.parameterName, value)
            }
        }
    }
    //---------------------------------------------------------------------------------------------
    /*
     Сохранение изображения из GLSurfaceView на устр-ве
     */
    fun savePhoto(gl: GL10,
                  contentResolver: ContentResolver,
                  view: GLSurfaceView,
                  name: String,
                  height: Int,
                  width: Int): String {

        val photo = PhotoUtils.savePixels(gl, height, width, view)
        try {
            val file = createFile(name, photo)
            MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, file.name, file.name)

        } catch (e: Exception) {
            return "Ошибка сохранения"
        }

        return "Сохранено"
    }
    /*
     Выгрузка изображений из bitmap
     */
    fun loadPhoto(photo: Bitmap, textures: IntArray, texRenderer: TextureRenderer) {

        GLES20.glGenTextures(5, textures, 0)

        texRenderer.updateTextureSize(photo.width, photo.height)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, photo, 0)

        GLToolbox.initTexParams()
    }

    private fun savePixels(gl: GL10, height: Int, width: Int, view: GLSurfaceView): Bitmap {

        val heightView = view.height
        val widthView = view.width

        val x: Int
        val y: Int
        val w: Int
        val h: Int

        if (heightView / height < widthView / width) {
            h = heightView
            w = width * heightView / height
        } else {
            h = height * widthView / width
            w = widthView
        }

        x = (widthView - w) / 2
        y = (heightView - h) / 2

        val b = IntArray(w * h)
        val bt = IntArray(w * h)
        val ib = IntBuffer.wrap(b)
        ib.position(0)

        gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib)

        for (i in 0 until h) {
            for (j in 0 until w) {
                val pix = b[i * w + j]
                val pb = pix shr 16 and 0xff
                val pr = pix shl 16 and 0x00ff0000
                val pix1 = pix and -0xff0100 or pr or pb
                bt[(h - i - 1) * w + j] = pix1
            }
        }

        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888)
    }


    @Throws(IOException::class)
    private fun createFile(name: String, photo: Bitmap): File {

        val path = Environment.getExternalStorageDirectory().toString()
        val myDir = File(path + "/saved_images")


        myDir.mkdirs()

        val fileName = name + ".jpg"

        var fOut: OutputStream? = null

        val file = File(myDir, fileName)

        try {
            fOut = FileOutputStream(file)
            photo.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
        } finally {
            if (fOut != null) {
                fOut.close()
            }
        }
        return file
    }

}
