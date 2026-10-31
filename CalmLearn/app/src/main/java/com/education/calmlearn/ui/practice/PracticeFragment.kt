package com.education.calmlearn.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.education.calmlearn.R
import com.education.calmlearn.databinding.FragmentPracticeBinding
import com.education.calmlearn.ui.chatbot.ChatbotBottomSheet

class PracticeFragment : Fragment() {

    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.itemSpeaking.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_speaking)
        }
        binding.itemListening.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_listeningList)
        }
        binding.itemQuiz.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_quiz)
        }
        binding.itemFlashcard.root.setOnClickListener {
            findNavController().navigate(R.id.action_global_flashcard, bundleOf("topicId" to "travel"))
        }
        binding.itemChatbot.root.setOnClickListener {
            ChatbotBottomSheet.show(requireActivity().supportFragmentManager)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
