package com.example.rest_api_call_demo

import android.app.Dialog
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

//Step6 Create a helper-class to process a REST API Calls in background thread
//It needs context to know at which activity to run the Process Bar Dialog

class RestApiCall(private val context: Context, private var url: URL){

    //Add a secondary constructor to the class to give user an optional choice to add
    //a username and password during class instantiation, in case a "POST" transaction is intended
    private var username: String? = null
    private var password: String? = null
    constructor(context: Context, url: URL , username: String, password: String): this(context, url){
        this.username = username
        this.password = password
    }
    //Step7 Var to hold the Dialog for Custom Progress Bar
    private lateinit var customProgressDialog: Dialog


    //Step10a Function to execute the helper-class object
    suspend fun execute(): String{
        //Step10b When the helper-class object executed, start with the CustomProgressDialog
        withContext(Main){
            showProgressDialog()
        }
        delay(500) //Just for visualisation purposes of the Progress Dialog
        //Step13 Run the API Call, get a result
        val result = makeApiCall()
        //Step14 Sent a result to closing function
        afterCallFinish(result)
        //Step15 Return result of an REST API Call to an activity that utilised this helper-class
        return result
    }

    //Step12 Function that will get the API call result as a String
    private fun makeApiCall(): String {
        //prepare necessary variables for the function
        var result: String
        var connection: HttpURLConnection? = null
        try {//try to establish a connection to the url
            //Returns a URLConnection instance that represents a connection to remote object referred to by the URL
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true //doInput tells if dat is being Received(by default doInput will be true and doOutput false)
            connection.doOutput = false //doOutput tells if data is being sent

            //Step18 Send the POST Request (Separate Branch)
            if (username!=null&&password!=null) {//proceeds only when user inputs data to send(POST)
                connection.doOutput = true //doOutput tells if data is being sent
                connection.instanceFollowRedirects =
                    false //ignore redirections, skip connection instead
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "UTF-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.useCaches = false

                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject() //Create a JSON object to utilise
                jsonRequest.put("username", username)
                jsonRequest.put("password", password)
                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()
            }//Step18 -- END -- This is how sending POST Request is done


            val httpResult: Int = connection.responseCode // Check what is the status of connection
            if (httpResult == HttpURLConnection.HTTP_OK) { //if connection is successful, read the data
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                try { //while reader has lines to read, also assign reader output (it) to "line" var, checking it's not null
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line + "\n")
                    }
                } catch (e: IOException) {
                    //Prints the detailed description of this throwable to the standard output or standard error output
                    e.printStackTrace()
                } finally { //Regardless if reader could get the content or not, close the stream
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        //Prints the detailed description of this throwable to the standard output or standard error output
                        e.printStackTrace()
                    }
                } //Get the result of URL connection
                result = stringBuilder.toString()
            } else { //if connection is unsuccessful, read the Response Message
                result = connection.responseMessage
            }
        } catch (e: SocketTimeoutException) { //If connection TimeOut error
            result = "Connection Timeout"
        } catch (e: Exception) { // If any other connection error
            result = "Error : " + e.message
        } finally { //Regardless of connection success, close the connection
            connection?.disconnect()
        }
        //Return result to the function
        return result
    }

    //Step11 Once the execution of helper-class object finished, remove the CustomProgressDialog, run other activities if necessary
    private suspend fun afterCallFinish(result:String){
        withContext(Main){
            cancelProgressDialog()
        }
        //For testing purposes send a result to logs
        Log.i("JSON RESPONSE RESULT:", result)

        //Step17 - Updates on how to deconstruct a JSON object and access its elements
        //Create a JSON object holder
        val jsonObject = JSONObject(result)
        //Get specific JSON elements by their names
        val message = jsonObject.optString("message")
        val userId= jsonObject.optInt("user_id")
        Log.i("Message and userID:", "$message + $userId")

        //In case there is a JSON object inside a JSON object, this is the approach:
        val profileDetailsObject = jsonObject.optJSONObject("profile_details")
        //Use the received JSON object to access its details as in example above
        val isProfileCompleted = profileDetailsObject?.optBoolean("is_profile_completed")
        Log.i("Is profile completed:", "$isProfileCompleted")

        //In case the JSON object contains a list of inner JSON objects:
        val jsonListArray = jsonObject.optJSONArray("data_list")
        Log.i("Data List Size", "${jsonListArray?.length()}")//only for the number of items in the list

        //to get each individual JSON object in the list of JSON objects:
        for(i in 0 until jsonListArray.length()){
            Log.i("Value $i", "${jsonListArray[i]}") //only to display all the objects

            val jsonObjectFromList = jsonListArray[i] as JSONObject
            //access the content of each individual jsonObject from the list
            val id = jsonObjectFromList.optInt("id")
            val value= jsonObjectFromList.optString("value")
            Log.i("id and value:", "$id + $value")
        }
        //Step17 -- END -- This is how elements of JSON object get accessed by deconstructing the JSON object

    }

    //Step8 Function to show progress dialog
    private fun showProgressDialog(){
        customProgressDialog = Dialog(context)
        //Set content view to be the progress bar with text as in the prepared layout
        customProgressDialog.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog.show()
    }

    //Step9 Function to dismiss the progress dialog if it's visible
    private fun cancelProgressDialog(){
        customProgressDialog.dismiss()
    }
}
