package com.labactivity.aratsaral

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FirebaseDBHandler(val context:Context) {
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val tblReference: DatabaseReference = database.getReference("tasks")
    private var sqliteDB:DBListHandler = DBListHandler(context)


    // Serves as a wrapper for syncAllTaskToCloud
    fun syncTasks(displayMessage:Boolean) {
        syncAllTasksToCloud { isSuccess ->
            if (isSuccess) {
                // All tasks were successfully inserted
                if (displayMessage){
                    Toast.makeText(context, "Data Synced Successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Task insertion failed
                if (displayMessage){
                    Toast.makeText(context, "Data Syncing Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // Serves as a wrapper for importAllTasksFromCloud
    fun importTasks(displayMessage:Boolean) {
        importAllTasksFromCloud { isSuccess ->
            if (isSuccess) {
                // All tasks were successfully inserted
                if (displayMessage){
                    Toast.makeText(context, "Data Synced Successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Task insertion failed
                if (displayMessage){
                    Toast.makeText(context, "Data Syncing Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun syncAllTasksToCloud(completion: (Boolean) -> Unit) {

        if (firebaseAuth.currentUser != null) {
            val userKey = "${firebaseAuth.currentUser!!.displayName}:${firebaseAuth.currentUser!!.uid}"
            val taskList = sqliteDB.getAllTasks()
            var tasksCompleted = 0
            val allTasks = taskList.size-1

            tblReference.child(userKey).removeValue()

            for (task in taskList) {
                val taskKey = "${task.subject}:${task.name}"
                val taskHashmap = HashMap<String, Any>()
                taskHashmap["task_name"] = task.name
                taskHashmap["task_description"] = task.desc
                taskHashmap["task_deadline"] = task.deadline
                taskHashmap["task_subject"] = task.subject
                taskHashmap["task_checked"] = task.isChecked

                tblReference.child(userKey).child(taskKey).setValue(taskHashmap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            tasksCompleted++
                            if (tasksCompleted == allTasks) {
                                // All tasks have been successfully completed
                                completion(true)
                            }
                        } else {
                            // If any task fails, immediately notify about failure
                            completion(false)
                        }
                    }
            }
        } else {
            completion(false) // User is not authenticated
        }
    }


    private fun importAllTasksFromCloud(completion: (Boolean) -> Unit){
        if (firebaseAuth.currentUser != null){
            val userKey = "${firebaseAuth.currentUser!!.displayName}:${firebaseAuth.currentUser!!.uid}"
            val userDataReference = tblReference.child(userKey)

            userDataReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //User
                    if (snapshot.exists()){
                        //Task
                        val totalTask = snapshot.children.count() - 1
                        var completedTask = 0
                        for (childSnapshot in snapshot.children){
                            val taskName = childSnapshot.child("task_name").getValue(String::class.java) ?: ""
                            val taskDesc = childSnapshot.child("task_description").getValue(String::class.java) ?: ""
                            val taskDeadline = childSnapshot.child("task_deadline").getValue(String::class.java) ?: ""
                            val taskSubj = childSnapshot.child("task_subject").getValue(String::class.java) ?: ""
                            val taskChecked = childSnapshot.child("task_checked").getValue(Boolean::class.java) ?: false

                            val task = Task(name = taskName, desc = taskDesc, deadline = taskDeadline,
                                subject = taskSubj, isChecked = taskChecked)
                            sqliteDB.insertToSubjectTable(taskSubj, false)
                            sqliteDB.insertToTaskTable(task, false)

                            //returns true if the tasks are all imported
                            if (completedTask == totalTask && childSnapshot.exists()){
                                completion(true)
                                //returns false if the tasks didn't exist
                            } else if (!childSnapshot.exists()) {
                                completion(false)
                            }

                            completedTask += 1
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FirebaseData", "Error: ${databaseError.message}")
                }

            })
        } else {
            completion(false)
        }
    }

}