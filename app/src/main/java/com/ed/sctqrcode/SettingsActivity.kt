package com.ed.sctqrcode

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.*
import java.util.stream.IntStream.range
import kotlin.streams.toList


// todo: BUG -> run time exceptio NumberFormatException at java.lang.Long.parseLong
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
        prefs.registerOnSharedPreferenceChangeListener(this)
        if ("" in prefs.all.values) dialogAlertSettings()
    }

    override fun onPause(){
        super.onPause()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
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
            "settings_name" -> checkSettingName()
            "settings_iban" -> checkSettingIban()
            "settings_bic" -> checkSettingBic()
            else -> {}
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun checkSettingName() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val value = prefs.getString("settings_name", "")?.trim()
        val editor = prefs.edit()
        editor.putString("settings_name", value)
        editor?.commit()
    }

    // https://en.wikipedia.org/wiki/International_Bank_Account_Number
    @SuppressLint("ApplySharedPref")
    private fun checkSettingIban() {
        var valid = true
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val iban = prefs.getString("settings_iban", "")!!.replace("\\s".toRegex(), "")

        //check length
        if (iban.length > 34 || iban.length < 5) valid = false
        else{
            val countriesList = Locale.getISOCountries()
            val countryCode = iban.substring(0, 2).uppercase()
            val checkDigits = iban.substring(2, 4)
            // check land code
            if (countryCode !in countriesList) valid = false
            // check digits
            else if (! checkDigits.isDigitsOnly() && checkDigits.toInt() in 2..99) valid = false
            //check modulus
            val accountNrDigits = ibanReplaceChars(iban)
            val test = accountNrDigits % 97
            if (test.toInt() != 1){
                valid = false
            }
        }

        val editor = prefs.edit()
        if (valid) {
            val grouped =  iban.chunked(4).joinToString(" ")
            editor.putString("settings_iban", grouped)
        }
        else{
            editor.putString("settings_iban", "")
            // todo show alert dialog
        }
        editor.commit()
    }

    private fun ibanReplaceChars(iban: String): Long{
        val alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray()
        val nrs = range(10, 36).toList()
        val map: Map<Char, Int> = alphabet.zip(nrs).toMap()

        val movedChars = iban.substring(4) + iban.substring(0, 4)
        return movedChars.lowercase().map { map.getOrDefault(it, it) }.joinToString("").toLong()
    }

    // https://nl.wikipedia.org/wiki/Business_Identifier_Code
    @SuppressLint("ApplySharedPref")
    private fun checkSettingBic() {
        var valid = true
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val bic = prefs.getString("settings_bic", "")!!.replace("\\s".toRegex(), "")

        // check length
        if (bic.length != 8 && bic.length != 15) valid = false
        else {
            val countryCode = bic.substring(4, 6)
            val bankCode = bic.substring(0, 4)
            // check country code
            if (countryCode.uppercase() !in Locale.getISOCountries()) valid = false
            // check bank code
            for (char in bankCode.toCharArray()) {
                if (!char.isLetter()) valid = false
            }
        }

        val editor = prefs.edit()
        if (valid) editor.putString("settings_bic", bic)
        else{
            editor.putString("settings_bic", "")
            // todo show alert dialog
        }
        editor.commit()
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


