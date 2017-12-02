package com.kroha22.photoEditor.ui.editor.properties

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.kroha22.photoEditor.R
import com.kroha22.photoEditor.ui.listAdapter.CollectionRecycleAdapter
import com.kroha22.photoEditor.ui.listAdapter.RecycleViewHolder
import com.kroha22.photoEditor.photoEffects.Modify
import com.kroha22.photoEditor.photoEffects.PropertiesType
import com.kroha22.photoEditor.photoEffects.Property
import com.kroha22.photoEditor.ui.EFFECT_TYPE
import com.kroha22.photoEditor.ui.editor.PhotoEffectsListener

/**
 * Created by Olga
 * on 10.11.2017.
 */

//--------------------------------------------------------------------------------------------------
@StateStrategyType(AddToEndSingleStrategy::class)
interface PropertiesView : MvpView {

    fun showProperties()

    fun setPropertiesList(properties: List<Property>)

}
//--------------------------------------------------------------------------------------------------

class PropertiesFragment : MvpAppCompatFragment(), PropertiesView {

    @BindView(R.id.fragment_property_list_button_flip_hor)
    lateinit var mFlipHorButton: ImageButton

    @BindView(R.id.fragment_property_list_button_flip_vert)
    lateinit var mFlipVertButton: ImageButton

    @BindView(R.id.fragment_properties_list)
    lateinit var mPropertyRecyclerView: RecyclerView

    @InjectPresenter
    lateinit internal var mPresenter: PropertiesPresenter

    private lateinit var mEffectsType: PropertiesType
    private lateinit var mAdapter: CollectionRecycleAdapter<Property>

    private var needReset: Boolean = false
    private var photoEffectsListener: PhotoEffectsListener? = null

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mEffectsType = this.arguments.getSerializable(EFFECT_TYPE) as PropertiesType
    }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_properties, container, false)
        ButterKnife.bind(this, view)

        mAdapter = createAdapter(inflater)

        mPropertyRecyclerView.adapter = mAdapter
        mPropertyRecyclerView.layoutManager = LinearLayoutManager(activity)

        mFlipHorButton.setOnClickListener { mPresenter.userSetFlip(Modify.FLIPHOR) }
        mFlipVertButton.setOnClickListener { mPresenter.userSetFlip(Modify.FLIPVERT) }

        return view
    }

    override
    fun onResume() {
        super.onResume()

        val propertyListener = this.photoEffectsListener
        if(propertyListener != null) {
            mPresenter.setPropertyListener(propertyListener)
        }

        if (needReset) {
            mPresenter.userResetProperties()
            needReset = false
        }
        mPresenter.userSelectPropertiesTab(mEffectsType)
    }

    override
    fun showProperties() {
        mPropertyRecyclerView.visibility = View.VISIBLE
    }

    override
    fun setPropertiesList(properties: List<Property>) {
        mAdapter.setItems(properties)
    }

    fun resetProperties() {
        needReset = true
    }

    fun setPropertyListener(photoEffectsListener: PhotoEffectsListener) {
        this.photoEffectsListener = photoEffectsListener
    }

    private fun createAdapter(inflater: LayoutInflater): CollectionRecycleAdapter<Property> {

        return object : CollectionRecycleAdapter<Property>(activity) {

            override
            fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecycleViewHolder<Property> {

                return PropertyViewHolder(
                        inflater.inflate(R.layout.item_property, parent, false),
                        { property -> mPresenter.userChangePropertiesValue(property) }
                )
            }
        }
    }

}
//--------------------------------------------------------------------------------------------------

internal class PropertyViewHolder(itemView: View, val onChangeProperty: (Property) -> Unit) : RecycleViewHolder<Property>(itemView) {

    @BindView(R.id.item_property_name) lateinit var mTextViewProperty: TextView
    @BindView(R.id.item_property_percent) lateinit var mTextViewPercent: TextView
    @BindView(R.id.item_property_seek_bar) lateinit var mSeekBarPercent: SeekBar
    @BindView(R.id.item_property_image) lateinit var mImageView: ImageView

    override fun create(rootView: View) {
        ButterKnife.bind(this, itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(model: Property) {

        mTextViewProperty.text = model.propertyName
        mTextViewPercent.text = model.value.toString() + " %"
        mSeekBarPercent.progress = model.value
        mImageView.setImageResource(model.iconId)

        mSeekBarPercent.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mTextViewPercent.text = progress.toString() + "%"
                model.value = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onChangeProperty(model)
            }
        })
    }
}
//--------------------------------------------------------------------------------------------------

