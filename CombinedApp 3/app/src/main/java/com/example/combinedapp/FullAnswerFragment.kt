package com.example.combinedapp

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
//import kotlinx.android.synthetic.main.fragment_full_answer.view.*


class FullAnswerFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        // Inflate the layout for this fragment
        var rootView: View = inflater.inflate(R.layout.fragment_full_answer, container, false)
        return rootView
    }




}
