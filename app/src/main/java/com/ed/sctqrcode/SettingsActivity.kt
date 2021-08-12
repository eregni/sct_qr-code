package com.ed.sctqrcode

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.util.*
import java.util.stream.IntStream.range
import kotlin.streams.toList


class SettingsActivity : AppCompatActivity() {
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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else ->
                return super.onOptionsItemSelected(item)
        })
    }
}


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener  {

    private val _prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        if ("" in _prefs) dialogAlertSettings()
    }

    override fun onSharedPreferenceChanged(preference: SharedPreferences?, key: String?) {
        when(key){
            "settings_name" -> checkSettingName()
            "settings_iban" -> checkSettingIban()
            "settings_bic" -> checkSettingBic()
            else -> {}
        }
        val pref = findPreference<EditTextPreference>(key!!)
        var value = _prefs.all[key].toString()
        if (value.isBlank()) value = getString(R.string.not_set)
        pref?.setSummaryProvider { value }
    }

    @SuppressLint("ApplySharedPref")
    private fun checkSettingName() {
        var value = _prefs.getString("settings_name", "")!!.trim()
        if (value.length > 70) {
            dialogIncorrectValue(getString(R.string.incorrect_name_value))
            value = ""
        }
        val editor = _prefs.edit()
        editor.putString("settings_name", value)
        editor.commit()
    }

    // https://en.wikipedia.org/wiki/International_Bank_Account_Number
    @SuppressLint("ApplySharedPref")
    private fun checkSettingIban() {
        var valid = true
        val iban = _prefs.getString("settings_iban", "")!!.replace("\\s".toRegex(), "").uppercase()
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

        val editor = _prefs.edit()
        if (valid) {
            val grouped =  iban.chunked(4).joinToString(" ")
            editor.putString("settings_iban", grouped)
        }
        else{
            editor.putString("settings_iban", "")
            dialogIncorrectValue(getString(R.string.invalid_iban_value))
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
        val bic = _prefs.getString("settings_bic", "")!!.replace("\\s".toRegex(), "").uppercase()
        // return when bic is deleted
        if (bic.isEmpty()) return
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

        val editor = _prefs.edit()
        if (valid) editor.putString("settings_bic", bic)
        else{
            editor.putString("settings_bic", "")
            dialogIncorrectValue(getString(R.string.invalid_bic_value))
        }
        editor.commit()
    }

    private fun dialogAlertSettings(){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(getString(R.string.dialog_setup_title))
        dialog.setMessage(getString(R.string.dialog_setup_message))
        dialog.setPositiveButton("Ok", null)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun dialogIncorrectValue(message: String){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(getString(R.string.incorrect_value))
        dialog.setMessage(message)
        dialog.setPositiveButton("Ok", null)
        dialog.setCancelable(true)
        dialog.show()
    }

    override fun onPause(){
        super.onPause()
        _prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        _prefs.registerOnSharedPreferenceChangeListener(this)
    }
}
