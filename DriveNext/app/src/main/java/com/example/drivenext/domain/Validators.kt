package com.example.drivenext.domain

/** Чистая логика проверок без Android-классов: её легко покрыть unit-тестами. */
object Validators {

    const val LICENSE_LENGTH = 10

    // name@domainname.com / name@domainname.ru и любые другие домены с TLD от 2 букв
    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun isValidLicenseNumber(number: String): Boolean = number.trim().length == LICENSE_LENGTH
}
