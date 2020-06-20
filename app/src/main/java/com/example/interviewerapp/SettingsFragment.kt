package com.example.interviewerapp

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.text.SimpleDateFormat


class SettingsFragment : PreferenceFragmentCompat() {

    var setHour = 0
    var setMin = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        Log.e("SETTINGSDEBUG","Gets to onCreate")
    }


    override fun onAttach(context: Context) {

        Log.e("SETTINGSDEBUG","Gets to onAttach")

        val timeSetPref: Preference? = findPreference("timesetter")
        Log.e("SETTINGSDEBUG","Finds Preference")

        if (timeSetPref != null) {
            timeSetPref.setOnPreferenceClickListener {

                val timePickerDialog =
                    TimePickerDialog(context, object : TimePickerDialog.OnTimeSetListener {

                        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

                            timeSetPref.summary = hourOfDay.toString() + ":" + minute.toString()
                        }
                    }, setHour, setMin, DateFormat.is24HourFormat(context))
                timePickerDialog.show()


                true
            }
        }

        super.onAttach(context)
    }

}
