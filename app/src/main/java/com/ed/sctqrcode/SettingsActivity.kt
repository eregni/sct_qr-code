package com.ed.sctqrcode

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager


class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        //prefs.registerOnSharedPreferenceChangeListener(this)      // todo: listener crashes app
        if ("" in prefs.all.values) dialogAlertSettings()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when (item.itemId) {
            android.R.id.home -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(this).all
                if ("" in prefs.values) dialogAlertSettings()
                else finish()
                true
            }
            else ->
                return super.onOptionsItemSelected(item)
        })
    }

    override fun onSharedPreferenceChanged(preference: SharedPreferences?, key: String?) {
        when(key){
            "settings_name" -> updateSettingName()
            "settings_iban" -> updateSettingIban()
            "settings_bic" -> updateSettingBic()
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun updateSettingName() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val value = prefs.getString("settings_name", "")?.trim()
        if (value!!.isNotBlank()) findViewById<TextView>(R.id.label_name_value).text = value
        val editor = prefs.edit()
        editor.putString("settings_name", value)
        editor?.commit()
    }

    private fun updateSettingIban() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val value = prefs.getString("settings_iban", "")        // todo check iban
        if (value!!.isNotBlank()) findViewById<TextView>(R.id.label_name_value).text = value
    }

    private fun updateSettingBic() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val value = prefs.getString("settings_bic", "")         // todo check bic
        if (value!!.isNotBlank()) findViewById<TextView>(R.id.label_name_value).text = value
    }

    private fun dialogAlertSettings(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(getString(R.string.dialog_setup_title))
        dialog.setMessage(getString(R.string.dialog_setup_message))
        dialog.setPositiveButton("Ok", null)
        dialog.setCancelable(false)
        dialog.show()
    }
}

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}