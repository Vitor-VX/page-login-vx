package victor.vx.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import victor.vx.app.databinding.ActivityMainBinding
import victor.vx.app.fragments.AccountFragment
import victor.vx.app.fragments.DiscordFragment
import victor.vx.app.fragments.LoginFragment

class MainActivity : AppCompatActivity() {

    init {
        System.loadLibrary("detect")
    }

    private lateinit var binding: ActivityMainBinding
    private var btnNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startElementID()
        mainActivityView = findViewById(android.R.id.content)

        btnNavigationView?.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null

            when (item.itemId) {
                R.id.navigation_login -> selectedFragment = LoginFragment(this)
                R.id.navigation_discord -> selectedFragment = DiscordFragment()
                R.id.navigation_username -> selectedFragment = AccountFragment(this)
            }

            if (selectedFragment != null) {
                initFragment(selectedFragment)
            }

            true
        }

        if (savedInstanceState == null) {
            val initialFragment = LoginFragment(this)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, initialFragment)
                .commit()
            btnNavigationView?.selectedItemId = R.id.navigation_login
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun startElementID() {
        btnNavigationView = findViewById(R.id.bottom_navigation)
    }

    private fun initFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "vxinjector" && data.host == "login-success") {
            val token = data.getQueryParameter("token")
            if (token != null) {
                navigateToAccountFragment(token)
            }
        }
    }

    private fun navigateToAccountFragment(token: String) {
        val bundle = Bundle().apply {
            putString("token", token)
        }

        val fragment = AccountFragment(this).apply {
            arguments = bundle
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var mainActivityView: View? = null
    }
}