package com.example.combinedapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
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
        val btnMic = findViewById<ImageButton>(R.id.btnMic)
        val reqTxt = findViewById<TextView>(R.id.reqTxt)
        val stopButton = findViewById<ImageButton>(R.id.stopButton)
        val muteBtn = findViewById<ImageButton>(R.id.muteBtn)
        val fullanswerText = findViewById<TextView>(R.id.fullanswertext)
        //val historyBtn = findViewById<ImageButton>(R.id.historyBtn)
        tts = TextToSpeech(this, this)


        fun clearText(){
            txtResponse.text = ""
            reqTxt.text = ""
        }

        fun onStop() {
            tts.stop() // Stop the TTS engine if it's speaking when the activity is destroyed.
        }

        fun onUnmute() {
            tts.speak(txtResponse.text.toString(), TextToSpeech.QUEUE_ADD, null)
        }

        stopButton.setOnClickListener {
            onStop()
            clearText()
        }

        muteBtn.setOnClickListener{
            when (tts.isSpeaking){
                true -> {onStop()}
                false -> {onUnmute()}
            }
        }

       /* historyBtn.setOnClickListener{
            val intent = Intent(this,chat_history_activity::class.java)
            intent.putExtra("response",txtResponse.text )
            intent.putExtra("question", reqTxt.text)
            startActivity(intent)
        }*/



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

         // The speech to text conversion part
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
                if (result!!.resultCode == RESULT_OK && result!!.data != null) {
                    val speechtext = result!!.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<Editable>
                    reqTxt.text = speechtext[0]
                    var question = reqTxt.text.toString().trim()
                    // Getting the response from API part
                    getResponse(question){response ->
                        runOnUiThread{
                            txtResponse.text = response // text response will appear here
                            // The text to speech conversion part
                            tts.speak(txtResponse.text.toString(), TextToSpeech.QUEUE_ADD, null)
                        }
                    }
                }
            }
    }

    // The hint/showcase of the users input when he talks to the mic
    private fun showDialog(txtResponse: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_full_answer)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val dialog_text :TextView = dialog.findViewById(R.id.dialog_text)
        val dialog_back : Button = dialog.findViewById(R.id.dialog_back)

        dialog_text.text =  txtResponse

        dialog_back.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }


    fun getResponse(question: String, callback:(String) -> Unit){
        val apiKey1="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"
        val apiKey = "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"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"
        val requestBody="""
            {
            "prompt": "$question .Summarise the response in less than 70 words. Cater your response specifically to Singapore ASTAR SIMTech" ,
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
                    Log.v("data", "There is no response from chatGPT side. empty")
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






