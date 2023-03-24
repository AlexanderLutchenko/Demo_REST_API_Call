package com.example.rest_api_call_demo


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.rest_api_call_demo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class MainActivity : AppCompatActivity() {
    //create a binding object for activity_main.xml layout
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Fill up the binding object with activity_main layout items
        binding = ActivityMainBinding.inflate(layoutInflater)
        //Now binding object can be used to access its layout items, set the contentView with it, etc.
        setContentView(binding.root)

        //Step16 Utilise the helper-class to make a REST API call at a provided URL
        lifecycleScope.launch(Dispatchers.IO) {
            //val url = URL("https://run.mocky.io/v3/1006f6cd-7d38-4f4b-9cfb-9b0ca2cfdeb7")
            val url = URL("http://www.mocky.io/v2/5e3826143100006a00d37ffa")
            val restApiCallResult = RestApiCall(this@MainActivity, url).execute()
            //Set the Main Activity TextView to display a RestAPICall result content
            withContext(Main){
            binding.RESTtext.text = restApiCallResult
            }
        }
    }
}