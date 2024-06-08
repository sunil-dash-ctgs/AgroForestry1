package co.kcagroforestry.app.network

import co.kcagroforestry.app.model.EmailVerifyModel
import co.kcagroforestry.app.model.LoginModel
import co.kcagroforestry.app.model.MobileVerifyModel
import co.kcagroforestry.app.model.PolygonSubmit
import co.kcagroforestry.app.model.StateIdModel
import co.kcagroforestry.app.model.UserModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {

    @Headers("Accept: application/json")
    @GET("V1/generateuniqueId")
    fun checkVersion(@Header("Authorization") token: String,@Query("version") version: String): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("register")
    fun register(@Body userModel: UserModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("validate")
    fun verifyNumber(@Body mobileVerifyModel: MobileVerifyModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("validate")
    fun verifyEmail(@Body emailVerifyModel: EmailVerifyModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("login")
    fun login(@Body loginModel: LoginModel): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/generateuniqueId")
    fun uniqueID(@Header("Authorization") token: String, @Query("version") version: String): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("State")
    fun state() : Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("districts/{id}")
    fun districts(@Path("id") id: String): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("taluka/{id}")
    fun taluka(@Path("id") id: String): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("villagepanchayat/{id}")
    fun villagepanchayat(@Path("id") id: String): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("village/{id}")
    fun Village(@Path("id") id: String): Call<ResponseBody>


    @Headers("Accept: application/json")
    @GET("relationshipowner")
    fun relationship(@Header("Authorization") token: String): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/agro-farmer-onboarding")
    fun submitOnboarding(
        @Header("Authorization") token: String,
        @Part farmer_name: MultipartBody.Part,
        @Part farmeruniquid: MultipartBody.Part,
        @Part farmer_age: MultipartBody.Part,
        @Part gender: MultipartBody.Part,
        @Part cast: MultipartBody.Part,
        @Part guardian_name: MultipartBody.Part,
        @Part nominee_name: MultipartBody.Part,
        @Part relationwith: MultipartBody.Part,
        @Part mobileno: MultipartBody.Part,
        @Part whats_no: MultipartBody.Part,
        @Part aadharnumber: MultipartBody.Part,
        @Part document1_photo: MultipartBody.Part,
        @Part document2_photo: MultipartBody.Part,
        @Part document3_photo: MultipartBody.Part,
        ): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/agro-farmer-location")
    fun submitLocationInfo(
        @Header("Authorization") token: String,
        @Part farmeruniquid: MultipartBody.Part,
        @Part state_id: MultipartBody.Part,
        @Part district_id: MultipartBody.Part,
        @Part taluka_id: MultipartBody.Part,
        @Part village_id: MultipartBody.Part,
        @Part pincode: MultipartBody.Part,
        @Part remark: MultipartBody.Part,
        @Part pattanumber: MultipartBody.Part,
        @Part survey_no: MultipartBody.Part,
        @Part totalarea: MultipartBody.Part,
        @Part planted_area: MultipartBody.Part,
        @Part document1_photo: MultipartBody.Part,
        @Part document2_photo: MultipartBody.Part,
        @Part document3_photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/agro-farmer-bank-details")
    fun submitBankDetails(
        @Header("Authorization") token: String,
        @Part farmeruniquid: MultipartBody.Part,
        @Part account_holder_name: MultipartBody.Part,
        @Part account_number: MultipartBody.Part,
        @Part bank_name: MultipartBody.Part,
        @Part ifsc_code: MultipartBody.Part,
        @Part branch: MultipartBody.Part,
        @Part document1_photo1: MultipartBody.Part,
        @Part document_photo2: MultipartBody.Part,
        @Part document3_photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/agro-farmer-document")
    fun submitDeclaration(
        @Header("Authorization") token: String,
        @Part farmeruniquid: MultipartBody.Part,
        @Part teramcondition: MultipartBody.Part,
        @Part farmer_signature: MultipartBody.Part,
        @Part enroller: MultipartBody.Part,
        @Part document1_photo: MultipartBody.Part,
        @Part document2_photo: MultipartBody.Part,
        @Part document3_photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/plantation-type")
    fun plantationtype(@Header("Authorization") token: String,) : Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/mix-plantation-type")
    fun mixplantationtype(@Header("Authorization") token: String,) : Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/plantation-info")
    fun submitPlantationinfo(
        @Header("Authorization") token: String,
        @Part farmeruniquid: MultipartBody.Part,
        @Part plantation_name: MultipartBody.Part,
        @Part type_of_plantation: MultipartBody.Part,
        @Part mixed_plantation: MultipartBody.Part,
        @Part no_of_plants: MultipartBody.Part,
        @Part girth_of_plant: MultipartBody.Part,
        @Part plant_spacing: MultipartBody.Part,
        @Part total_plants: MultipartBody.Part,
        @Part date_of_plantation: MultipartBody.Part,
        @Part year_of_plantation: MultipartBody.Part,
        @Part document1_photo: MultipartBody.Part,
        @Part document2_photo: MultipartBody.Part,
        @Part document3_photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/agro-cultivation-info")
    fun submitCultivationinfo(
        @Header("Authorization") token: String,
        @Part farmeruniquid: MultipartBody.Part,
        @Part soil_types: MultipartBody.Part,
        @Part plowwing_of_land: MultipartBody.Part,
        @Part fertigation_management: MultipartBody.Part,
        @Part irrigation_type: MultipartBody.Part,
        @Part water_management: MultipartBody.Part,
        @Part did_you_notice: MultipartBody.Part,
        @Part document1_photo: MultipartBody.Part,
        @Part document2_photo: MultipartBody.Part,
        @Part document3_photo: MultipartBody.Part,
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/agro-farmer-search")
    fun searchFramer(
        @Header("Authorization") token: String,
        @Query("data") mobile: String
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/agro-farmer-farmer_details")
    fun farmerDetails(
        @Header("Authorization") token: String,
        @Query("farmeruniquid") mobile: String
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/agro-farmer-check-polygon-nearby")
    fun polygonNearby(
        @Header("Authorization") token: String,
        @Query("farmeruniquid") farmeruniquid: String,
        @Query("lat") lat: String,
        @Query("lng") lng: String,
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/check-visit-no")
    fun checkvisitno(
        @Header("Authorization") token: String,
        @Query("farmeruniquid") farmeruniquid: String,
        @Query("visit_no") visit_no: String,
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/agro-farmer-check-lat-lng-inside")
    fun checkCoordinates(
        @Header("Authorization") token: String,
        @Query("farmeruniquid") farmeruniquid: String,
        @Query("lat") lat: String,
        @Query("lng") lng: String,
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @POST("V1/agro-farmer-polygon")
    fun submitPloygon(
        @Header("Authorization") token: String,
        @Body polygonSubmit: PolygonSubmit
    ): Call<ResponseBody>

    @Multipart
    @Headers("Accept: application/json")
    @POST("V1/agro-farmer-revisit")
    fun polygonImageSubmit(
        @Header("Authorization") token: String,
        @Part farmeruniquid: MultipartBody.Part,
        @Part polygon_id: MultipartBody.Part,
        @Part visit_no: MultipartBody.Part,
        @Part latitude: MultipartBody.Part,
        @Part longitude: MultipartBody.Part,
        @Part document_1: MultipartBody.Part,
        @Part document_2: MultipartBody.Part,
        @Part document_3: MultipartBody.Part,
        @Part status: MultipartBody.Part,
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("V1/agro-farmer-visit-no")
    fun farmervisitno(@Header("Authorization") token: String,) : Call<ResponseBody>

}