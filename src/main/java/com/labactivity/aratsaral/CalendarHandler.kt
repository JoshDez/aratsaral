package com.labactivity.aratsaral

import android.app.DatePickerDialog
import com.labactivity.aratsaral.databinding.ActivityTaskViewBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarHandler() {

    private lateinit var binding:ActivityTaskViewBinding
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private var currentCal:Calendar = Calendar.getInstance()
    private var currentDay:Int = currentCal.get(Calendar.DAY_OF_MONTH)
    private var currentMonth:Int = currentCal.get(Calendar.MONTH)
    private var currentYear:Int = currentCal.get(Calendar.YEAR)
    private var deadline = ""

    constructor(binding: ActivityTaskViewBinding) : this() {
        this.binding = binding
    }

    //Displays date picker
    fun showDatePicker(date:String){
        val context = binding.datePickerLinear.context
        deadline = date

        val datePickerDialog = DatePickerDialog(context, { _, year:Int, monthOfYear:Int, dayOfMonth:Int ->
            val date:Calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            date.set(year, monthOfYear, dayOfMonth)
            val selectedDate = dateFormat.format(date.time)
            if (date >= currentCal){
                deadline = selectedDate
                bindDeadline(deadline)
            }
        },
            currentYear,
            currentMonth,
            currentDay,
        )
        datePickerDialog.show()
    }

    fun compareDateToPresent(date: String): Int {
        val parsedTDate = parseDate(date)
        val parsedCDate = parseDate(getCurrentDate())
        val targetDate = "${parsedTDate[0]}${parsedTDate[1]}${parsedTDate[2]}"
        val currentDate = "${parsedCDate[0]}${parsedCDate[1]}${parsedCDate[2]}"
        return currentDate.toInt() - targetDate.toInt()
    }

    private fun parseDate(date:String):ArrayList<String>{
        //separating date fields
        val parsedDate: ArrayList<String> = if (date != "null" && date != ""){
            date.split('/') as ArrayList<String>
        } else {
            getCurrentDate().split('/') as ArrayList<String>
        }

        //Adding the name of the month at the end of the list
        when(parsedDate[1]){
            "01" -> parsedDate.add("January")
            "02" -> parsedDate.add("February")
            "03" -> parsedDate.add("March")
            "04" -> parsedDate.add("April")
            "05" -> parsedDate.add("May")
            "06" -> parsedDate.add("June")
            "07" -> parsedDate.add("July")
            "08" -> parsedDate.add("August")
            "09" -> parsedDate.add("September")
            "10" -> parsedDate.add("October")
            "11" -> parsedDate.add("November")
            "12" -> parsedDate.add("December")
        }
        return parsedDate
    }

    fun bindDeadline(deadline:String){
        //Parse Date
        this.deadline = deadline
        val parsedDate:ArrayList<String> = CalendarHandler().parseDate(this.deadline)
        //Bind Date
        binding.dateTV.text = "${parsedDate[3]}    ${parsedDate[2]},   ${parsedDate[0]}"
    }

    fun getCurrentDate():String{
        val currentDate:Calendar = Calendar.getInstance()
        return dateFormat.format(currentDate.time)
    }

    fun getDeadline():String{
        return deadline
    }
}