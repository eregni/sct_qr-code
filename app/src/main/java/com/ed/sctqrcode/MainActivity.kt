package com.ed.sctqrcode

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.PreferenceManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.*

class MainActivity : AppCompatActivity() {
    // todo: DEBUG: Skipped 33 frames!  The application may be doing too much work on its main thread. ???
    // todo Enable data binding -> https://developer.android.com/codelabs/kotlin-android-training-data-binding-basics#2
    // todo Readme.md
    private val _prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        }

    private fun preferencesMissing(): Boolean {
        val name: Boolean = _prefs.getString("settings_name", "").isNullOrBlank()
        val iban: Boolean = _prefs.getString("settings_iban", "").isNullOrBlank()
        return name || iban
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.label_name_value).text =
            "${_prefs.getString("settings_name", getString(R.string.not_set))}"
        findViewById<TextView>(R.id.label_iban_value).text =
            "${_prefs.getString("settings_iban", getString(R.string.not_set))}"
        findViewById<TextView>(R.id.label_bic_value).text =
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

    fun handlerGenerateQrCode(view: View){
        val inputAmount = findViewById<EditText>(R.id.input_amount).text
        if (inputAmount.isEmpty()) return
        val inputMessage = findViewById<EditText>(R.id.input_message).text
        val qrView = findViewById<ImageView>(R.id.imageView_qr_code)
        val qrCode = generateQrCode(inputAmount.toString(), inputMessage.toString())
        qrView.setImageBitmap(qrCode)
        closeKeyBoard()
    }

    private fun generateQrCode(amount: String, message: String): Bitmap{
        //Guidelines: https://www.europeanpaymentscouncil.eu/sites/default/files/KB/files/EPC069-12%20v2.1%20Quick%20Response%20Code%20-%20Guidelines%20to%20Enable%20the%20Data%20Capture%20for%20the%20Initiation%20of%20a%20SCT.pdf
        val qrView = findViewById<ImageView>(R.id.imageView_qr_code)
        val hints = HashMap<EncodeHintType, ErrorCorrectionLevel>()
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
        val iban = _prefs.getString("settings_iban", "")!!.replace("\\s".toRegex(), "")
        val qrString =
            "BCD\n002\n1\nSCT\n" +
            "${_prefs.getString("settings_bic", "")}\n" +
            "${_prefs.getString("settings_name", "")}\n" +
            "${iban}\n" +
            "EUR$amount\n\n\n" +
            message
        val bitMatrix: BitMatrix = QRCodeWriter().encode(qrString, BarcodeFormat.QR_CODE,
            qrView.width, qrView.height, hints)
        return BarcodeEncoder().createBitmap(bitMatrix)
    }

    private fun dialogMenuAbout(){
        val dialog = AlertDialog.Builder(this)
        // todo: about text...
        dialog.setTitle("Spaceman Spiff!")
        dialog.setMessage("")
        dialog.setIcon(AppCompatResources.getDrawable(this, R.drawable.spiff))
        dialog.show()
    }

    private fun menuOpenSettings(){
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
