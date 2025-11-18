package ru.netology.nmadia_hw.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmadia_hw.R
import ru.netology.nmadia_hw.databinding.ActivityMainBinding
import ru.netology.nmadia_hw.repository.PushRepository

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val REQ_NOTIFICATIONS = 1
        private const val ARG_INITIAL_CONTENT = "initial_content"
        private const val EXTRA_POST_ID = "EXTRA_POST_ID"
        private const val ARG_POST_ID = "POST_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        requestNotificationsPermission()

        handleIncomingShareIntent(intent)
        handlePushNavigation(intent)

        checkGoogleApiAvailability()
        requestFcmToken()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingShareIntent(intent)
        handlePushNavigation(intent)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        intent?.let { incoming ->
            if (incoming.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = incoming.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.error_empty_content,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        finish()
                    }
                    .show()
                return@let
            }

            // чтобы не обработать EXTRA_TEXT повторно, если придёт ещё один Intent
            incoming.removeExtra(Intent.EXTRA_TEXT)

            val args = Bundle().apply {
                // ключ совпадает с ARG_INITIAL_CONTENT в NewPostFragment
                putString(ARG_INITIAL_CONTENT, text)
            }

            findNavController(R.id.nav_host_fragment).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                args
            )
        }
    }

    private fun handlePushNavigation(intent: Intent?) {
        val incoming = intent ?: return

        val postId = incoming.getLongExtra(EXTRA_POST_ID, 0L)
        if (postId == 0L) return

        // чтобы при поворотах/перезапусках не навигировать повторно
        incoming.removeExtra(EXTRA_POST_ID)

        val args = Bundle().apply {
            putLong(ARG_POST_ID, postId)
        }

        findNavController(R.id.nav_host_fragment).navigate(
            R.id.action_feedFragment_to_postDetailsFragment,
            args
        )
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        requestPermissions(arrayOf(permission), REQ_NOTIFICATIONS)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@MainActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@MainActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(
                this@MainActivity,
                R.string.google_play_unavailable,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                val token = task.result
                println("FCM token: $token")
                // отправляем токен на фейковый backend
                PushRepository.sendPushToken(token)
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_NOTIFICATIONS) {
            // grantResults[0] == PackageManager.PERMISSION_GRANTED или DENIED
        }
    }
}
