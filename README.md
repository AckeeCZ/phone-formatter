[ ![Download](https://api.bintray.com/packages/ackeecz/rxoauth2/rxoauth/images/download.svg) ](https://bintray.com/ackeecz/phoneformatter/_latestVersion)

# Android phone Formatter library

This library is replacement for systems [PhoneNumberFormattingTextWatcher](https://developer.android.com/reference/android/telephony/PhoneNumberFormattingTextWatcher) . It's a copy of this class rewritten to Kotlin with mutable countryCode property.

### Purpose
This library was written because in a lot of applications we have this kind of input for phone number

![alt text](https://github.com/AckeeCZ/phone-formatter/raw/master/images/phone_input.png) "Phone input example")

We have separated input (Spinner in this example) for phone prefix and separated input for the rest of the phone number.

If we initialize PhoneNumberFormattingTextWatcher without specifying country code, it will be formatted by system's locale country. That is not right because for example if I have phone in English (US) and I choose Czech phone prefix +420, the number is still formatted by US rules. It should be formatted according to Czech rules instead.

That is the reason why our own modification of this Formatter was created. When user changes phone prefix, formatter needs to be notified with new country code and it will be automatically reformatted.

### Usage

```kotlin
    private val textFormatter = PhoneNumberFormatter(Locale.getDefault().country)
    ...
    editPhone.addTextWatcher(textFormatter)

    // then when prefix is changed
    someInput.onPrefixChanged { country ->
        textFormatter.countryCode = country
    }
```

There is multiple solutions to have mapping from phone prefixes to country code:

1) Have a list of prefixes in the app along with the country code

```kotlin
val prefixes = mapOf(
    "CZ" to "+420",
    "DE" to "+49",
    ...
)
```

and then just perform lookup in this map when prefix is changed

2) Have a list of prefixes without the country association and use [LibPhoneNumber library](https://github.com/googlei18n/libphonenumber) to perform the lookup

```kotlin
input.onPrefixChanged { prefix -> // prefix like +420
       // method getRegionCodeForCountryCode accepts integer like 420 so we need tu strip the plus sign and convert it to integer
       textFormatter.countryCode = PhoneNumberUtil.getInstance().getRegionCodeForCountryCode(prefix.substring(1).toInt())
}
```

