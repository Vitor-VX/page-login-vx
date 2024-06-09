package victor.vx.app.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import victor.vx.app.R

class DiscordFragment : Fragment() {

    private var btnEnterDc : Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_discord, container, false)

        startElementID(view)

        btnEnterDc?.setOnClickListener {
            val url = "https://discord.gg"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        return view
    }

    private fun startElementID(view: View) {
        btnEnterDc = view.findViewById(R.id.btn_enter_dc)
    }
}