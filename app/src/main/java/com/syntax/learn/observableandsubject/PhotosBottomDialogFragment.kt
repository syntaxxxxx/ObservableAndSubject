package com.syntax.learn.observableandsubject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_photo_bottom_sheet.*

class PhotosBottomDialogFragment : BottomSheetDialogFragment(), PhotosAdapter.PhotoListener {

  private lateinit var viewModel: SharedViewModel

  private val selectedPhotosSubject = PublishSubject.create<Photo>()

  val selectedPhotos: Observable<Photo> = selectedPhotosSubject.hide()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.layout_photo_bottom_sheet, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    val ctx = activity
    ctx?.let {
      viewModel = ViewModelProviders.of(ctx).get(SharedViewModel::class.java)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerview.layoutManager = GridLayoutManager(context, 3)
    recyclerview.adapter = PhotosAdapter(PhotoStore.photos, this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    // complete observable when user selected photo
    // fro avoiding leaks memory
    selectedPhotosSubject.onComplete()
  }

  override fun photoClicked(photo: Photo) {
    selectedPhotosSubject.onNext(photo)
  }

  companion object {
    const val TAG = "PhotosBottomDialogFragment"
    fun newInstance(): PhotosBottomDialogFragment {
      return PhotosBottomDialogFragment()
    }
  }
}