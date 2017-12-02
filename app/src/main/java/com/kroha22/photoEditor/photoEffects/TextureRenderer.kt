package com.kroha22.photoEditor.photoEffects

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * Created by Olga
 * on 10.11.2017.
 */

//---------------------------------------------------------------------------------------------
private const val VERTEX_SHADER = "attribute vec4 a_position;\n" +
        "attribute vec2 a_texcoord;\n" +
        "varying vec2 v_texcoord;\n" +
        "void main() {\n" +
        "  gl_Position = a_position;\n" +
        "  v_texcoord = a_texcoord;\n" +
        "}\n"

private const val FRAGMENT_SHADER = "precision mediump float;\n" +
        "uniform sampler2D tex_sampler;\n" +
        "varying vec2 v_texcoord;\n" +
        "void main() {\n" +
        "  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
        "}\n"

private const val FLOAT_SIZE_BYTES = 4
private const val TEX_SAMPLER = "tex_sampler"
private const val A_TEXCOORD = "a_texcoord"
private const val A_POSITION = "a_position"
//---------------------------------------------------------------------------------------------
class TextureRenderer {

    private val TEX_VERTICES = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    private val POS_VERTICES = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f)

    private var program: Int = 0
    private var texSamplerHandle: Int = 0
    private var texCrdHandle: Int = 0
    private var posCrdHandle: Int = 0

    lateinit var texVertices: FloatBuffer
    lateinit var posVertices: FloatBuffer

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    private var texWidth: Int = 0
    private var texHeight: Int = 0

    fun init() {
        program = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)

        // Bind attributes and uniforms
        texSamplerHandle = GLToolbox.getUniformLocation(program, TEX_SAMPLER)
        texCrdHandle = GLToolbox.getAttribLocation(program, A_TEXCOORD)
        posCrdHandle = GLToolbox.getAttribLocation(program, A_POSITION)

        // Setup coordinate buffers
        texVertices = getFloatBuffer(TEX_VERTICES.size * FLOAT_SIZE_BYTES)
        texVertices.put(TEX_VERTICES).position(0)

        posVertices = getFloatBuffer(POS_VERTICES.size * FLOAT_SIZE_BYTES)
        posVertices.put(POS_VERTICES).position(0)
    }

    fun tearDown() {
        GLES20.glDeleteProgram(program)
    }

    fun updateViewSize(viewWidth: Int, viewHeight: Int) {
        this.viewWidth = viewWidth
        this.viewHeight = viewHeight

        computeOutputVertices()
    }

    fun updateTextureSize(texWidth: Int, texHeight: Int) {
        this.texWidth = texWidth
        this.texHeight = texHeight

        computeOutputVertices()
    }

    fun renderTexture(texId: Int) {
        GLToolbox.bindFramebuffer()

        GLToolbox.useProgram(program)

        GLToolbox.setViewport(viewWidth, viewHeight)
        GLToolbox.disableBlending()
        GLToolbox.setVertexAttr(texCrdHandle, 2, false, 0, texVertices)
        GLToolbox.setVertexAttr(posCrdHandle, 2, false, 0, posVertices)

        GLToolbox.bindTexture(texId)

        GLToolbox.uniform(texSamplerHandle)

        GLToolbox.draw()
    }

    private fun computeOutputVertices() {
        val imgAspectRatio = texWidth / texHeight.toFloat()
        val viewAspectRatio = viewWidth / viewHeight.toFloat()
        val coords = computeOutputVertices(imgAspectRatio, viewAspectRatio)
        posVertices.put(coords).position(0)
    }

    private fun computeOutputVertices(imgAspectRatio: Float, viewAspectRatio: Float): FloatArray {
        val relativeAspectRatio = viewAspectRatio / imgAspectRatio

        val x0: Float
        val y0: Float
        val x1: Float
        val y1: Float
        if (relativeAspectRatio > 1.0f) {
            x0 = -1.0f / relativeAspectRatio
            y0 = -1.0f
            x1 = 1.0f / relativeAspectRatio
            y1 = 1.0f

        } else {
            x0 = -1.0f
            y0 = -relativeAspectRatio
            x1 = 1.0f
            y1 = relativeAspectRatio
        }

        return floatArrayOf(x0, y0, x1, y0, x0, y1, x1, y1)
    }

    private fun getFloatBuffer(capacity: Int): FloatBuffer {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder()).asFloatBuffer()
    }

}

