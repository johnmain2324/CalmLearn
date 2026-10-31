package com.education.calmlearn.ui.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.calmlearn.databinding.FragmentChatbotBinding
import com.education.calmlearn.ui.common.hideKeyboard

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatbotViewModel by viewModels()
    private val adapter = ChatMessageAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvMessages.adapter = adapter

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSend.setOnClickListener { sendCurrentText() }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            if (messages.isNotEmpty()) binding.rvMessages.scrollToPosition(messages.size - 1)
        }
        viewModel.isReplying.observe(viewLifecycleOwner) { replying ->
            binding.tvTyping.visibility = if (replying) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !replying
            binding.btnSend.alpha = if (replying) 0.5f else 1f
        }
        viewModel.error.observe(viewLifecycleOwner) { error -> if (error != null) showError(error) }
    }

    private fun sendCurrentText() {
        val text = binding.etMessage.text.toString()
        if (text.isBlank()) return
        binding.etMessage.setText("")
        binding.root.hideKeyboard()
        viewModel.send(text)
    }

    private fun showError(error: ChatbotError) {
        Toast.makeText(requireContext(), error.reason.toMessageRes(), Toast.LENGTH_LONG).show()
        if (error.failedText.isNotEmpty() && binding.etMessage.text.isNullOrEmpty()) {
            binding.etMessage.setText(error.failedText)
            binding.etMessage.setSelection(error.failedText.length)
        }
        viewModel.consumeError()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
