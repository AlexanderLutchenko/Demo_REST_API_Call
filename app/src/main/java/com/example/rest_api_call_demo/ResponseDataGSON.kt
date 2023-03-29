package com.example.rest_api_call_demo

//Step19.2 Create a data class that will be used to receive a JSON object of its own specific model
//GSON will map the prepared here variables to the JSON file variables(keys) names
data class ResponseDataGSON(
    val message: String,
    val user_id: Int,
    val name: String,
    val email: String,
    val mobile: Long,
    val profile_details: ProfileDetails,
    val data_list: List<DataListDetail>
)

data class ProfileDetails(
    val is_profile_completed: Boolean,
    val rating: Double
)

data class DataListDetail(
    val id: Int,
    val value: String
)
