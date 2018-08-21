package cz.ackee.phoneformatter

import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.PhoneNumberUtil

/**
 * Copy of systems [PhoneNumberFormattingTechWatcher] that has mutable [countryCode] property. When
 * this property is changed the underlaying phone formatter is changed and the text is reformatted.
 */
class PhoneNumberFormatter(countryCode: String) : TextWatcher {

    private var mFormatter: AsYouTypeFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(countryCode)
    private var lastEditable: Editable? = null

    var countryCode: String = countryCode
        set(value) {
            field = value
            mFormatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(field)
            lastEditable?.let {
                afterTextChanged(it)
            }
        }

    /**
     * Indicates the change was caused by ourselves.
     */
    private var mSelfChange = false

    /**
     * Indicates the formatting has been stopped.
     */
    private var mStopFormatting: Boolean = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                   after: Int) {
        if (mSelfChange || mStopFormatting) {
            return
        }
        // If the user manually deleted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            stopFormatting()
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (mSelfChange || mStopFormatting) {
            return
        }
        // If the user inserted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            stopFormatting()
        }
    }

    @Synchronized override fun afterTextChanged(s: Editable) {
        if (mStopFormatting) {
            // Restart the formatting when all texts were clear.
            mStopFormatting = s.isNotEmpty()
            return
        }
        if (mSelfChange) {
            // Ignore the change caused by s.replace().
            return
        }
        val formatted = reformat(s, Selection.getSelectionEnd(s))
        if (formatted != null) {
            val rememberedPos = mFormatter.rememberedPosition
            mSelfChange = true
            s.replace(0, s.length, formatted, 0, formatted.length)
            // The text could be changed by other TextWatcher after we changed it. If we found the
            // text is not the one we were expecting, just give up calling setSelection().
            if (formatted == s.toString()) {
                Selection.setSelection(s, rememberedPos)
            }
            mSelfChange = false
        }
        lastEditable = s
    }

    /**
     * Generate the formatted number by ignoring all non-dialable chars and stick the cursor to the
     * nearest dialable char to the left. For instance, if the number is  (650) 123-45678 and '4' is
     * removed then the cursor should be behind '3' instead of '-'.
     */
    private fun reformat(s: CharSequence, cursor: Int): String? {
        // The index of char to the leftward of the cursor.
        val curIndex = cursor - 1
        var formatted: String? = null
        mFormatter.clear()
        var lastNonSeparator: Char = 0.toChar()
        var hasCursor = false
        val len = s.length
        for (i in 0 until len) {
            val c = s[i]
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator.toInt() != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor)
                    hasCursor = false
                }
                lastNonSeparator = c
            }
            if (i == curIndex) {
                hasCursor = true
            }
        }
        if (lastNonSeparator.toInt() != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor)
        }
        return formatted
    }

    private fun getFormattedNumber(lastNonSeparator: Char, hasCursor: Boolean): String {
        return if (hasCursor)
            mFormatter.inputDigitAndRememberPosition(lastNonSeparator)
        else
            mFormatter.inputDigit(lastNonSeparator)
    }

    private fun stopFormatting() {
        mStopFormatting = true
        mFormatter.clear()
    }

    private fun hasSeparator(s: CharSequence, start: Int, count: Int): Boolean {
        for (i in start until start + count) {
            val c = s[i]
            if (!PhoneNumberUtils.isNonSeparator(c)) {
                return true
            }
        }
        return false
    }
}