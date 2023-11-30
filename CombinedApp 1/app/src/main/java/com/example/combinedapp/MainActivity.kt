package com.example.combinedapp

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

class MainActivity : AppCompatActivity() {
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
                            txtResponse.text = response
                        }
                    }
                }
            }
    }

    fun getResponse(question: String, callback:(String) -> Unit){
        val apiKey1="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"
        val apiKey = "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"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"


        val requestBody="""
            {
            "prompt": "$question",
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
                        tts.language = Locale.CHINA
                        tts.setSpeechRate(1.0f)
                        tts.speak(responseTxt.toString(), TextToSpeech.QUEUE_ADD,null)
                    }
                })

            }
        })



    }

}


