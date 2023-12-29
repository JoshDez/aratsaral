package com.labactivity.aratsaral

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.labactivity.aratsaral.databinding.ActivityTaskViewBinding

class TaskView : AppCompatActivity() {
    private lateinit var binding:ActivityTaskViewBinding
    private lateinit var calTaskView:CalendarHandler
    private lateinit var task: Task
    private var toAddTask = true

    //task global variables
    private var isChecked = false
    private var id:Long = 0
    private var name = ""
    private var desc = ""
    private var subject = ""
    private var deadline =  ""
    private var tempDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calTaskView = CalendarHandler(binding)
        toAddTask = intent.getBooleanExtra("toAddTask",true)
        isChecked = intent.getBooleanExtra("isChecked",false)
        id = intent.getLongExtra("id",0)
        name = intent.getStringExtra("name").toString()
        desc = intent.getStringExtra("desc").toString()
        subject = intent.getStringExtra("subject").toString()
        deadline = intent.getStringExtra("deadline").toString()
        binding.taskSubjTV.text = subject
        calTaskView.bindDeadline(deadline)
        tempDate = calTaskView.getCurrentDate()

        //For Adding New Task
        if(toAddTask){
            //renaming ui names related to adding task
            supportActionBar?.title =  "Add Task"
            binding.positiveBtn.text = "Add"
            binding.negativeBtn.text = "Cancel"

            //Add Button
            binding.positiveBtn.setOnClickListener {
                addTask(saveInputs())
            }
            //Cancel Button
            binding.negativeBtn.setOnClickListener {
                this.finish()
            }
        }
        //For Viewing/Editing Task
        else {

            supportActionBar?.title =  "Task"
            //bind task
            binding.taskNameEdtTxt.setText(name)
            binding.taskDescEdtTxt.setText(desc)

            //Confirm Button
            binding.positiveBtn.setOnClickListener {
                updateTask(saveInputs())
            }
            //Delete Button
            binding.negativeBtn.setOnClickListener {
                deleteTask()
            }
        }

        //Checkbox that allows to set date for deadline
        binding.deadlineCB.setOnClickListener {
            if(binding.deadlineCB.isChecked){
                deadline = tempDate
                binding.datePickerLinear.isClickable = true
                binding.deadlineLayout.setBackgroundColor(Color.parseColor("#D0ADF1"))
                calTaskView.bindDeadline(deadline)
            }else{
                tempDate = deadline
                deadline = "null"
                binding.datePickerLinear.isClickable = false
                binding.deadlineLayout.setBackgroundColor(Color.parseColor("#847493"))
                calTaskView.bindDeadline(deadline)
            }
        }

        //Allows you to pick dates for deadline
        binding.datePickerLinear.setOnClickListener {
            calTaskView.showDatePicker(deadline)
        }
        setDeadlineEditable()

    }
    private fun setDeadlineEditable(){
        if (deadline == "null"){
            binding.deadlineLayout.setBackgroundColor(Color.parseColor("#847493"))
            binding.deadlineCB.isChecked = false
            binding.datePickerLinear.isClickable = false
        } else {
            binding.deadlineCB.isChecked = true
            binding.datePickerLinear.isClickable = true
        }
    }

    //Validates and Saves Inputs while returning a boolean value that allows add or update functions to the db
    private fun saveInputs():Boolean{
        if (binding.taskNameEdtTxt.text.toString().isEmpty()){
            Toast.makeText(this, "Enter name of task", Toast.LENGTH_SHORT).show()
            return false
        } else {
            name = binding.taskNameEdtTxt.text.toString()
            desc = binding.taskDescEdtTxt.text.toString()
            deadline = calTaskView.getDeadline()
            task = Task(
                id = id,
                subject = subject,
                name = name,
                desc = desc,
                deadline = deadline,
                isChecked = false
            )
            return true
        }
    }

    private fun addTask(toAdd:Boolean) {
        if (toAdd){
            //returns to previous activity if the insert is successful
            if (DBListHandler(this).insertToTaskTable(task, true)){
                goToPreviousActivity(subject)
            } else {
                Toast.makeText(this, "This task already exists", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateTask(toUpdate:Boolean) {
        if (toUpdate){
            DBListHandler(this).updateTask(task)
            goToPreviousActivity(subject)
        }
    }
    private fun deleteTask() {

        val dialogBox = AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Do you want to delete this task?")
            .setNegativeButton("No"){ dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton("Yes"){dialog, which ->
                DBListHandler(this).deleteTask(id)
                goToPreviousActivity(subject)
            }
        dialogBox.show()

    }


    private fun goToPreviousActivity(subject:String){
        val intent = Intent(this, TaskList::class.java)
        intent.putExtra("subject", subject)
        startActivity(intent)
        this.finish()
    }
}