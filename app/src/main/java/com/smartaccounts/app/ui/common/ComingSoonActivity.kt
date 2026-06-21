package com.smartaccounts.app.ui.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smartaccounts.app.databinding.ActivityComingSoonBinding

class ComingSoonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComingSoonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComingSoonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        binding.toolbar.setNavigationOnClickListener { finish() }

        val iconRes = intent.getIntExtra(EXTRA_ICON_RES, 0)
        if (iconRes != 0) binding.imageIcon.setImageResource(iconRes)

        intent.getStringExtra(EXTRA_MESSAGE)?.let { binding.textMessage.text = it }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_ICON_RES = "extra_icon_res"
        private const val EXTRA_MESSAGE = "extra_message"

        fun start(context: Context, title: String, iconRes: Int = 0, message: String? = null) {
            val intent = Intent(context, ComingSoonActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ICON_RES, iconRes)
                message?.let { putExtra(EXTRA_MESSAGE, it) }
            }
            context.startActivity(intent)
        }
    }
}
