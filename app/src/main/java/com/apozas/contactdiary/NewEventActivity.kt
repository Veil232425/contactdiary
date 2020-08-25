package com.apozas.contactdiary

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_addcontact.*
import kotlinx.android.synthetic.main.activity_addevent.*
import kotlinx.android.synthetic.main.activity_editcontact.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class NewEventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addevent)
        setSupportActionBar(findViewById(R.id.toolbar))

        var cal = Calendar.getInstance()

        // Set current values
        eventdate_input.setText(DateFormat.getDateInstance().format(cal.time))
        eventtime_input.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.time))

        // Listen to new values
        val eventdateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            eventdate_input.setText(DateFormat.getDateInstance().format(cal.time))

        }

        eventdate_input.setOnClickListener {
            DatePickerDialog(this@NewEventActivity, eventdateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val eventtimeSetListener = TimePickerDialog.OnTimeSetListener { view, hour, minute ->
//            cal.set(Calendar.HOUR_OF_DAY, hour)
//            cal.set(Calendar.MINUTE, minute)
//
//            eventtime_input.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.time))

        }

        eventtime_input.setOnClickListener {
            TimePickerDialog(this@NewEventActivity, eventtimeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true).show()
        }

//      Database operation
        val dbHelper = FeedReaderDbHelper(this)

        okButton_AddEvent.setOnClickListener {
//          Gets the data repository in write mode
            val db = dbHelper.writableDatabase

            var errorCount = 0

//          Process RadioButtons
            val eventIndoorOutdoorId = event_indoor_outdoor.checkedRadioButtonId
            var eventIndoorOutdoorChoice = -1
            if (eventIndoorOutdoorId != -1) {
                val btn: View = event_indoor_outdoor.findViewById(eventIndoorOutdoorId)
                eventIndoorOutdoorChoice = event_indoor_outdoor.indexOfChild(btn)
            } else {
                event_encounter_question.requestFocus()
                event_encounter_question.error = getString(R.string.choose_option)
                errorCount++
            }

            val eventCloseContactId = eventclosecontact.checkedRadioButtonId
            var eventCloseContactChoice = -1
            if (eventCloseContactId != -1) {
                val btn: View = eventclosecontact.findViewById(eventCloseContactId)
                eventCloseContactChoice = eventclosecontact.indexOfChild(btn)
            } else {
                closecontact_question.requestFocus()
                closecontact_question.error = getString(R.string.choose_option)
                errorCount++
            }

//          Compulsory text fields
            val eventName = eventname_input.getText().toString()
            if (eventName.length == 0) {
                eventname_input.error = getString(R.string.compulsory_field)
                errorCount++
            }
            val eventPlace = eventplace_input.getText().toString()
            if (eventPlace.length == 0) {
                eventplace_input.error = getString(R.string.compulsory_field)
                errorCount++
            }
//          Create a new map of values, where column names are the keys
            if (errorCount == 0) {
                val values = ContentValues().apply {
                    put(ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN, "Event")
                    put(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN, eventName)
                    put(ContactDatabase.ContactDatabase.FeedEntry.PLACE_COLUMN, eventPlace)
                    put(ContactDatabase.ContactDatabase.FeedEntry.DATETIME_COLUMN, cal.timeInMillis)
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.PHONE_COLUMN,
                        eventphone_input.getText().toString()
                    )
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.COMPANIONS_COLUMN,
                        eventpeople_input.getText().toString()
                    )
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.ENCOUNTER_COLUMN,
                        eventIndoorOutdoorChoice
                    )
                    put(
                        ContactDatabase.ContactDatabase.FeedEntry.CLOSECONTACT_COLUMN,
                        eventCloseContactChoice
                    )
                }

//              Insert the new row, returning the primary key value of the new row
                val newRowId =
                    db?.insert(ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME, null, values)

                Toast.makeText(
                    applicationContext,
                    applicationContext.getResources().getString(R.string.event_saved),
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
        }
    }
}