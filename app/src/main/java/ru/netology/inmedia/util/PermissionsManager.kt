package ru.netology.inmedia.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.netology.inmedia.R

class PermissionsManager(val activity: Activity, val list: List<String>, val code: Int) {

    fun checkPermissions(): Boolean {
        return isPermissionsGranted() == PackageManager.PERMISSION_GRANTED
    }

    private fun isPermissionsGranted(): Int {
        var counter = 0
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }

    private fun deniedPermission(): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_DENIED
            ) return permission
        }
        return ""
    }

    private fun showAlert(permission: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.permissions_needed_title))
        builder.setMessage(activity.getString(R.string.permissions_needed_message))
        builder.setPositiveButton(activity.getString(R.string.everything_fine)) { dialog, which ->
            ActivityCompat.requestPermissions(activity, arrayOf(permission), code)

        }
        builder.setNeutralButton(activity.getString(R.string.action_cancel), null)
        val dialog = builder.create()
        dialog.show()
    }

    fun requestPermissions() {
        val permission = deniedPermission()
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            showAlert(permission)
        } else {
            ActivityCompat.requestPermissions(activity, list.toTypedArray(), code)
        }
    }
}