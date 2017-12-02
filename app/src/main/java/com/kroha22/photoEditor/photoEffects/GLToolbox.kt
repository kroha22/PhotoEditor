package com.kroha22.photoEditor.photoEffects

import android.opengl.GLES20

import java.nio.FloatBuffer


/**
 * Created by Olga
 * on 10.11.2017.
 */
internal object GLToolbox {

    fun createProgram(vertexSource: String, fragmentSource: String): Int {

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }

        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }

        val program = GLES20.glCreateProgram()
        if (program != 0) {
            glAttachShader(vertexShader, program)
            glAttachShader(pixelShader, program)
            linkProgram(program)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                val info = GLES20.glGetProgramInfoLog(program)
                GLES20.glDeleteProgram(program)

                throw RuntimeException("Could not link program: " + info)
            }
        }
        return program
    }

    fun initTexParams() {
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }

    fun setVertexAttr(indx: Int, size: Int, normalized: Boolean, stride: Int, buffer: FloatBuffer) {
        GLES20.glVertexAttribPointer(indx, size, GLES20.GL_FLOAT, normalized, stride, buffer)
        GLES20.glEnableVertexAttribArray(indx)
        checkGlError("vertex attribute setup")
    }

    fun draw() {
        GLES20.glClearColor(240.toFloat() / 255, 240.toFloat() / 255, 240.toFloat() / 255, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    fun disableBlending() {
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    fun setViewport(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        checkGlError("glViewport")
    }

    fun useProgram(program: Int) {
        GLES20.glUseProgram(program)
        checkGlError("glUseProgram")
    }

    fun bindTexture(texId: Int) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        checkGlError("glActiveTexture")

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        checkGlError("glBindTexture")
    }

    fun bindFramebuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    fun uniform(location: Int) {
        GLES20.glUniform1i(location, 0)
    }

    fun getAttribLocation(program: Int, name: String): Int {
        return GLES20.glGetAttribLocation(program, name)
    }

    fun getUniformLocation(program: Int, name: String): Int {
        return GLES20.glGetUniformLocation(program, name)
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        val shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)

            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val info = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)

                throw RuntimeException("Could not compile shader $shaderType:$info")
            }
        }
        return shader
    }

    private fun glAttachShader(vertexShader: Int, program: Int) {
        GLES20.glAttachShader(program, vertexShader)
        checkGlError("glAttachShader")
    }

    private fun linkProgram(program: Int) {
        GLES20.glLinkProgram(program)
    }

    private fun checkGlError(op: String) {
        val error = GLES20.glGetError()

        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException(op + ": glError " + error)
        }
    }
}
