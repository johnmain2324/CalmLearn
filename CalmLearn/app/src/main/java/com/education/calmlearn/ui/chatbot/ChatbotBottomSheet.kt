package com.education.calmlearn.ui.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.education.calmlearn.R
import com.education.calmlearn.databinding.BottomSheetChatbotBinding
import com.education.calmlearn.ui.common.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChatbotBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetChatbotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatbotViewModel by viewModels()
    private val adapter = ChatMessageAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val sheet = dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        sheet.setBackgroundResource(R.drawable.bg_sheet_top)
        val maxHeight = (resources.displayMetrics.heightPixels * SHEET_HEIGHT_RATIO).toInt()
        (sheet.parent as View).addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val height = minOf(maxHeight, bottom - top)
            if (sheet.layoutParams.height != height) {
                sheet.layoutParams.height = height
                sheet.requestLayout()
            }
        }
        BottomSheetBehavior.from(sheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvMessages.adapter = adapter

        binding.btnClose.setOnClickListener { dismiss() }
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

    companion object {
        private const val TAG = "chatbot_sheet"
        private const val SHEET_HEIGHT_RATIO = 0.85

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                ChatbotBottomSheet().show(fragmentManager, TAG)
            }
        }
    }
}
