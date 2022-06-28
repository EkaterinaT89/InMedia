package ru.netology.inmedia

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.inmedia.auth.AppAuth
import ru.netology.inmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var navController: NavController

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.auth_menu, menu)

        menu.let {
            it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_in -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.authFragment)
                true
            }
            R.id.sign_up -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.signUpFragment)
                true
            }
            R.id.sign_out -> {
                AlertDialog.Builder(this).setMessage("Уверены?")
                    .setPositiveButton(
                        "Выйти"
                    ) { dialogInterface, i ->
                        auth.removeAuth()
                        findNavController(R.id.nav_host_fragment).navigateUp()
                    }
                    .setNegativeButton(
                        "Остаться"
                    ) { dialogInterface, i ->
                        return@setNegativeButton
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}