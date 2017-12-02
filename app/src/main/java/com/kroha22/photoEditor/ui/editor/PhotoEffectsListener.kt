package com.kroha22.photoEditor.ui.editor

import com.kroha22.photoEditor.photoEffects.Filter
import com.kroha22.photoEditor.photoEffects.Modify
import com.kroha22.photoEditor.photoEffects.Property


/**
 * Created by Olga
 * on 10.11.2017.
 */

interface PhotoEffectsListener {

    fun setFilter(filter: Filter)

    fun setProperty(property: Property)

    fun setModify(modify: Modify)

}
