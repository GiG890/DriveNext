package com.example.drivenext.presentation.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.widget.doAfterTextChanged
import com.example.drivenext.R
import com.example.drivenext.domain.DateRules
import com.example.drivenext.domain.Gender
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.showError
import com.example.drivenext.presentation.common.showMessage
import com.example.drivenext.presentation.common.showPastDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate

/** Регистрация, шаг 2: ФИО, дата рождения, пол. */
class RegistrationStep2Activity : AppCompatActivity() {

    private lateinit var baseData: RegistrationData

    private lateinit var tilBirthDate: TextInputLayout
    private lateinit var etLastName: TextInputEditText
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etPatronymic: TextInputEditText
    private lateinit var etBirthDate: TextInputEditText
    private lateinit var rgGender: RadioGroup
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = IntentCompat.getSerializableExtra(intent, EXTRA_DATA, RegistrationData::class.java)
        if (data == null) {
            finish()
            return
        }
        baseData = data

        enableEdgeToEdgeLight()
        setContentView(R.layout.activity_registration_step2)
        findViewById<View>(R.id.root).applySystemBarInsets()

        tilBirthDate = findViewById(R.id.tilBirthDate)
        etLastName = findViewById(R.id.etLastName)
        etFirstName = findViewById(R.id.etFirstName)
        etPatronymic = findViewById(R.id.etPatronymic)
        etBirthDate = findViewById(R.id.etBirthDate)
        rgGender = findViewById(R.id.rgGender)
        btnNext = findViewById(R.id.btnNext)

        // «Назад» закрывает экран: шаг 1 остаётся в стеке с уже введёнными данными
        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        etLastName.doAfterTextChanged { updateNextButton() }
        etFirstName.doAfterTextChanged { updateNextButton() }
        etBirthDate.doAfterTextChanged {
            tilBirthDate.showError(null)
            updateNextButton()
        }
        rgGender.setOnCheckedChangeListener { _, _ -> updateNextButton() }

        // Выбор даты: по иконке слева и по нажатию на само поле
        tilBirthDate.setStartIconOnClickListener { openBirthDatePicker() }
        etBirthDate.setOnClickListener { openBirthDatePicker() }

        btnNext.setOnClickListener { onNextClicked() }
        updateNextButton()
    }

    private fun openBirthDatePicker() {
        showPastDatePicker(
            title = getString(R.string.auth_label_birth_date),
            currentText = etBirthDate.text?.toString().orEmpty(),
            fallback = LocalDate.of(2000, 1, 1),
            tag = "birth_date_picker"
        ) { etBirthDate.setText(it) }
    }

    private fun requiredFieldsFilled(): Boolean =
        !etLastName.text.isNullOrBlank() &&
            !etFirstName.text.isNullOrBlank() &&
            !etBirthDate.text.isNullOrBlank() &&
            rgGender.checkedRadioButtonId != View.NO_ID

    private fun updateNextButton() {
        btnNext.isEnabled = requiredFieldsFilled()
    }

    private fun onNextClicked() {
        if (!requiredFieldsFilled()) {
            showMessage(R.string.auth_error_fill_required)
            return
        }

        val birthDate = etBirthDate.text?.toString().orEmpty().trim()
        if (!DateRules.isValidPastDate(birthDate)) {
            tilBirthDate.showError(getString(R.string.auth_error_birth_date))
            return
        }

        val gender = if (rgGender.checkedRadioButtonId == R.id.rbMale) Gender.MALE else Gender.FEMALE

        val data = baseData.copy(
            lastName = etLastName.text?.toString().orEmpty().trim(),
            firstName = etFirstName.text?.toString().orEmpty().trim(),
            patronymic = etPatronymic.text?.toString().orEmpty().trim(),
            birthDate = birthDate,
            gender = gender
        )
        startActivity(RegistrationStep3Activity.newIntent(this, data))
    }

    companion object {
        private const val EXTRA_DATA = "registration_data"

        fun newIntent(context: Context, data: RegistrationData): Intent =
            Intent(context, RegistrationStep2Activity::class.java).putExtra(EXTRA_DATA, data)
    }
}
