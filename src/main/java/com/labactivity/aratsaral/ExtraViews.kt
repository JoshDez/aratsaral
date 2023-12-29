package com.labactivity.aratsaral

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import com.labactivity.aratsaral.databinding.ItemLayoutBinding

class ExtraViews {

    fun createLinearLayout(task:Task, isTaskList:Boolean, binding: ItemLayoutBinding):LinearLayout{
        val context = binding.expandLayout.context
        //Creating Horizontal Linear Layout for the row
        val linearLayout = LinearLayout(context)
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        if (isTaskList){
            //FOR TASK LIST
            linearLayout.orientation = LinearLayout.VERTICAL

            //Description TextView

            val descriptionTextView = generateTextView(linearLayout.context, "\nDescription:\t\t${task.desc}")
            descriptionTextView.setOnClickListener {
                val intent = Intent(context, TaskView::class.java)
                intent.putExtra("toAddTask", false)
                intent.putExtra("id", task.id)
                intent.putExtra("name", task.name)
                intent.putExtra("desc", task.desc)
                intent.putExtra("subject", task.subject)
                intent.putExtra("deadline", task.deadline)
                intent.putExtra("isChecked", task.isChecked)
                context.startActivity(intent)
            }
            linearLayout.addView(descriptionTextView)

            //Deadline TextView
            if (task.deadline != "null"){
                val deadlineTV = generateDateTextView(linearLayout.context, task.deadline, task.isChecked, binding)
                deadlineTV.text = "\nDeadline:\t\t${deadlineTV.text}\n"
                linearLayout.addView(deadlineTV)
            }

        }else{
            //FOR SUBJECT LIST

            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.gravity = Gravity.CENTER
            linearLayout.setPadding(30,20,30,40)

            //CheckBox
            val checkBox = generateCheckBox(linearLayout.context, task)
            checkBox.setOnClickListener {
                DBListHandler(context).updateTaskState(task.id, checkBox.isChecked)
                updateFinishedTasks(task.subject, binding)
            }

            // Creating LayoutParams to set height and width for name and deadline text views
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width
                LinearLayout.LayoutParams.WRAP_CONTENT  // Height
            )

            //Name TextView
            val nameTextView = generateTextView(linearLayout.context, task.name)
            nameTextView.layoutParams = layoutParams

            //cuts the string if its too long
            if (nameTextView.text.length > 25){
                nameTextView.text = "${nameTextView.text.substring(0, 22)}..."
            }

            //Deadline TextView
            if (task.deadline != "null"){
                val deadlineTV = generateDateTextView(linearLayout.context, task.deadline, task.isChecked, binding)
                layoutParams.weight = 5f
                deadlineTV.gravity = Gravity.END
                deadlineTV.layoutParams = layoutParams
                linearLayout.addView(checkBox)
                linearLayout.addView(nameTextView)
                linearLayout.addView(deadlineTV)
            } else {
                linearLayout.gravity = Gravity.NO_GRAVITY
                linearLayout.addView(checkBox)
                linearLayout.addView(nameTextView)
            }

        }

        return linearLayout
    }

    private fun generateCheckBox(context: Context, task: Task):CheckBox{
        val checkBox = CheckBox(context)
        checkBox.isChecked = task.isChecked
        CompoundButtonCompat.setButtonTintList(checkBox, ContextCompat.getColorStateList(context, R.color.yellow))
        return checkBox
    }
    private fun generateTextView(context: Context, text:String): TextView{
        val textView = TextView(context)
        val lightPurple = Color.parseColor("#EEE4F8")
        textView.setTextColor(lightPurple)
        textView.text = "\t\t\t$text"
        textView.textSize = 16f
        return textView
    }
    private fun generateDateTextView(context: Context, deadline:String, isTaskChecked:Boolean, binding: ItemLayoutBinding): TextView{
        val redColor = Color.parseColor("#FF4C4C")
        val lightPurple = Color.parseColor("#EEE4F8")
        val deadlineTextView = TextView(context)
        deadlineTextView.textSize = 16f

        if (CalendarHandler().compareDateToPresent(deadline) > 0){
            deadlineTextView.text = "\t\t\tAlready Due"
            deadlineTextView.setTextColor(redColor)
            toggleUrgentTV(binding, isTaskChecked)
        } else if (CalendarHandler().compareDateToPresent(deadline) == 0){
            deadlineTextView.text = "\t\t\tToday"
            deadlineTextView.setTextColor(redColor)
            toggleUrgentTV(binding, isTaskChecked)

        } else if (CalendarHandler().compareDateToPresent(deadline) == -1) {
            deadlineTextView.text = "\t\t\tTomorrow"
            deadlineTextView.setTextColor(redColor)
            toggleUrgentTV(binding, isTaskChecked)
        } else if (CalendarHandler().compareDateToPresent(deadline) == -2) {
            deadlineTextView.text = "\t\t\t$deadline"
            deadlineTextView.setTextColor(redColor)
            toggleUrgentTV(binding, isTaskChecked)
        } else {
            deadlineTextView.text = "\t\t\t$deadline"
            deadlineTextView.setTextColor(lightPurple)
        }
        return deadlineTextView
    }

    //If the deadline is near the urgent message will pop out
    private fun toggleUrgentTV(binding: ItemLayoutBinding, isTaskChecked: Boolean){
        if (!isTaskChecked){
            binding.priorityTV.visibility = View.VISIBLE
        }
    }

    //Method for updating number of finished task over total tasks
    private fun updateFinishedTasks(subject: String, binding: ItemLayoutBinding){
        val context = binding.expandLayout.context
        val tasks = DBListHandler(context).getSubjTasks(subject)
        var finished = 0
        var total = 0
        total += tasks.size
        for (task in tasks){
            if (task.isChecked){
                finished += 1
            }
        }
        binding.totalTV.text = "$finished/$total"
    }
}