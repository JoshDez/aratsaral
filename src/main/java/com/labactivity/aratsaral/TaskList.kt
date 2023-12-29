package com.labactivity.aratsaral

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labactivity.aratsaral.databinding.ActivityTaskListBinding
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class TaskList : AppCompatActivity() {
    private lateinit var binding:ActivityTaskListBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DBListHandler
    private var subject = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = DBListHandler(this)

        subject += intent.getStringExtra("subject")
        supportActionBar?.title = "Tasks | $subject"

        recyclerView = binding.taskRV
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = ItemAdapter(isTaskList = true, toDelete = false, database, subject)

        binding.cancelBtn.setOnClickListener {
            cancelDeleteSubj()
        }
        binding.deleteBtn.setOnClickListener {
            deleteSubj()
        }

        binding.addFAB.setOnClickListener {
            addTask()

        }

    }

    private fun addTask() {
        val intent = Intent(this, TaskView::class.java)
        intent.putExtra("subject", subject)
        startActivity(intent)
    }

    private fun editSubjName(){
        //creates a dialog for editing subject name
        val dialogPlus = DialogPlus.newDialog(this)
            .setContentHolder(ViewHolder(R.layout.add_subject))
            .setGravity(Gravity.CENTER)
            .create()

        val view = dialogPlus.holderView
        val title: TextView = view.findViewById(R.id.titleTextView)
        val subjName: EditText = view.findViewById(R.id.subjNameEdtTxt)
        val addBtn: Button = view.findViewById(R.id.addSubjNameBtn)

        title.text = "Edit Subject Name"
        addBtn.text = "Edit"
        subjName.setText(subject)

        dialogPlus.show()


        addBtn.setOnClickListener{
            if (subjName.text.isEmpty()){
                Toast.makeText(this, "Enter Subject Name", Toast.LENGTH_SHORT).show()
            } else {
                //dismisses dialog if the insert is successful
                if (database.updateSubjName(subject, subjName.text.toString())){
                    //Refreshes the taskList
                    val intent = Intent(this, TaskList::class.java)
                    intent.putExtra("subject", subjName.text.toString())
                    startActivity(intent)
                    this.finish()

                } else {
                    Toast.makeText(this, "${subjName.text} already exists", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteSubj() {
        if (database.isItemListNotEmpty()){
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Do you want to delete these tasks?")
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("Yes"){ _, _ ->
                    database.deleteItems(isTaskList = true)
                    recyclerView.adapter = ItemAdapter(isTaskList = true, toDelete = false, database, subject)
                    binding.deleteActionBar.visibility = View.GONE
                    binding.addFAB.visibility = View.VISIBLE
                }
            alertDialog.show()
        }
    }

    private fun cancelDeleteSubj() {
        database.clearItems()
        recyclerView.adapter = ItemAdapter(isTaskList = true, toDelete = false, database, subject)
        binding.deleteActionBar.visibility = View.GONE
        binding.addFAB.visibility = View.VISIBLE
    }

    private fun backToPreviousActivity(){
        val intent = Intent(this, MainActivity::class.java)
        this.finish()
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backToPreviousActivity()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.subj_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.deleteSubject -> {
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Delete Subject")
                    .setMessage("Do you want to delete $subject subject?")
                    .setPositiveButton("Yes"){ _, _ ->
                        database.deleteSubj(subject)
                        backToPreviousActivity()
                    }
                    .setNegativeButton("No"){ dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                return true
            }
            R.id.editSubjName -> {
                editSubjName()
                return true
            }
            R.id.deleteTask -> {
                recyclerView.adapter = ItemAdapter(isTaskList = true, toDelete = true, database, subject)
                binding.deleteActionBar.visibility = View.VISIBLE
                binding.addFAB.visibility = View.GONE
                return true
            }
            R.id.addNewTask -> {
                addTask()
                return true
            } else -> super.onOptionsItemSelected(item)
        }
    }
}