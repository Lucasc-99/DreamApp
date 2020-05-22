package com.example.interviewerapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.regions.Regions
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.plugin.Plugin
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.result.StorageUploadFileResult
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import kotlinx.android.synthetic.main.activity_first_question.*
import java.io.IOException


class InterviewActivity : AppCompatActivity() {

    //Paths
    private final var externalStorage = System.getenv("EXTERNAL_STORAGE")
    //private final var internalStorage  = this.getFilesDir().getAbsolutePath()

    //private val TAG = "InterviewActivity"

    private var fileIterator = 1
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var currentRecordingTime: Long = 0
    private var questionIterator: Int = 0
    val dogs = listOf<Int>(R.drawable.pic1,R.drawable.pic4,R.drawable.pic8)
    val icecream = listOf<Int>(R.drawable.pic10,R.drawable.pic3,R.drawable.pic6,R.drawable.pic7)
    val babies = listOf<Int>(R.drawable.pic2,R.drawable.pic5,R.drawable.pic9)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_question)
        recordButton.text = "Record"//Translate
        questionTextView.text = "Tell me about a recent dream you had"//Translate
        beginImagesButton.visibility = View.INVISIBLE
        recordButton.setBackgroundColor(Color.LTGRAY)


        val credentialsProvider =
            CognitoCachingCredentialsProvider(
                applicationContext,
                "us-east-2:8fad9d42-8842-458b-9a40-4257eeda075c",  // Identity pool ID
                Regions.US_EAST_2 // Region
            )



        //Connect to AWS user auth service
        AWSMobileClient.getInstance().initialize(
            applicationContext,
            object : Callback<UserStateDetails> {
                override fun onResult(userStateDetails: UserStateDetails) {
                    try {
                        Amplify.addPlugin<Plugin<*>>(
                            AWSS3StoragePlugin()
                        )
                        Amplify.configure(applicationContext)
                        Log.i("amplifyapponly", "All set and ready to go!")
                    } catch (exception: java.lang.Exception) {
                        Log.e("amplifyapponly", exception.message, exception)
                    }

                    when (userStateDetails.userState) {
                        UserState.SIGNED_IN -> runOnUiThread {

                            Log.i("amplifyapponly", "user is signed in")
                        }
                        UserState.SIGNED_OUT -> runOnUiThread {

                            Log.i("amplifyapponly", "user is signed out")
                        }
                        else -> AWSMobileClient.getInstance().signOut()
                    }
                }

                override fun onError(e: Exception) {
                    Log.e("amplifyapponly", e.toString())
                }
            })

        AWSMobileClient.getInstance().addUserStateListener { userStateDetails ->
            when (userStateDetails.userState) {
                UserState.GUEST -> Log.i("amplifyapponly", "user is in guest mode")
                UserState.SIGNED_OUT -> Log.i(
                    "amplifyapponly",
                    "user is signed out"
                )
                UserState.SIGNED_IN -> Log.i("amplifyapponly", "user is signed in")
                UserState.SIGNED_OUT_USER_POOLS_TOKENS_INVALID -> Log.i(
                    "amplifyapponly",
                    "need to login again"
                )
                UserState.SIGNED_OUT_FEDERATED_TOKENS_INVALID -> Log.i(
                    "amplifyapponly",
                    "user logged in via federation, but currently needs new tokens"
                )
                else -> Log.e("amplifyapponly", "unsupported")
            }
        }
    }

    public fun recordButtonClicked(v : View?) {

        /* Delay user from pressing button again */

        val  TIME: Long =  700
        recordButton.isEnabled = false
        val r = Runnable { recordButton.isEnabled = true }
        Handler().postDelayed(r, TIME)

        //PERMISSIONS
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        }

        else if (state == false){

            startRecording()
        }
        else if(state) {
            stopRecording()
        }
    }

    public fun nextButtonClicked(v : View?){
        stopRecording()
        nextQuestion()
    }


    public fun beginImagesButtonClicked(v : View?) {
        nextButton.visibility = View.INVISIBLE
        beginImagesButton.visibility = View.INVISIBLE
        questionTextView.visibility = View.INVISIBLE
        nextQuestion()
    }

    private fun startRecording() {
        try {
            recordButton.setBackgroundColor(Color.RED);
            recordButton.text = "Recording"//Translate
            //Set file output
            output = externalStorage + "/"+ intent.getStringExtra("Username") + returnCurrentQuestion() + fileIterator.toString() +".mp3"
            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder?.setOutputFile(output)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            chronometer.base = SystemClock.elapsedRealtime()-currentRecordingTime
            chronometer.start()

            state = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (state) {
            recordButton.setBackgroundColor(Color.LTGRAY)
            recordButton.text = "Record"//Translate
            mediaRecorder?.stop()
            mediaRecorder?.release()
            chronometer.stop()
            currentRecordingTime = SystemClock.elapsedRealtime() - chronometer.base
            if(currentRecordingTime <= 30000){
                Toast.makeText(this, "Please tell me more!", Toast.LENGTH_SHORT).show()
            }
            else{
                nextQuestion()
            }
            state = false
            //Set file output possibly redundant?
            output = externalStorage + "/"+intent.getStringExtra("Username")+ returnCurrentQuestion() + fileIterator.toString() +".mp3"

            //
            //Storage File Upload
            Amplify.Storage.uploadFile((intent.getStringExtra("Username")+ returnCurrentQuestion() + fileIterator.toString() +".mp3"),
                output.toString(),
                { result: StorageUploadFileResult ->
                    Log.i(
                        "amplifyapponly",
                        "Successfully uploaded: " + result.getKey()
                    )
                }
            ) { storageFailure: StorageException? ->
                Log.e(
                    "amplifyapponly",
                    "Upload error.",
                    storageFailure
                )
            }
            fileIterator++
        }
    }

    //Returns string with current question
    private fun returnCurrentQuestion(): String{
        when (questionIterator) {
            0 -> return "dream"
            1 -> return "day"
            3 -> return "dogs"
            4 -> return "icecream"
            5 -> return "babies"
            else -> return ""
        }
    }

    private fun returnRandomImage(t :String): Int{
        when(t){
            "dogs"-> return dogs[(0..2).random()]
            "icecream"-> return icecream[(0..3).random()]
            "babies"-> return babies[(0..2).random()]
            else-> return 0 //should never run this case
        }
    }

    private fun nextQuestion(){
        questionIterator++
        currentRecordingTime = 0
        chronometer.base = SystemClock.elapsedRealtime()
        fileIterator = 1

        when(questionIterator){
            1-> questionTextView.text = "Tell me about your day yesterday"//Translate
            2-> {
                questionTextView.text = "I will now show you a series of images for 15 seconds, you will have to tell a story about each one"//Translate
                nextButton.visibility = View.INVISIBLE
                recordButton.visibility = View.INVISIBLE
                chronometer.visibility = View.INVISIBLE
                beginImagesButton.visibility = View.VISIBLE
            }
            3->{
                //Image 1
                imageDisplay.setImageResource(returnRandomImage("dogs"))//randomize
                imageDisplay.visibility = View.VISIBLE
                Handler().postDelayed(
                    {
                        imageDisplay.visibility = View.INVISIBLE
                        nextButton.visibility = View.VISIBLE
                        recordButton.visibility = View.VISIBLE
                        chronometer.visibility = View.VISIBLE
                        questionTextView.text = "Now tell me a story about the puppy"//Translate
                        questionTextView.visibility = View.VISIBLE
                    },
                    10000
                )

            }
            4->{
                //Image 2
                questionTextView.visibility = View.INVISIBLE
                recordButton.visibility = View.INVISIBLE
                chronometer.visibility = View.INVISIBLE
                nextButton.visibility = View.INVISIBLE

                imageDisplay.setImageResource(returnRandomImage("icecream"))//randomize
                imageDisplay.visibility = View.VISIBLE
                Handler().postDelayed(
                    {
                        nextButton.visibility = View.VISIBLE
                        imageDisplay.visibility = View.INVISIBLE
                        recordButton.visibility = View.VISIBLE
                        chronometer.visibility = View.VISIBLE
                        questionTextView.text = "Now tell me a story about the ice cream"//Translate
                        questionTextView.visibility = View.VISIBLE
                    },
                    10000
                )
            }
            5->{
                //Image 3
                questionTextView.visibility = View.INVISIBLE
                recordButton.visibility = View.INVISIBLE
                chronometer.visibility = View.INVISIBLE
                nextButton.visibility = View.INVISIBLE

                imageDisplay.setImageResource(returnRandomImage("babies"))//randomize
                imageDisplay.visibility = View.VISIBLE
                Handler().postDelayed(
                    {
                        nextButton.visibility = View.VISIBLE
                        imageDisplay.visibility = View.INVISIBLE
                        recordButton.visibility = View.VISIBLE
                        chronometer.visibility = View.VISIBLE
                        questionTextView.text = "Now tell me a story about the baby"//Translate
                        questionTextView.visibility = View.VISIBLE
                    },
                    10000
                )
            }
            6->{
                //Final screen
                recordButton.visibility = View.INVISIBLE
                chronometer.visibility = View.INVISIBLE
                nextButton.visibility = View.INVISIBLE
                questionTextView.text = "Thank you for participating!"//Translate
            }
        }
    }


}