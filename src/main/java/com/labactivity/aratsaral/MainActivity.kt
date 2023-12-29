package com.labactivity.aratsaral

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.labactivity.aratsaral.databinding.ActivityMainBinding
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var database:DBListHandler
    private lateinit var toggle:ActionBarDrawerToggle
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //gets firebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance()

        //changes action bar title
        supportActionBar?.title = "Subjects"

        //open or create database
        database = DBListHandler(this)

        recyclerView = binding.subRV
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ItemAdapter(isTaskList = false, toDelete = false, database)

        binding.cancelBtn.setOnClickListener {
            cancelDeleteSubj()
        }
        binding.deleteBtn.setOnClickListener {
            deleteSubj()
        }

        binding.addFAB.setOnClickListener {
            addSubj()
        }


        binding.navView
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val header = binding.navView.getHeaderView(0)
        val userTV = header.findViewById<TextView>(R.id.usernameTV)
        val emailTV = header.findViewById<TextView>(R.id.emailTV)
        if (firebaseAuth.currentUser == null){
            userTV.text = ""
            emailTV.text = "Sign in to sync your list to cloud"
        } else {
            userTV.text = firebaseAuth.currentUser?.displayName.toString()
            emailTV.text = firebaseAuth.currentUser?.email.toString()
        }
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.deleteSubjects -> {
                    recyclerView.adapter = ItemAdapter(isTaskList = false, toDelete = true, database)
                    binding.deleteActionBar.visibility = View.VISIBLE
                    binding.addFAB.visibility = View.GONE
                }
                R.id.addNewSubject -> {
                    addSubj()
                }
                R.id.syncDataToFirebase -> {
                    if (firebaseAuth != null){
                        Toast.makeText(this, "Data Syncing..", Toast.LENGTH_SHORT).show()
                        FirebaseDBHandler(this).syncTasks(true)
                    }
                }
                R.id.importDataFromFirebase -> {
                    if (firebaseAuth != null){
                        Toast.makeText(this, "Data Importing..", Toast.LENGTH_SHORT).show()
                        FirebaseDBHandler(this).importTasks(displayMessage = true)
                        Handler().postDelayed({
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }, 5000)
                    }
                }
                R.id.loginOrLogout -> {
                    //for logging out
                    if (firebaseAuth.currentUser != null){
                        //save the data first before signing out
                        //and then clear the data from sqlite
                        val alertDialog = AlertDialog.Builder(this)
                            .setTitle("Logout")
                            .setMessage("Do you want to logout? (This would delete all of your data from your device)")
                            .setPositiveButton("Yes"){ _, _ ->
                                FirebaseDBHandler(this).syncTasks(false)
                                database.deleteAllData()
                                firebaseAuth.signOut()
                                proceedToLogIn()
                            }
                            .setNegativeButton("No"){ dialog, _ ->
                                dialog.dismiss()
                            }
                        alertDialog.show()
                    //for logging in
                    } else {
                        //Ask if the user wants to clear data in the local database
                        if (database.getSubjects().isNotEmpty()){
                            val alertDialog = AlertDialog.Builder(this)
                                .setTitle("Clear Data")
                                .setMessage("Do you want to clear all of your data before logging in?")
                                .setPositiveButton("Yes"){ _, _ ->
                                    database.deleteAllData()
                                    Toast.makeText(this, "All data has been cleared", Toast.LENGTH_SHORT).show()
                                }
                                .setNegativeButton("No"){ dialog, _ ->
                                    dialog.dismiss()
                                }
                                .setOnDismissListener{
                                    proceedToLogIn()
                                }
                            alertDialog.show()
                        } else {
                            proceedToLogIn()
                        }
                    }
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer
            true
        }

    }

    private fun addSubj() {
        val dialogPlus = DialogPlus.newDialog(this)
            .setContentHolder(ViewHolder(R.layout.add_subject))
            .setGravity(Gravity.CENTER)
            .create()

        val view = dialogPlus.holderView
        val subjName:EditText = view.findViewById(R.id.subjNameEdtTxt)
        val addBtn: Button = view.findViewById(R.id.addSubjNameBtn)

        dialogPlus.show()

        addBtn.setOnClickListener{
            if (subjName.text.isEmpty()){
                Toast.makeText(this, "Enter Subject Name", Toast.LENGTH_SHORT).show()
            } else {
                //dismisses dialog if the insert is successful
                if (database.insertToSubjectTable(subjName.text.toString(), true)){
                    dialogPlus.dismiss()
                } else {
                    Toast.makeText(this, "${subjName.text} already exists", Toast.LENGTH_SHORT).show()
                }
                recyclerView.adapter = ItemAdapter(isTaskList = false, toDelete = false, database)
            }
        }
    }

    private fun cancelDeleteSubj() {
        database.clearItems()
        recyclerView.adapter = ItemAdapter(isTaskList = false, toDelete = false, database)
        binding.deleteActionBar.visibility = View.GONE
        binding.addFAB.visibility = View.VISIBLE
    }

    private fun deleteSubj() {
        if (database.isItemListNotEmpty()){
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Do you want to delete these subjects?")
                .setNegativeButton("No"){ dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("Yes"){ _, _ ->
                    database.deleteItems(isTaskList = false)
                    recyclerView.adapter = ItemAdapter(isTaskList = false, toDelete = false, database)
                    binding.deleteActionBar.visibility = View.GONE
                    binding.addFAB.visibility = View.VISIBLE
                }
            alertDialog.show()
        }
    }

    private fun proceedToLogIn(){
        val intent = Intent(this, LogIn::class.java)
        intent.putExtra("login", true)
        startActivity(intent)
        this.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return onOptionsItemSelected(item)
    }
}