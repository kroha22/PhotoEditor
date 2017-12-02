package com.kroha22.photoEditor.ui

import android.app.Activity
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import butterknife.BindView
import butterknife.ButterKnife
import com.kroha22.photoEditor.R
import com.kroha22.photoEditor.photoEffects.PropertiesType
import com.kroha22.photoEditor.ui.editor.PhotoEffectsActivity
import com.kroha22.photoEditor.ui.editor.filters.FiltersFragment
import com.kroha22.photoEditor.ui.editor.properties.PropertiesFragment
import com.roughike.bottombar.BottomBar


/**
 * Created by Olga
 * on 10.11.2017.
 */

//---------------------------------------------------------------------------------------------
const val EFFECT_TYPE = "effect_type"
const val GALLERY_REQUEST = 1
//---------------------------------------------------------------------------------------------

class MainActivity : PhotoEffectsActivity() {

    @BindView(R.id.activity_main_photo_placeholder) lateinit var photoPlaceholder: FrameLayout
    @BindView(R.id.activity_main_toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.activity_main_nav_view) lateinit var navigationView: NavigationView
    @BindView(R.id.activity_main_drawerlayout) lateinit var drawerLayout: DrawerLayout

    lateinit private var fragmentManager: FragmentManager
    lateinit private var filters: FiltersFragment
    lateinit private var standardProperties: PropertiesFragment
    lateinit private var extendProperties: PropertiesFragment

    override
    fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        filters = createFilterFragment()
        standardProperties = createPropertiesFragment(PropertiesType.STANDARD)
        extendProperties = createPropertiesFragment(PropertiesType.EXTEND)

        fragmentManager = supportFragmentManager

        setSupportActionBar(toolbar)
        initActionBar()
        initBottomBar()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
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

    override
    fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)

        if (imageReturnedIntent != null && requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            selectPhoto(imageReturnedIntent.data)
        }
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override
    fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override
    fun createSurfaceView(): GLSurfaceView {
        val glSurfaceView = findViewById<View>(R.id.activity_main_image_view_photo) as GLSurfaceView

        if (isProbablyEmulator()) {
            // Avoids crashes on startup with some emulator images.
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        }

        return glSurfaceView
    }

    override
    fun hidePlaceholder() {
        initEffectView()
        photoPlaceholder.visibility = View.GONE
    }

    override
    fun showPlaceholder() {
        photoPlaceholder.visibility = View.VISIBLE
    }

    private fun createFilterFragment(): FiltersFragment {
        val fragment = FiltersFragment()
        fragment.setPropertyListener(presenter)
        return fragment
    }

    private fun createPropertiesFragment(type: PropertiesType): PropertiesFragment {
        val fragment = PropertiesFragment()

        val args = Bundle()
        args.putSerializable(EFFECT_TYPE, type)
        fragment.arguments = args
        fragment.setPropertyListener(presenter)

        return fragment
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

    private fun initBottomBar() {
        val bottomBar = findViewById<BottomBar>(R.id.bottomBar)
        bottomBar.setOnTabSelectListener { tabId ->
            if (tabId == R.id.bottom_menu_standard) {
                fragmentManager.beginTransaction()
                        .replace(R.id.activity_main_frame_layout_container, standardProperties)
                        .addToBackStack(null)
                        .commit()

            } else if (tabId == R.id.bottom_menu_extend) {
                fragmentManager.beginTransaction()
                        .replace(R.id.activity_main_frame_layout_container, extendProperties)
                        .addToBackStack(null)
                        .commit()
            } else {
                fragmentManager.beginTransaction()
                        .replace(R.id.activity_main_frame_layout_container, filters)
                        .addToBackStack(null)
                        .commit()
            }
        }
    }

    private fun showPhotoPicker() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"

        standardProperties.resetProperties()
        extendProperties.resetProperties()
        filters.resetFilters()

        startActivityForResult(photoPickerIntent, GALLERY_REQUEST)
    }

    private fun showEnterNameDialog() {
        val name = EditText(this)

        val builder = AlertDialog.Builder(this)

        builder.
                setMessage(R.string.enter_name).
                setView(name).
                setPositiveButton(R.string.enter_name) {
                    _, _ ->
                    if (name.text.length == 0) {
                        name.isFocusable = true
                    } else {
                        setPhotoName(name.text.toString())
                    }
                }.
                setNegativeButton(R.string.enter_name, null).create().show()
    }
    /*
     Проверка работаем ли на эмуляторе
     */
    private fun isProbablyEmulator(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))
    }
}

