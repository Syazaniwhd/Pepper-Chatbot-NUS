package com.example.combinedapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.recyclerview.widget.RecyclerView

class chat_history_activity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)

        val chatHistBack_btn = findViewById<Button>(R.id.chatHistBack_btn)
        val HistClear_btn = findViewById<Button>(R.id.HistClear_btn)
        val historyListView = findViewById<ListView>(R.id.historyListView)
        val receivedResponse = intent.getStringExtra("response")
        val receivedQuestion = intent.getStringExtra("question")

        //var list : MutableList<String?> = mutableListOf("Q: " + receivedQuestion, "A: " + receivedResponse)
        var list  = arrayOf("Q: " + receivedQuestion, "A: " + receivedResponse)

        val arrayAdapter : ArrayAdapter<String?> = ArrayAdapter(this,android.R.layout.simple_list_item_1,list)
        historyListView.adapter = arrayAdapter

        chatHistBack_btn.setOnClickListener{
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

    }
}