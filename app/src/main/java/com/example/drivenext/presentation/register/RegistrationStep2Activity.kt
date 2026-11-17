package com.example.drivenext.presentation.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.widget.doAfterTextChanged
import com.example.drivenext.R
import com.example.drivenext.domain.Gender
import com.example.drivenext.domain.RegistrationData
import com.example.drivenext.presentation.common.applySystemBarInsets
import com.example.drivenext.presentation.common.enableEdgeToEdgeLight
import com.example.drivenext.presentation.common.showError
import com.example.drivenext.presentation.common.showErrorDialog
import com.example.drivenext.presentation.common.showPastDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate

/** Регистрация, шаг 2: ФИО, дата рождения, пол. */
class RegistrationStep2Activity : AppCompatActivity() {

    private val viewModel: RegistrationStep2ViewModel by viewModels()

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

        etLastName.doAfterTextChanged { notifyFieldsChanged() }
        etFirstName.doAfterTextChanged { notifyFieldsChanged() }
        etBirthDate.doAfterTextChanged {
            tilBirthDate.showError(null)
            notifyFieldsChanged()
        }
        rgGender.setOnCheckedChangeListener { _, _ -> notifyFieldsChanged() }

        // Выбор даты: по иконке слева и по нажатию на само поле
        tilBirthDate.setStartIconOnClickListener { openBirthDatePicker() }
        etBirthDate.setOnClickListener { openBirthDatePicker() }

        btnNext.setOnClickListener {
            viewModel.onNextClicked(
                base = baseData,
                lastName = etLastName.text?.toString().orEmpty(),
                firstName = etFirstName.text?.toString().orEmpty(),
                patronymic = etPatronymic.text?.toString().orEmpty(),
                birthDate = etBirthDate.text?.toString().orEmpty(),
                gender = selectedGender()
            )
        }

        viewModel.nextEnabled.observe(this) { btnNext.isEnabled = it }
        viewModel.events.observe(this) { event ->
            event.getContentIfNotHandled()?.let { handle(it) }
        }
        notifyFieldsChanged()
    }

    private fun openBirthDatePicker() {
        showPastDatePicker(
            title = getString(R.string.auth_label_birth_date),
            currentText = etBirthDate.text?.toString().orEmpty(),
            fallback = LocalDate.of(2000, 1, 1),
            tag = "birth_date_picker"
        ) { etBirthDate.setText(it) }
    }

    private fun selectedGender(): Gender? = when (rgGender.checkedRadioButtonId) {
        R.id.rbMale -> Gender.MALE
        R.id.rbFemale -> Gender.FEMALE
        else -> null
    }

    private fun notifyFieldsChanged() {
        viewModel.onFieldsChanged(
            lastName = etLastName.text?.toString().orEmpty(),
            firstName = etFirstName.text?.toString().orEmpty(),
            birthDate = etBirthDate.text?.toString().orEmpty(),
            gender = selectedGender()
        )
    }

    private fun handle(event: Step2Event) {
        when (event) {
            is Step2Event.RequiredMissing -> showErrorDialog(R.string.auth_error_fill_required)
            is Step2Event.InvalidBirthDate ->
                tilBirthDate.showError(getString(R.string.auth_error_birth_date))
            is Step2Event.Proceed ->
                startActivity(RegistrationStep3Activity.newIntent(this, event.data))
        }
    }

    companion object {
        private const val EXTRA_DATA = "registration_data"

        fun newIntent(context: Context, data: RegistrationData): Intent =
            Intent(context, RegistrationStep2Activity::class.java).putExtra(EXTRA_DATA, data)
    }
}
