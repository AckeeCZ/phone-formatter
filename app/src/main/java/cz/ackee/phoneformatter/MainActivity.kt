package cz.ackee.phoneformatter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val textFormatter = PhoneNumberFormatter(Locale.getDefault().country)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edit_phone.addTextChangedListener(textFormatter)

        btn_czech.setOnClickListener { textFormatter.countryCode = "CZ" }
        btn_english.setOnClickListener { textFormatter.countryCode = "US" }
        btn_german.setOnClickListener { textFormatter.countryCode = "DE" }
    }
}
