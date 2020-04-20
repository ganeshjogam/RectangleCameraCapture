package com.example.rectanglecameracapture

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rectanglecameracapture.data.Photo
import com.example.rectanglecameracapture.data.PhotoViewModelFactory

class PhotoListFragment : Fragment() {
    var photoListAction: PhotoListAction? = null
    lateinit var photoViewModel: PhotoViewModel
    lateinit var photoListAdapter: PhotoListAdapter
    lateinit var no_result_layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        photoViewModel = ViewModelProvider(
            requireActivity(),
            PhotoViewModelFactory(requireActivity().application)
        )
            .get(PhotoViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoViewModel.photoList.observe(this, Observer { photoList ->
            photoListAdapter.photoList = photoList
            if (photoList.isEmpty()) {
                no_result_layout.visibility = View.VISIBLE
            } else {
                no_result_layout.visibility = View.INVISIBLE
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is PhotoListAction) {
            photoListAction = activity as PhotoListAction
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_photo_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.photo_list)
        photoListAdapter = PhotoListAdapter()
        recyclerView.adapter = photoListAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        view.findViewById<ImageView>(R.id.capture_image).setOnClickListener {
            photoListAction?.onCaptureAction()
        }

        no_result_layout = view.findViewById(R.id.no_result_layout)

        return view
    }

    interface PhotoListAction {
        fun onCaptureAction()
    }
}
