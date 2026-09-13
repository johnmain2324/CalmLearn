package com.example.calmlearn.ui.listening

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calmlearn.R
import com.example.calmlearn.data.mock.MockData
import com.example.calmlearn.databinding.FragmentListeningListBinding

class ListeningListFragment : Fragment() {

    private var _binding: FragmentListeningListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeningListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.lessonList.layoutManager = LinearLayoutManager(requireContext())
        binding.lessonList.adapter = ListeningLessonAdapter(MockData.listeningLessons) { lesson ->
            findNavController().navigate(R.id.action_global_listening, bundleOf("lessonId" to lesson.id))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
