package com.example.calmlearn.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.calmlearn.R
import com.example.calmlearn.data.mock.MockData
import com.example.calmlearn.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val pages = MockData.onboardingPages
    private val dots = mutableListOf<View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.onboardingPager.adapter = OnboardingPagerAdapter(pages)
        buildDots()

        binding.onboardingPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                val isLastPage = position == pages.lastIndex
                binding.btnPrimary.text = if (isLastPage) {
                    getString(R.string.onboarding_start)
                } else {
                    getString(R.string.onboarding_next)
                }
                binding.btnSkip.visibility = if (isLastPage) View.INVISIBLE else View.VISIBLE
            }
        })

        // Bo qua onboarding khong duoc phep bo qua buoc xac thuc: ca "Bo qua" lan nut cuoi
        // cung deu dua nguoi dung ve man hinh Dang nhap (project chua co che do khach rieng).
        binding.btnSkip.setOnClickListener { goToLogin() }

        binding.btnPrimary.setOnClickListener {
            val current = binding.onboardingPager.currentItem
            if (current == pages.lastIndex) {
                goToLogin()
            } else {
                binding.onboardingPager.currentItem = current + 1
            }
        }
    }

    private fun buildDots() {
        binding.dotsContainer.removeAllViews()
        dots.clear()
        val activeWidth = resources.getDimensionPixelSize(R.dimen.dot_size_active)
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.dot_size)
        pages.indices.forEach { index ->
            val dot = View(requireContext())
            val width = if (index == 0) activeWidth else inactiveSize
            val params = LinearLayout.LayoutParams(width, inactiveSize)
            params.marginStart = 4.dp()
            params.marginEnd = 4.dp()
            dot.layoutParams = params
            dot.setBackgroundResource(if (index == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
            binding.dotsContainer.addView(dot)
            dots.add(dot)
        }
    }

    private fun updateDots(activePosition: Int) {
        val activeWidth = resources.getDimensionPixelSize(R.dimen.dot_size_active)
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.dot_size)
        dots.forEachIndexed { index, dot ->
            val isActive = index == activePosition
            dot.setBackgroundResource(if (isActive) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
            val params = dot.layoutParams
            params.width = if (isActive) activeWidth else inactiveSize
            dot.layoutParams = params
        }
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()

    private fun goToLogin() {
        findNavController().navigate(R.id.action_global_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
