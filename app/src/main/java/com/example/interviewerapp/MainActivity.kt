package com.example.interviewerapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.interviewerapp.R.layout.activity_main
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        //GET USER DATA
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        editText.setText(sharedPreferences.getString("username", ""))
        editTextEmail.setText(sharedPreferences.getString("email",""))
        editTextLastName.setText(sharedPreferences.getString("lastname", ""))
        editTextLastName.clearFocus()
        editText.clearFocus()
        editTextEmail.clearFocus()


        //Button to begin interview
        entryButton.setOnClickListener {

            //SAVE USER DATA
            var editor = sharedPreferences.edit()
            editor.putString("username", editText.text.toString())
            editor.putString("email", editTextEmail.text.toString())
            editor.putString("lastname", editTextLastName.text.toString())
            editor.commit()

            if("@" in editTextEmail.text.toString()){
                val intent = Intent(this, InterviewActivity::class.java)
                var user = editText.text.toString()
                var email = editTextEmail.text.toString()

                intent.putExtra("Username", user)
                intent.putExtra("Email", email)
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "Please enter an Email", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
