package com.eregni.sctqrcode

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import com.eregni.sctqrcode.databinding.ActivityMainBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

class MainActivity : AppCompatActivity() {
    // todo: Warning:(6, 9) On SDK version 23 and up, your app data will be automatically backed up and restored on app install. Consider adding the attribute `android:fullBackupContent` to specify an `@xml` resource which configures which files to backup, or just set `android:fullBackupOnly=true`. More info: https://developer.android.com/guide/topics/data/autobackup
    // todo: DEBUG: Skipped 33 frames!  The application may be doing too much work on its main thread. -> move qr code generation to separate class
    // todo Readme.md
    private val DEVMODE = false  // Set this true to use default some default settings values

    private lateinit var _binding: ActivityMainBinding
    private val _prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (DEVMODE) {
            val editor = _prefs.edit()
            editor.putString("settings_name", "spaceman spiff")
            editor.putString("settings_iban", "IT69R0300203280616899586685")  // random iban
            editor.putString("settings_bic", "PASCITM1")
            editor.apply()
        }
    }

    private fun preferencesMissing(): Boolean {
        val name: Boolean = _prefs.getString("settings_name", "").isNullOrBlank()
        val iban: Boolean = _prefs.getString("settings_iban", "").isNullOrBlank()
        return name || iban
    }

    override fun onResume() {
        super.onResume()
        _binding.labelNameValue.text =
            "${_prefs.getString("settings_name", getString(R.string.not_set))}"
        _binding.labelIbanValue.text =
            "${_prefs.getString("settings_iban", getString(R.string.not_set))}"
        _binding.labelBicValue.text =
            "${_prefs.getString("settings_bic", getString(R.string.not_set))}"
        if (preferencesMissing()) menuOpenSettings()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when (item.itemId) {
            R.id.menu_settings -> {
                menuOpenSettings()
                true
            }
            R.id.menu_about -> {
                dialogMenuAbout()
                true
            }
            else ->
                return super.onContextItemSelected(item)
        })
    }

    fun handlerGenerateQrCode(view: View) {
        val inputAmount = _binding.inputAmount.text
        val qrView = _binding.imageViewQrCode
        if (inputAmount.isEmpty()) {
            qrView.visibility = View.INVISIBLE
            return
        }
        val inputMessage = _binding.inputMessage.text
        val qrCode = generateQrCode(inputAmount.toString(), inputMessage.toString())
        qrView.setImageBitmap(qrCode)
        qrView.visibility = View.VISIBLE
        closeKeyBoard()
    }

    private fun generateQrCode(amount: String, message: String): Bitmap {
        //Guidelines: https://www.europeanpaymentscouncil.eu/sites/default/files/KB/files/EPC069-12%20v2.1%20Quick%20Response%20Code%20-%20Guidelines%20to%20Enable%20the%20Data%20Capture%20for%20the%20Initiation%20of%20a%20SCT.pdf
        val mainLayoutHeight = _binding.mainLayout.measuredHeight
        val size = mainLayoutHeight / 2
        val options = HashMap<EncodeHintType, ErrorCorrectionLevel>()
        options[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
        val iban = _prefs.getString("settings_iban", "")!!.replace("\\s".toRegex(), "")
        val qrString =
            "BCD\n002\n1\nSCT\n" +
                    "${_prefs.getString("settings_bic", "")}\n" +
                    "${_prefs.getString("settings_name", "")}\n" +
                    "${iban}\n" +
                    "EUR$amount\n\n\n" +
                    message
        val encoder = BarcodeEncoder()
        return encoder.encodeBitmap(qrString, BarcodeFormat.QR_CODE, size, size, options)
    }

    private fun dialogMenuAbout() {
        val dialog = AlertDialog.Builder(this)
        // todo: about text...
        dialog.setTitle("Spaceman Spiff!")
        dialog.setMessage("")
        dialog.setIcon(AppCompatResources.getDrawable(this, R.drawable.spiff))
        dialog.show()
    }

    private fun menuOpenSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    // Close virtual keyboard.
    // Source: (Solution no. 11) -> https://izziswift.com/kotlin-close-hide-the-android-soft-keyboard-with-kotlin/
    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
