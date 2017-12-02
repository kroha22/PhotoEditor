package com.kroha22.photoEditor.ui.editor.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import butterknife.BindView
import butterknife.ButterKnife
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.kroha22.photoEditor.R
import com.kroha22.photoEditor.photoEffects.Filter
import com.kroha22.photoEditor.ui.editor.PhotoEffectsListener
import java.util.*

//--------------------------------------------------------------------------------------------------
private const val FILTER = "FILTER"
//--------------------------------------------------------------------------------------------------

@StateStrategyType(AddToEndSingleStrategy::class)
interface FiltersView : MvpView {

    fun setFiltersList(filters: List<Filter>)

    fun checkCurrentFilter(filter: Filter)

}
//--------------------------------------------------------------------------------------------------

class FiltersFragment : MvpAppCompatFragment(), FiltersView {

    @BindView(R.id.fragment_filter_radio_group)
    lateinit var mFilterRadioGroup: RadioGroup

    @InjectPresenter(tag = FILTER, type = PresenterType.GLOBAL)
    lateinit var mPresenter: FiltersPresenter

    lateinit var mRadioButtons: MutableList<RadioButton>

    private var mReset: Boolean = false
    private var photoEffectsListener: PhotoEffectsListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)
        ButterKnife.bind(this, view)

        mRadioButtons = ArrayList()

        return view
    }

    override fun onResume() {
        super.onResume()

        val propertyListener = this.photoEffectsListener
        if(propertyListener != null) {
            mPresenter.setFilterListener(propertyListener)
        }
        mPresenter.userSelectFiltersTab()
        if (mReset) {
            mPresenter.userResetFilter()
            mReset = false
        }
        mPresenter.userUpdateFiltersList()
        mFilterRadioGroup.setOnCheckedChangeListener { _, checkedId -> setFilter(checkedId) }
    }

    override fun setFiltersList(filters: List<Filter>) {
        mRadioButtons.clear()
        for (filter in Filter.values()) {
            val radioButton = RadioButton(context)
            radioButton.text = filter.filterName
            mRadioButtons.add(radioButton)
        }
    }

    override fun checkCurrentFilter(filter: Filter) {
        mRadioButtons.indices
                .map { mRadioButtons[it] }
                .filter { it.text.toString() == filter.filterName }
                .forEach { mFilterRadioGroup.check(it.id) }
    }

    fun resetFilters() {
        mReset = true
    }

    fun setPropertyListener(photoEffectsListener: PhotoEffectsListener) {
        this.photoEffectsListener = photoEffectsListener
    }

    private fun setFilter(id: Int) {
        mRadioButtons.indices
                .filter { mRadioButtons[it].id == id }
                .forEach { mPresenter.userCheckFilter(it) }
    }

}
