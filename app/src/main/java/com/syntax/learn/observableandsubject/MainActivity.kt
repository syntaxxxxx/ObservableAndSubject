package com.syntax.learn.observableandsubject

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SharedViewModel
    private val selectedImagesDisposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "ObservablesAndSubject"

        viewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)

        btn_add.setOnClickListener {
            actionAdd()
        }

        btn_clear.setOnClickListener {
            actionClear()
        }

        btn_save.setOnClickListener {
            actionSave()
        }
        doObserver()
        doObserverThumbnailStatus()
    }

    private fun doObserver() {
        viewModel.getSelectedPhotos().observe(this, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    val bitmaps = it.map { BitmapFactory.decodeResource(resources, it.drawable) }
                    val newBitmap = combineImages(bitmaps)
                    iv_satu.setImageDrawable(BitmapDrawable(resources, newBitmap))
                    updateUI(it)
                } else {
                    actionClear()
                }
            }
        })
    }

    private fun doObserverThumbnailStatus() {
        viewModel.getThumbnailStatus().observe(this, Observer {status ->
            if (status == ThumbnailStatus.READY) {
                iv_dua.setImageDrawable(iv_dua.drawable)
            }
        })
    }

    private fun actionAdd() {
//        viewModel.addPhotos(PhotoStore.photos[0])
        val addPhotoBottomDialogFragment = PhotosBottomDialogFragment.newInstance()
        addPhotoBottomDialogFragment.show(supportFragmentManager, PhotosBottomDialogFragment.TAG)
        addPhotoBottomDialogFragment.selectedPhotos
                .subscribe {
                    val drawable = resources.getDrawable(it.drawable, theme)
                    iv_dua.setImageDrawable(drawable)
                }.addTo(selectedImagesDisposables)
        viewModel.subscribeSelectedPhotos(addPhotoBottomDialogFragment)
    }

    private fun actionClear() {
        viewModel.clearPhotos()
        iv_satu.setImageResource(android.R.color.transparent)
        updateUI(listOf())
    }

    private fun actionSave() {
        progress_bar.visibility = View.VISIBLE
        viewModel.saveBitmapFromImageView(iv_satu, this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { file ->
                            toast("$file saved")
                            progress_bar.visibility = View.GONE
                        },
                        onError = {
                            toast("Error saving file :${it.message}")
                            progress_bar.visibility = View.GONE
                        }
                )
    }

    private fun updateUI(photos: List<Photo>) {
        btn_save.isEnabled = photos.isNotEmpty() && (photos.size % 2 == 0)
        btn_clear.isEnabled = photos.isNotEmpty()
        btn_add.isEnabled = photos.size < 6
        title = if (photos.isNotEmpty()) {
            resources.getQuantityString(R.plurals.photos_format, photos.size, photos.size)
        } else {
            "ObservableAndSubject"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!selectedImagesDisposables.isDisposed) {
            selectedImagesDisposables.dispose()
        }
    }
}
