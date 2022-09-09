package com.umutdmr.astroapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{

    var sunSign = "Aries"
    var resultView: TextView? = null
    lateinit var radioGroup: RadioGroup
    lateinit var selectedRadioButton: RadioButton
    lateinit var day: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var buttonView: Button = findViewById(R.id.button)
        buttonView.setOnClickListener {
            GlobalScope.async {
                getPredictions()
            }
        }

        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.sunsigns,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter;
        spinner.onItemSelectedListener = this

        resultView = findViewById(R.id.resultView)

        radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        sunSign = "Aries"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            sunSign = parent.getItemAtPosition(position).toString()
        }
    }
    suspend fun getPredictions() {
        try {
            val selectedRadioButtonId: Int = radioGroup.checkedRadioButtonId
            selectedRadioButton = findViewById(selectedRadioButtonId)
            day = selectedRadioButton.text.toString()
            val result = GlobalScope.async {
                callAztroAPI("https://sameer-kumar-aztro-v1.p.rapidapi.com/?sign=" + sunSign + "&day=" + day)
            }.await()

            onResponse(result)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callAztroAPI(apiUrl:String ):String?{
        var result: String? = ""
        val url: URL;
        var connection: HttpURLConnection? = null
        try {
            url = URL(apiUrl)
            connection = url.openConnection() as HttpURLConnection
            // set headers for the request
            // set host name
            connection.setRequestProperty("x-rapidapi-host", "sameer-kumar-aztro-v1.p.rapidapi.com")

            // set the rapid-api key
            connection.setRequestProperty("x-rapidapi-key", "853529855amshfc067dc937e41eep1eca63jsnf1cab2e47583")
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded")
            // set the request method - POST
            connection.requestMethod = "POST"
            val `in` = connection.inputStream
            val reader = InputStreamReader(`in`)

            // read the response data
            var data = reader.read()
            while (data != -1) {
                val current = data.toChar()
                result += current
                data = reader.read()
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // if not able to retrieve data return null
        return null

    }
    private fun onResponse(result: String?) {
        try {

            // convert the string to JSON object for better reading
            val resultJson = JSONObject(result)

            // Initialize prediction text
            val whichDay = day.capitalize()
            var prediction = whichDay + "'s prediction "
            prediction += this.sunSign + ". "

            // Update text with various fields from response
            prediction += resultJson.getString("date_range") + ". "
            prediction += resultJson.getString("description")

            //Update the prediction to the view
            setText(this.resultView,prediction)

        } catch (e: Exception) {
            e.printStackTrace()
            this.resultView!!.text = "Oops!! something went wrong, please try again"
        }
    }

    private fun setText(text: TextView?, value: String) {
        runOnUiThread { text!!.text = value }
    }
}