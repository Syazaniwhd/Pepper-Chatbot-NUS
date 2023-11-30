package com.example.combinedapp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale


class MainActivity : AppCompatActivity(), OnInitListener {
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    lateinit var tts: TextToSpeech
    private val client = OkHttpClient()



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val txtResponse = findViewById<TextView>(R.id.responseTxt)
        val btnMic = findViewById<Button>(R.id.btnMic)
        val txtView = findViewById<TextView>(R.id.txtView)
        val reqTxt = findViewById<TextView>(R.id.reqTxt)
        val stopButton = findViewById<Button>(R.id.stopButton)
        //val readMore = findViewById<com.borjabravo.readmoretextview.ReadMoreTextView>(R.id.readMore)
        val muteBtn = findViewById<Button>(R.id.muteBtn)
        val readMoreTextView = findViewById<TextView>(R.id.readMoreTextView)
        val fullanswerText = findViewById<TextView>(R.id.fullanswertext)
        tts = TextToSpeech(this, this)


        fun clearText(){
            txtResponse.text = ""
            reqTxt.text = ""
            //readMore.text = ""
        }

        fun onStop() {
            if (tts.isSpeaking) {
                tts.stop() // Stop the TTS engine if it's speaking when the activity is destroyed.
            }
            //tts.shutdown() // Release TTS resources.
        }


        fun onUnmute() {
            if (tts.isSpeaking == false){
                tts.speak(txtResponse.text.toString(), TextToSpeech.QUEUE_ADD, null)
            }
        }

        stopButton.setOnClickListener {
            onStop()
            clearText()
        }

        muteBtn.setOnClickListener{
            if (tts.isSpeaking){
                onStop()
            }
            else{
               onUnmute()
            }
        }

        readMoreTextView.setOnClickListener{
            var dialog = FullAnswerFragment()
            dialog.show(supportFragmentManager, "customDialog")
        }


        btnMic.setOnClickListener(View.OnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please state your question")
            try {
                activityResultLauncher.launch(intent)
            } catch (exp: ActivityNotFoundException) {
                Toast.makeText(
                    applicationContext,
                    "Your device does not support",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
                if (result!!.resultCode == RESULT_OK && result!!.data != null) {
                    val speechtext = result!!.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<Editable>
                    reqTxt.text = speechtext[0]
                    var question = reqTxt.text.toString().trim()
                    getResponse(question){response ->
                        runOnUiThread{
                            txtResponse.text = response // text response will appear here
                            //readMore.text = response
                            //fullanswerText.text = response
                            tts.speak(txtResponse.text.toString(), TextToSpeech.QUEUE_ADD, null)
                        }
                    }
                }
            }
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction.commit()
    }

    fun getResponse(question: String, callback:(String) -> Unit){
        val apiKey1="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"
        val apiKey = "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"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"


        val requestBody="""
            {
            "prompt": "$question .Summarise response in less than 100 words" ,
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data", "empty")
                }

                val jsonObject =JSONObject(body)
                var jsonArray : JSONArray = jsonObject.getJSONArray("choices")
                val responseTxt = jsonArray.getJSONObject(0).getString("text")
                callback(responseTxt)

                tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
                    if(it == TextToSpeech.SUCCESS){
                        tts.language = Locale.US
                        tts.setSpeechRate(1.0f)
                        tts.speak(responseTxt.toString(), TextToSpeech.QUEUE_ADD, null)

                    }

                })
            }
        })
    }


    override fun onInit(status: Int, ) {
        if(status == TextToSpeech.SUCCESS){
            tts.language = Locale.US
            tts.setSpeechRate(1.1f)
            tts.setPitch(1.0f)
        }

    }

}





//    tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener {
//        if(it == TextToSpeech.SUCCESS){
//            tts.language = Locale.US
//            tts.setSpeechRate(1.0f)
//            tts.speak(txtResponse.text.toString(), TextToSpeech.QUEUE_ADD, null)
//
//        }
//    })




