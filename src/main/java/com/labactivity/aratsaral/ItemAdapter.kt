package com.labactivity.aratsaral

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.aratsaral.databinding.ItemLayoutBinding

class ItemAdapter(private val isTaskList:Boolean, private val toDelete:Boolean, private val db:DBListHandler,
                  private val subject:String = ""):RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (isTaskList){
            val taskList = db.getSubjTasks(subject)
            holder.bindTask(taskList[position], toDelete, db)
        } else {
            val subjList = db.getSubjects()
            holder.bindSubj(subjList[position], toDelete, db)
        }
    }

    override fun getItemCount(): Int {
        return if (isTaskList){
            val taskList = db.getSubjTasks(subject)
            taskList.size
        } else {
            val subjList = db.getSubjects()
            subjList.size
        }
    }

}