package com.labactivity.aratsaral

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.widget.Toast

class DBListHandler(val context: Context) {
    private var db:SQLiteDatabase = context.openOrCreateDatabase("dblist", Context.MODE_PRIVATE, null)
    private var itemArrayList:ArrayList<String> = ArrayList()


    //CREATE FUNCTIONS
    private fun createTableIfNotExists(tableName:String){
        when(tableName){
            "tblsubject" -> db.execSQL("CREATE TABLE IF NOT EXISTS tblsubject(subjID INTEGER PRIMARY KEY AUTOINCREMENT, subjName VARCHAR)")
            "tbltask"    -> db.execSQL("CREATE TABLE IF NOT EXISTS tbltask(taskID INTEGER PRIMARY KEY AUTOINCREMENT, taskName VARCHAR, taskDesc VARCHAR, taskDL VARCHAR," +
                    "taskSubj VARCHAR, isCheck INTEGER)")
        }
    }

    fun insertToSubjectTable(subjName:String, displayMessage:Boolean):Boolean{
        //create subject table if it does not exist
        createTableIfNotExists("tblsubject")

        //Validation for duplicated subject names
        val subjTbl = getReferenceTable("tblsubject", "subjName")
        val name = subjTbl.getColumnIndex("subjName")

        if (subjTbl.moveToFirst()){
            do {
                if (subjName.uppercase() == subjTbl.getString(name).uppercase()){
                    //return false if there is a duplicated subject
                    return false
                }
            }while (subjTbl.moveToNext())
        }

        //Inserting to tblsubject
        val mysql = "INSERT INTO tblsubject(subjName)values(?)"
        val statement:SQLiteStatement = db.compileStatement(mysql)
        statement.bindString(1, subjName)
        statement.execute()

        //notify user
        if (displayMessage){
            Toast.makeText(context, "Subject Successfully Added", Toast.LENGTH_LONG).show()
        }

        //return true if the insert is successful
        return true
    }

    fun insertToTaskTable(task:Task, displayMessage:Boolean):Boolean{
        //create task table if it does not exist
        createTableIfNotExists("tbltask")

        //Validation for duplicated tasks
        val taskTbl = getReferenceTable("tbltask", "taskDL")


        //getting column indexes
        val nameInd = taskTbl.getColumnIndex("taskName")
        val subInd = taskTbl.getColumnIndex("taskSubj")

        if (taskTbl.moveToFirst()){
            do {
                if (task.name.uppercase() == taskTbl.getString(nameInd).uppercase() &&
                    task.subject.uppercase() == taskTbl.getString(subInd).uppercase()){
                    //return false if the insert is unsuccessful
                    return false
                }
            }while (taskTbl.moveToNext())
        }


        //Inserting to tblsubject
        val mysql = "INSERT INTO tbltask(taskName, taskDesc, taskDL, taskSubj, isCheck)values(?,?,?,?,?)"
        val statement:SQLiteStatement = db.compileStatement(mysql)
        statement.bindString(1, task.name)
        statement.bindString(2, task.desc)
        statement.bindString(3, task.deadline)
        statement.bindString(4, task.subject)
        statement.bindLong(5, if (task.isChecked) 1 else 0)
        statement.execute()

        //notify user
        if (displayMessage){
            Toast.makeText(context, "Task Successfully Added", Toast.LENGTH_LONG).show()
        }
        syncToCloud()

        //return true if the insert is successful
        return true
    }




    //READ FUNCTIONS

    private fun getReferenceTable(tblName: String, sort:String): Cursor {
        return db.rawQuery("SELECT * FROM $tblName ORDER BY $sort ASC", null)
    }

    fun getSubjects():ArrayList<String>{
        //create subject table if it does not exist
        createTableIfNotExists("tblsubject")

        val subjList:ArrayList<String> = ArrayList()
        val subjTbl = getReferenceTable("tblsubject", "subjName")
        val nameInd = subjTbl.getColumnIndex("subjName")

        if (subjTbl.moveToFirst()){
            do {
                val subject = subjTbl.getString(nameInd)
                subjList.add(subject)
            }while (subjTbl.moveToNext())
        }

        return subjList
    }

    fun getAllTasks():ArrayList<Task>{
        //create task table if it does not exist
        createTableIfNotExists("tbltask")

        val taskList:ArrayList<Task> = ArrayList()
        val taskTbl = getReferenceTable("tbltask", "taskSubj")

        //getting column indexes
        val nameInd = taskTbl.getColumnIndex("taskName")
        val descInd = taskTbl.getColumnIndex("taskDesc")
        val subjInd = taskTbl.getColumnIndex("taskSubj")
        val checkInd = taskTbl.getColumnIndex("isCheck")
        val idInd = taskTbl.getColumnIndex("taskID")
        val deadInd = taskTbl.getColumnIndex("taskDL")

        //appending task to taskList
        if (taskTbl.moveToFirst()){
            do {
                val task = Task(id = taskTbl.getInt(idInd).toLong(),name = taskTbl.getString(nameInd),
                    desc = taskTbl.getString(descInd), deadline = taskTbl.getString(deadInd),
                    subject = taskTbl.getString(subjInd), isChecked = taskTbl.getInt(checkInd)==1)
                taskList.add(task)

            }while (taskTbl.moveToNext())
        }

        return taskList
    }

    fun getSubjTasks(subject:String):ArrayList<Task>{
        //create task table if it does not exist
        createTableIfNotExists("tbltask")

        val taskList:ArrayList<Task> = ArrayList()
        val taskTbl = getReferenceTable("tbltask", "taskDL")

        //getting column indexes
        val nameInd = taskTbl.getColumnIndex("taskName")
        val descInd = taskTbl.getColumnIndex("taskDesc")
        val subjInd = taskTbl.getColumnIndex("taskSubj")
        val checkInd = taskTbl.getColumnIndex("isCheck")
        val idInd = taskTbl.getColumnIndex("taskID")
        val deadInd = taskTbl.getColumnIndex("taskDL")

        //appending task to taskList
        if (taskTbl.moveToFirst()){
            do {
                if(subject.uppercase() == taskTbl.getString(subjInd).uppercase()){
                    val task = Task(id = taskTbl.getInt(idInd).toLong(),name = taskTbl.getString(nameInd),
                        desc = taskTbl.getString(descInd), deadline = taskTbl.getString(deadInd),
                        subject = taskTbl.getString(subjInd), isChecked = taskTbl.getInt(checkInd)==1)
                    taskList.add(task)
                }
            }while (taskTbl.moveToNext())
        }

        return taskList
    }


    //UPDATE FUNCTIONS

    fun updateSubjName(subjName:String, newSubjName:String): Boolean{
        //Validation for duplicated subject names
        val subjTbl = getReferenceTable("tblsubject", "subjName")
        val name = subjTbl.getColumnIndex("subjName")

        if (subjTbl.moveToFirst()){
            do {
                if (newSubjName.uppercase() == subjTbl.getString(name).uppercase()){
                    //return false if the insert is unsuccessful
                    return false
                }
            }while (subjTbl.moveToNext())
        }

        //Begins updating the subject name
        val mysql1 = "update tbltask set taskSubj = ? where taskSubj = ?"
        val mysql2 = "update tblsubject set subjName = ? where subjName = ?"

        val statement1:SQLiteStatement = db.compileStatement(mysql1)
        val statement2:SQLiteStatement = db.compileStatement(mysql2)

        statement1.bindString(1, newSubjName)
        statement1.bindString(2, subjName)
        statement2.bindString(1, newSubjName)
        statement2.bindString(2, subjName)

        statement1.execute()
        statement2.execute()

        syncToCloud()

        return true
    }

    fun updateTaskState(id:Long, isChecked:Boolean){
        val mysql = "update tbltask set isCheck = ? where taskID = ?"
        val statement:SQLiteStatement = db.compileStatement(mysql)
        statement.bindLong(1, if (isChecked) 1 else 0)
        statement.bindLong(2,id)
        statement.execute()
        syncToCloud()
    }
    fun updateTask(task: Task){
        val mysql = "update tbltask set taskName = ?, taskDesc = ?, taskDL = ?, taskSubj = ?, isCheck = ? where taskID = ?"
        val statement:SQLiteStatement = db.compileStatement(mysql)
        statement.bindString(1, task.name)
        statement.bindString(2, task.desc)
        statement.bindString(3, task.deadline)
        statement.bindString(4, task.subject)
        statement.bindLong(5, if (task.isChecked) 1 else 0)
        statement.bindLong(6, task.id)
        statement.execute()
        syncToCloud()
        //Toast.makeText(context, "Task Updated Successfully", Toast.LENGTH_LONG).show()
    }

    //DELETE FUNCTIONS
    fun deleteAllData(){
        val mysql1 = "DELETE FROM tbltask"
        val mysql2 = "DELETE FROM tblsubject"
        val statement1:SQLiteStatement = db.compileStatement(mysql1)
        val statement2:SQLiteStatement = db.compileStatement(mysql2)
        statement1.execute()
        statement2.execute()
    }

    fun deleteTask(id: Long){
        val mysql = "DELETE FROM tbltask where taskID = ?"
        val statement:SQLiteStatement = db.compileStatement(mysql)
        statement.bindLong(1, id)
        statement.execute()
        syncToCloud()
    }

    fun deleteSubj(subj:String){
        //Delete from subject table
        val mysql1 = "DELETE FROM tblsubject where subjName = ?"
        val statement1:SQLiteStatement = db.compileStatement(mysql1)
        statement1.bindString(1, subj)
        statement1.execute()

        //Delete from task table
        val mysql2 = "DELETE FROM tbltask where taskSubj = ?"
        val statement2:SQLiteStatement = db.compileStatement(mysql2)
        statement2.bindString(1, subj)
        statement2.execute()
        syncToCloud()
    }

    fun addItemToList(item:String){
        itemArrayList.add(item)
    }
    fun removeItemToList(data: String){
        val newArrayList:ArrayList<String> = ArrayList()
        for (item in itemArrayList){
            if (data != item){
                newArrayList.add(item)
            }
        }
        itemArrayList = newArrayList
    }
    fun deleteItems(isTaskList:Boolean){
        if (isTaskList){
            for (task in itemArrayList){
                deleteTask(task.toLong())
            }
        } else {
            for (subj in itemArrayList){
                deleteSubj(subj)
            }
        }
        clearItems()
    }
    fun clearItems(){
        itemArrayList = ArrayList()
    }
    fun isItemListNotEmpty():Boolean{
        return itemArrayList.isNotEmpty()
    }
    private fun syncToCloud(){
        FirebaseDBHandler(context).syncTasks(false)
    }


}