package com.labactivity.aratsaral

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.aratsaral.databinding.ItemLayoutBinding

class ItemViewHolder(private val binding:ItemLayoutBinding):RecyclerView.ViewHolder(binding.root) {
    private var isExpanded = false
    private var expandList:ArrayList<Task> = ArrayList()

    //View Holder for Task List
    fun bindTask(task: Task, toDelete:Boolean, db:DBListHandler){
        val context = db.context
        expandList.add(task)
        binding.checkBox.visibility = View.VISIBLE
        binding.totalTV.visibility = View.GONE
        binding.checkBox.isChecked = task.isChecked
        binding.titleTV.text = truncateString(task.name, task.name.length)

        val layoutParams = binding.titleTV.layoutParams
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.titleTV.layoutParams = layoutParams


        if (toDelete){
            binding.deleteCheckBox.visibility = View.VISIBLE
            binding.checkBox.visibility = View.GONE
            binding.deleteCheckBox.setOnClickListener {
                if (binding.deleteCheckBox.isChecked){
                    db.addItemToList(task.id.toString())
                } else {
                    db.removeItemToList(task.id.toString())
                }
            }
        }

        //displays description and date
        expandItem(expandList, true, binding)

        binding.mainLayout.setOnClickListener {
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

        binding.expandBtn.setOnClickListener{
            if (isExpanded){
                binding.expandLayout.visibility = View.GONE
                isExpanded = false
            } else {
                binding.expandLayout.visibility = View.VISIBLE
                isExpanded = true
            }
        }

        binding.checkBox.setOnClickListener{
            DBListHandler(context).updateTaskState(task.id, binding.checkBox.isChecked)
        }

    }

    //View Holder for Subject List
    fun bindSubj(subject:String, toDelete:Boolean, db:DBListHandler){
        val context = db.context

        if (toDelete){
            binding.deleteCheckBox.visibility = View.VISIBLE
            binding.totalTV.visibility = View.GONE
            binding.deleteCheckBox.setOnClickListener {
                if (binding.deleteCheckBox.isChecked){
                    db.addItemToList(subject)
                } else {
                    db.removeItemToList(subject)
                }
            }
        }

        expandList = db.getSubjTasks(subject)
        binding.titleTV.text = truncateString(subject, subject.length)

        countFinishedTasks(expandList)
        expandItem(expandList, false, binding)

        binding.mainLayout.setOnClickListener{
            val intent = Intent(context, TaskList::class.java)
            intent.putExtra("subject", subject)
            context.startActivity(intent)
        }

        binding.expandBtn.setOnClickListener{
            if (isExpanded){
                binding.expandLayout.visibility = View.GONE
                isExpanded = false
            } else {
                binding.expandLayout.visibility = View.VISIBLE
                isExpanded = true
            }
        }
    }

    //this truncates excess characters
    private fun truncateString(string:String, stringLen:Int): String{
        return if (stringLen > 18){
            "${string.substring(0, 15)}..."
        }else {
            string
        }
    }

    private fun countFinishedTasks(tasks:ArrayList<Task>){
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
    private fun expandItem(expandList:ArrayList<Task>, isTaskList:Boolean, binding: ItemLayoutBinding){
        for (task in expandList){
            val newRow:LinearLayout = ExtraViews().createLinearLayout(task,
                isTaskList, binding)
            binding.expandLayout.addView(newRow)
        }
    }

}