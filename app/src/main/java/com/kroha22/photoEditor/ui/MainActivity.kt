package com.kroha22.photoEditor.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import com.kroha22.photoEditor.R
import com.kroha22.photoEditor.ui.editor.PhotoEffectsActivity

//---------------------------------------------------------------------------------------------
const val GALLERY_REQUEST = 1
//---------------------------------------------------------------------------------------------
class MainActivity  : PhotoEffectsActivity() {

    lateinit private var photoPlaceholder: FrameLayout
    lateinit private var photoContainer: FrameLayout
    lateinit private var toolbar: Toolbar
    lateinit private var navigationView: NavigationView
    lateinit private var drawerLayout: DrawerLayout

    private lateinit var effectsContainer: LinearLayout
    private lateinit var effectsDetails: LinearLayout
    private lateinit var flipHorBtn: ImageButton
    private lateinit var flipVertBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        photoPlaceholder = findViewById(R.id.photo_placeholder)
        photoContainer = findViewById(R.id.photo_container)
        drawerLayout = findViewById(R.id.activity_main_drawerlayout)
        effectsContainer = findViewById(R.id.effects_container)
        effectsDetails = findViewById(R.id.effects_detail)
        flipHorBtn = findViewById(R.id.flip_hor_btn)
        flipVertBtn = findViewById(R.id.flip_vert_btn)
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.nav_view)

        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        initActionBar()
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.navigation_menu_item_property -> {
                    presenter.userSelectProperties()
                }

                R.id.navigation_menu_item_filter -> {
                    presenter.userSelectFilters()
                }

                R.id.navigation_menu_item_select -> {
                    showPhotoPicker()
                }
                R.id.navigation_menu_item_save -> {
                    showEnterNameDialog()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        photoPlaceholder.setOnClickListener({ showPhotoPicker() })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)

        if (imageReturnedIntent != null && requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            selectPhoto(imageReturnedIntent.data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun setPhotoContainer(view: View) {
        photoContainer.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    override fun hidePlaceholder() {
        photoPlaceholder.visibility = View.INVISIBLE
    }

    override fun showPlaceholder() {
        photoPlaceholder.visibility = View.VISIBLE
    }

    override fun getEffectsDetails(): LinearLayout {
        return effectsDetails
    }

    override fun getEffectsContainer(): LinearLayout {
        return effectsContainer
    }

    override fun getFlipHorBtn(): ImageButton {
        return flipHorBtn
    }

    override fun getFlipVertBtn(): ImageButton {
        return flipVertBtn
    }

    private fun initActionBar() {
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            val indicator = VectorDrawableCompat.create(resources, R.drawable.ic_menu, theme) ?: throw RuntimeException("action bar indicator null")

            indicator.setTint(ResourcesCompat.getColor(resources, R.color.darkGray, theme))

            supportActionBar.setHomeAsUpIndicator(indicator)
            supportActionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun showPhotoPicker() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"

        resetProperties()
        resetFilters()

        startActivityForResult(photoPickerIntent, GALLERY_REQUEST)
    }

    private fun showEnterNameDialog() {
        val name = EditText(this)

        val builder = AlertDialog.Builder(this)

        builder.
                setMessage(R.string.enter_name).
                setView(name).
                setPositiveButton(R.string.enter_name) { _, _ ->
                    if (name.text.isEmpty()) {
                        name.isFocusable = true
                    } else {
                        setPhotoName(name.text.toString())
                    }
                }.
                setNegativeButton(R.string.enter_name, null).create().show()
    }
}
