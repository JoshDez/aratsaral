package com.labactivity.aratsaral

data class Task(
    //task id
    val id:Long = 0,
    //task name
    val name:String,
    //task description
    val desc:String,
    //task deadline
    val deadline:String,
    //task's subject
    val subject:String,
    //task finished
    val isChecked:Boolean
)
