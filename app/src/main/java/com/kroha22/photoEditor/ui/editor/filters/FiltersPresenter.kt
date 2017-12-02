package com.kroha22.photoEditor.ui.editor.filters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.kroha22.photoEditor.photoEffects.Filter
import com.kroha22.photoEditor.ui.editor.PhotoEffectsListener
import java.util.*


/**
 * Date: 08.07.16
 * Time: 17:03
 *
 * @author Olga
 */

@InjectViewState
class FiltersPresenter : MvpPresenter<FiltersView>() {

    lateinit var mCurrentFilter: Filter
    lateinit var mFilters: List<Filter>
    private var mFilterListener: PhotoEffectsListener? = null

    internal fun setFilterListener(filterListener: PhotoEffectsListener) {
        mFilterListener = filterListener
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        mCurrentFilter = Filter.NONE
        mFilters = Arrays.asList(*Filter.values())
    }

    internal fun userCheckFilter(i: Int) {
        val filter = mFilters[i]

        if (mCurrentFilter != filter) {
            mCurrentFilter = filter
            mFilterListener!!.setFilter(mCurrentFilter)
        }
    }

    internal fun userSelectFiltersTab() {
        viewState.setFiltersList(mFilters)
    }

    internal fun userResetFilter() {
        mCurrentFilter = Filter.NONE
    }

    internal fun userUpdateFiltersList() {
        viewState.checkCurrentFilter(mCurrentFilter)
    }

}

