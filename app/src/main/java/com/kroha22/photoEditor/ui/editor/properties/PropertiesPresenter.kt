package com.kroha22.photoEditor.ui.editor.properties

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.kroha22.photoEditor.photoEffects.Modify
import com.kroha22.photoEditor.photoEffects.PropertiesType
import com.kroha22.photoEditor.photoEffects.Property
import com.kroha22.photoEditor.ui.editor.PhotoEffectsListener

/**
 * Created by Olga
 * on 10.11.2017.
 */

@InjectViewState
class PropertiesPresenter : MvpPresenter<PropertiesView>() {

    lateinit private var mStandardProperties: List<Property>
    lateinit private var mExtendProperties: List<Property>

    private var mPropertyListener: PhotoEffectsListener? = null

    internal fun setPropertyListener(propertyListener: PhotoEffectsListener) {
        mPropertyListener = propertyListener
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        mStandardProperties = Property.get(PropertiesType.STANDARD)
        mExtendProperties = Property.get(PropertiesType.EXTEND)
    }

    fun userResetProperties() {
        for (i in mStandardProperties.indices) {
            mStandardProperties[i].clear()
        }
        for (i in mExtendProperties.indices) {
            mExtendProperties[i].clear()
        }
    }

    fun userSelectPropertiesTab(type: PropertiesType) {

        when (type) {
            PropertiesType.STANDARD -> viewState.setPropertiesList(mStandardProperties)

            PropertiesType.EXTEND -> viewState.setPropertiesList(mExtendProperties)
        }
    }

    fun userSetFlip(modify: Modify) {
        mPropertyListener!!.setModify(modify)
    }

    fun userChangePropertiesValue(property: Property) {
        mPropertyListener!!.setProperty(property)
    }
}
