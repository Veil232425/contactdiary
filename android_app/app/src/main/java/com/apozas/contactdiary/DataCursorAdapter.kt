package com.apozas.contactdiary

/*
    This file is part of COVID Diary.
    COVID Diary is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    COVID Diary is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with COVID Diary. If not, see <http://www.gnu.org/licenses/>.
    Copyright 2020 by Alex Pozas-Kerstjens (apozas)
*/

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cursoradapter.widget.CursorAdapter
import java.text.DateFormat
import java.util.*

class DataCursorAdapter(context: Context?, c: Cursor?) : CursorAdapter(context, c, 0) {
    private var mDateColumnIndex = cursor.getColumnIndex(ContactDatabase.ContactDatabase.FeedEntry.DATETIME_COLUMN)
    val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM)
    val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return inflater.inflate(R.layout.list_layout, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        var contact : String = ""
        if (cursor != null) {
            contact = cursor.getString(
                cursor.getColumnIndex(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN))
        }

        val list_item = view?.findViewById<TextView>(R.id.list_item) as TextView
        list_item.text = contact
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        if (convertView == null) {
            convertView = inflater.inflate(
                R.layout.list_layout, parent, false
            )
        }
//      Set the data for the row
        cursor.moveToPosition(position)
        val listItemHeader = convertView?.findViewById<TextView>(R.id.list_item_header) as TextView
        val listItem = convertView?.findViewById<TextView>(R.id.list_item) as TextView
        val listDivider = convertView?.findViewById<View>(R.id.list_divider) as View
        listItem.text = cursor.getString(cursor.getColumnIndex(ContactDatabase.ContactDatabase.FeedEntry.NAME_COLUMN))

        if (position - 1 >= 0) {
//          If there is a previous position see if it has the same date
            val currentDate = formatter.format(Date(cursor.getLong(mDateColumnIndex)))
            cursor.moveToPosition(position - 1)
            val previousDate = formatter.format(Date(cursor.getLong(mDateColumnIndex)))
            if (currentDate.equals(previousDate, ignoreCase = true)) {
//              The dates are the same so abort everything as we already set the header before
                listItemHeader.visibility = View.GONE
                listDivider.visibility = View.VISIBLE
            } else {
//              This is the first occurrence of this date so show the header
                listItemHeader.visibility = View.VISIBLE
                listItemHeader.text = currentDate
                listDivider.visibility = View.GONE
            }
        } else {
//          This is position 0 and we need a header here
            listItemHeader.visibility = View.VISIBLE
            listItemHeader.text = formatter.format(Date(cursor.getLong(mDateColumnIndex)))
            listDivider.visibility = View.GONE
        }
        return convertView
    }
}