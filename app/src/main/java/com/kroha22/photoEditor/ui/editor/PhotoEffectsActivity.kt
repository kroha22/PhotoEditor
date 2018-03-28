package com.kroha22.photoEditor.ui.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.kroha22.photoEditor.R
import com.kroha22.photoEditor.photoEffects.Filter
import com.kroha22.photoEditor.photoEffects.Modify
import com.kroha22.photoEditor.photoEffects.Property



/**
 * Created by Olga
 * on 10.11.2017.
 */
//---------------------------------------------------------------------------------------------
@StateStrategyType(AddToEndSingleStrategy::class)
interface PhotoEffectsView : MvpView {

    fun hidePlaceholder()

    fun showPlaceholder()

    fun showPhoto(photo: Bitmap)

    fun showToast(message: String)

    fun showProperties(properties: Array<Property>)

    fun showPropertyDetail(property: Property)

    fun hidePropertyDetail()

    fun showFilters(filters: Array<Filter>)

    fun highlightFilter(filter: Filter, color: Int)

    fun applyEffects()

    fun showProgress()

    fun hideProgress()
}

//---------------------------------------------------------------------------------------------
abstract class PhotoEffectsActivity : MvpAppCompatActivity(), PhotoEffectsView {
    //---------------------------------------------------------------------------------------------
    private lateinit var photoViewContainer: PhotoViewContainer
    private lateinit var filterViews: LinkedHashMap<Filter, View>

    @InjectPresenter
    lateinit var presenter: PhotoEffectsPresenter

    private var needReset: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val photoContainer = PhotoContainer(this, presenter, contentResolver)
        setPhotoContainer(photoContainer.getView())
        photoViewContainer = photoContainer

        filterViews = LinkedHashMap()

        getFlipHorBtn().setOnClickListener { presenter.userSetFlip(Modify.FLIPHOR) }
        getFlipVertBtn().setOnClickListener { presenter.userSetFlip(Modify.FLIPVERT) }
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onPause() {
        super.onPause()
        photoViewContainer.onPause()
    }

    override fun onResume() {
        super.onResume()
        photoViewContainer.onResume()

        if (needReset) {
            presenter.userResetProperties()
            presenter.userResetFilter()
            needReset = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun showPhoto(photo: Bitmap) {
        photoViewContainer.showPhoto(photo)
    }

    override fun applyEffects() {
        photoViewContainer.applyEffects()
    }

    override fun showProperties(properties: Array<Property>) {
        getEffectsContainer().removeAllViewsInLayout()
        properties.forEach {
            getEffectsContainer().addView(
                    createPropertyView(this, it.iconId, { presenter.userCheckProperty(it) }),
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }
    }

    override fun showPropertyDetail(property: Property) {
        getEffectsDetails().visibility = View.VISIBLE
        getEffectsDetails().removeAllViewsInLayout()
        getEffectsDetails().addView(createPropertyDetailView(this, property, presenter::userChangePropertiesValue),
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
    }

    override fun hidePropertyDetail() {
        getEffectsDetails().visibility = View.INVISIBLE
    }

    override fun showFilters(filters: Array<Filter>) {
        getEffectsContainer().removeAllViewsInLayout()
        filters.forEach {
            val view = createFilterView(this, it.iconId, { presenter.userCheckFilter(it) })
            filterViews.put(it, view)
            getEffectsContainer().addView(
                    view,
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }
    }

    override fun highlightFilter(filter: Filter, color: Int) {
        filterViews[filter]!!.setBackgroundColor(color)
    }

    override fun showToast(message: String) {
       runOnUiThread({ Toast.makeText(this, message, Toast.LENGTH_SHORT).show()})
    }

    fun savePhoto() {
        photoViewContainer.savePhoto()
    }

    fun resetFilters() {
        needReset = true
    }

    fun resetProperties() {
        needReset = true
    }

    fun selectPhoto(selectedImage: Uri) {
        presenter.userSelectPhoto(contentResolver, selectedImage)
    }

    abstract fun getEffectsDetails(): LinearLayout
    abstract fun getEffectsContainer(): LinearLayout
    abstract fun getFlipHorBtn(): ImageButton
    abstract fun getFlipVertBtn(): ImageButton
    internal abstract fun setPhotoContainer(view: View)

    //--------------------------------------------------------------------------------------------------
    @SuppressLint("SetTextI18n", "InflateParams")
    private fun createPropertyDetailView(context: Context, model: Property, onPropertyChange: (Property) -> Unit): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.detail_property, null, false) as LinearLayout
        val propertyName = rootView.findViewById<TextView>(R.id.property_name)
        val percent = rootView.findViewById<TextView>(R.id.property_value)
        val propertySeekBar = rootView.findViewById<SeekBar>(R.id.property_seek_bar)
        val propertyImg = rootView.findViewById<ImageView>(R.id.property_img)

        propertyName.text = model.propertyName
        percent.text = model.value.toString() + " %"
        propertySeekBar.progress = model.value
        propertyImg.setImageResource(model.iconId)
        propertySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                percent.text = progress.toString() + "%"
                model.value = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onPropertyChange(model)
            }
        })

        return rootView
    }

    //--------------------------------------------------------------------------------------------------
    @SuppressLint("InflateParams")
    private fun createPropertyView(context: Context, imgRes: Int, onClick: () -> Unit): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.item_property, null, false) as LinearLayout
        val propertyImg = rootView.findViewById<ImageView>(R.id.item_property_icon)
        propertyImg.setImageResource(imgRes)
        rootView.setOnClickListener({ onClick() })
        return rootView
    }
    //--------------------------------------------------------------------------------------------------
    @SuppressLint("InflateParams")
    private fun createFilterView(context: Context, imgRes: Int, onClick: () -> Unit): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.item_filter, null, false) as LinearLayout
        val propertyImg = rootView.findViewById<ImageView>(R.id.item_filter_icon)
        propertyImg.setImageResource(imgRes)
        rootView.setOnClickListener({ onClick() })
        return rootView
    }
}