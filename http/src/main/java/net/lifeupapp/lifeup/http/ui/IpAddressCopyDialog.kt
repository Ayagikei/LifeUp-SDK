package net.lifeupapp.lifeup.http.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.lifeupapp.lifeup.http.R
import net.lifeupapp.lifeup.http.databinding.DialogCopyIpBinding

internal fun Context.showIpAddressCopyDialog(ipAddresses: List<String>, port: Int): AlertDialog {
    val binding = DialogCopyIpBinding.inflate(LayoutInflater.from(this))
    var values = ipAddresses
    val adapter = ArrayAdapter(this, R.layout.item_copy_ip_address, ArrayList(values))
    binding.ipAddresses.adapter = adapter

    binding.copyFormat.addOnButtonCheckedListener { _, checkedId, isChecked ->
        if (!isChecked) return@addOnButtonCheckedListener
        values = when (checkedId) {
            R.id.format_ip -> ipAddresses
            R.id.format_port -> listOf(port.toString())
            R.id.format_ip_port -> ipAddresses.map { ip ->
                // IPv6 addresses need brackets to distinguish the host from the port.
                if (':' in ip) "[$ip]:$port" else "$ip:$port"
            }
            else -> return@addOnButtonCheckedListener
        }
        adapter.clear()
        adapter.addAll(values)
    }

    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle(R.string.copy_ip_dialog_title)
        .setView(binding.root)
        .setNegativeButton(android.R.string.cancel, null)
        .create()
    binding.ipAddresses.setOnItemClickListener { _, _, position, _ ->
        val value = values[position]
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.copy_ip_dialog_title), value))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, getString(R.string.cloud_scheme_copied, value), Toast.LENGTH_SHORT).show()
        }
        dialog.dismiss()
    }
    dialog.show()
    return dialog
}
