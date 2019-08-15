package com.syntax.learn.observableandsubject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit


class SharedViewModel : ViewModel() {

    private val _selectedPhotos = MutableLiveData<List<Photo>>()
    private val _thumbnailStatus = MutableLiveData<ThumbnailStatus>()

    private val subscriptions = CompositeDisposable()
    private val _imagesSubject: BehaviorSubject<MutableList<Photo>> = BehaviorSubject.createDefault<MutableList<Photo>>(mutableListOf())

    init {
        _imagesSubject.subscribe { photos ->
            _selectedPhotos.value = photos
        }.addTo(subscriptions)
    }

    override fun onCleared() {
        subscriptions.dispose()
        super.onCleared()
    }

    fun getSelectedPhotos(): LiveData<List<Photo>> {
        return _selectedPhotos
    }

    fun getThumbnailStatus(): LiveData<ThumbnailStatus> {
        return _thumbnailStatus
    }

//    fun addPhotos(photo: Photo) {
//        ivSubjects.value?.add(photo)
//        ivSubjects.onNext(ivSubjects.value!!)
//    }

    fun subscribeSelectedPhotos(fragment: PhotosBottomDialogFragment) {
        val newPhotos = fragment.selectedPhotos.share()
        subscriptions.add(newPhotos
                .doOnComplete {
                    Log.v("TAG", "Selected photos is complete")

                }.takeWhile {
                    _imagesSubject.value?.size ?: 0 < 6
                }
                .filter { newImage ->
                    val bitmap = BitmapFactory.decodeResource(fragment.resources, newImage.drawable)
                    bitmap.width > bitmap.height
                }
                .filter { newImage ->
                    val photos = _imagesSubject.value ?: mutableListOf()
                    !(photos.map {
                        it.drawable
                    }.contains(newImage.drawable))
                }
                .debounce(250, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { photo ->
                    _imagesSubject.value?.add(photo)
                    _imagesSubject.onNext(_imagesSubject.value ?: mutableListOf())
                }
        )
        subscriptions.add(newPhotos
                .ignoreElements()
                .subscribe {
                    _thumbnailStatus.postValue(ThumbnailStatus.READY)
                })
    }

    // subscribe { photo ->
    // _imagesSubject.value?.add(photo)
    // _imagesSubject.onNext(_imagesSubject.value ?: mutableListOf())

    fun clearPhotos() {
        _imagesSubject.value?.clear()
    }

    fun saveBitmapFromImageView(imageView: ImageView, context: Context): Single<String> {
        return Single.create { emitter ->
            val tmpImg = "${System.currentTimeMillis()}.png"

            val os: OutputStream?

            val collagesDirectory = File(context.getExternalFilesDir(null), "collages")
            if (!collagesDirectory.exists()) {
                collagesDirectory.mkdirs()
            }

            val file = File(collagesDirectory, tmpImg)

            try {
                os = FileOutputStream(file)
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.flush()
                os.close()
                emitter.onSuccess(tmpImg)
            } catch (e: IOException) {
                emitter.onError(e)
            }
        }
    }
}

