package com.apozas.contactdiary

import android.app.*
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        createNotificationChannel()
        createNotification()

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val onlyRisky = preferences.getBoolean("closecontactonly", false)

        restrict15LastDays()
        viewData(onlyRisky)

        diarytable.setOnItemClickListener { adapterView, view, position, id ->
            val idx = diarytable.adapter.getItemId(position)
            val entry = diarytable.adapter.getItem(position) as Cursor

            val contactType = entry.getString(entry.getColumnIndex(ContactDatabase.ContactDatabase.FeedEntry.TYPE_COLUMN))

            if (contactType == "Contact") {
                val intent = Intent(this@MainActivity, EditContactActivity::class.java)
                intent.putExtra("entry", idx.toString())
                startActivity(intent)
            } else if (contactType == "Event") {
                val intent = Intent(this@MainActivity, EditEventActivity::class.java)
                intent.putExtra("entry", idx.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Something very wrong has happened", Toast.LENGTH_LONG).show()
            }
        }

        fab.setOnClickListener {
            animateFAB()
        }
    }

    override fun onResume() {
        super.onResume()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val onlyRisky = preferences.getBoolean("closecontactonly", false)
        viewData(onlyRisky)
        createNotification()
    }

    override fun onBackPressed() {
        if (isFabOpen) {
            collapseFAB()
        } else {
            super.onBackPressed()
        }
    }

//  FAB animations
    fun expandFAB() {
        val fab_open = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        val rotate_forward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_forward)

        fab.startAnimation(rotate_forward)
//      fab1.animate().translationY(0.toFloat());
//      fab2.animate().translationY(0.toFloat());
        fab1.startAnimation(fab_open)
        fabText1.startAnimation(fab_open)
        fab2.startAnimation(fab_open)
        fabText2.startAnimation(fab_open)
        fab1.isClickable = true
        fab2.isClickable = true
        isFabOpen = true
    }

    fun collapseFAB() {
        val fab_close: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        val rotate_backward: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_backward)

        fab.startAnimation(rotate_backward)
        fab1.startAnimation(fab_close)
        fabText1.startAnimation(fab_close)
        fab2.startAnimation(fab_close)
        fabText2.startAnimation(fab_close)
//      fab1.animate().translationY(-resources.getDimension(R.dimen.standard_55));
//      fab2.animate().translationY(-resources.getDimension(R.dimen.standard_105));
        fab1.isClickable = false
        fab2.isClickable = false
        isFabOpen = false
    }

    fun animateFAB() {
        if (isFabOpen) {
            collapseFAB()
        } else {
            expandFAB()
        }
    }

//  New button actions
    fun addContact(v: View): Unit {
        startActivity(Intent(this@MainActivity, NewContactActivity::class.java))
    }

    fun addEvent(v: View): Unit {
        startActivity(Intent(this@MainActivity, NewEventActivity::class.java))
    }

    fun openSettings(v: View): Unit {
        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
    }

//    Database operation
    val dbHelper = FeedReaderDbHelper(this)

    fun viewData(onlyRisky: Boolean) {
        val cursor = dbHelper.viewData(onlyRisky)

        val adapter = SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
            cursor, arrayOf(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN),
            intArrayOf(android.R.id.text1), 0)

        diarytable.adapter = adapter
    }

    fun restrict15LastDays() {
        val db = dbHelper.writableDatabase
//      Create Calendar set to 15 days ago
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -15)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.HOUR, 0)
        val fifteenDaysAgo = cal.timeInMillis

        // Define 'where' part of query.
        val selection = "DELETE FROM ${ContactDatabase.ContactDatabase.FeedEntry.TABLE_NAME} " +
                "WHERE ${ContactDatabase.ContactDatabase.FeedEntry.DATETIME_COLUMN} <= " + fifteenDaysAgo.toString()
        // Issue SQL statement.
        db.execSQL(selection)
    }
//  Notification codes
    fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "channel"
            val descriptionText = "Notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("473X", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val time = preferences.getString("reminder_time", "21:00") as String
        val timeparts = time.split(':')

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, timeparts[0].toInt())
        cal.set(Calendar.MINUTE, timeparts[1].toInt() - 1)  // Weird 1-minute fix
        if (cal.time.compareTo(Date()) < 0) cal.add(Calendar.DAY_OF_MONTH, 1)

        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext,0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarm_toggle = preferences.getBoolean("reminder_toggle", false)

        if (alarm_toggle) {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, cal.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

}