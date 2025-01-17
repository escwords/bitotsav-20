package `in`.bitotsav.bitotsav_20.profile.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.bitotsav.bitotsav_20.R
import `in`.bitotsav.bitotsav_20.VolleyService
import `in`.bitotsav.bitotsav_20.config.Secret
import `in`.bitotsav.bitotsav_20.profile.data.User
import `in`.bitotsav.bitotsav_20.utils.SharedPrefUtils
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_register_fragment_step_two.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.NumberFormatException

/**
 * A simple [Fragment] subclass.
 */
class RegisterFragmentStepTwo : Fragment(), View.OnClickListener {

    lateinit var navController: NavController

    lateinit var email: String
    lateinit var token: String
    lateinit var phone: String

    companion object {
        fun newInstance() = RegisterFragmentStepTwo()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        email = arguments?.get("email") as String
        phone = arguments?.get("phone") as String
        token = SharedPrefUtils(context!!).getToken()!!
        println("argument received step two email: $email, phone: $phone and token: $token")
        return inflater.inflate(R.layout.fragment_register_fragment_step_two, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<MaterialButton>(R.id.verify_btn).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.verify_btn -> tryToVerify()
        }
    }

    private fun tryToVerify() {
        val name = register_name.text.toString()
        val gender = when(register_gender.text.toString()) {
            "M" -> 1
            "F" -> 2
            else -> -1
        }
        val clgName = register_clg_name.text.toString()
        val clgCity = register_clg_city.text.toString()
        val clgState = register_clg_state.text.toString()
        val clgId = register_clg_id.text.toString()
        val emailOtp = register_email_otp.text.toString()
        val mobileOtp = register_mobile_otp.text.toString()

        if (name.isNotBlank() && (gender == 1 || gender == 2) && clgName.isNotBlank() && clgCity.isNotBlank() && clgState.isNotBlank() && clgId.isNotBlank() && emailOtp.isNotBlank() && mobileOtp.isNotBlank()) {
            validateCaptcha(name, gender, clgName, clgCity, clgState, clgId, emailOtp, mobileOtp)
        } else {
            Snackbar.make(verify_btn, "Invalid data found", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun validateCaptcha(
        name: String,
        gender: Int,
        clgName: String,
        clgCity: String,
        clgState: String,
        clgId: String,
        emailOtp: String,
        mobileOtp: String
    ) {
        reg_two_progress_bar.visibility = View.VISIBLE
        SafetyNet.getClient(activity!!).verifyWithRecaptcha(Secret.recptchaSiteKey)
            .addOnSuccessListener(activity!!) {response ->
                println("recaptcha success: ${response.tokenResult}")
                reg_two_progress_bar.visibility = View.GONE
                sendVerificationRequest(name, gender, clgName, clgCity, clgState, clgId, emailOtp, mobileOtp, response.tokenResult)
            }
            .addOnFailureListener(activity!!) {
                println("recaptcha failure: $it")
                reg_two_progress_bar.visibility = View.GONE
                Snackbar.make(parent_register_two_frag, "Recaptcha failed", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun sendVerificationRequest(
        name: String,
        gender: Int,
        clgName: String,
        clgCity: String,
        clgState: String,
        clgId: String,
        emailOtp: String,
        mobileOtp: String,
        captchaToken: String
    ) {
        reg_two_progress_bar.visibility = View.VISIBLE
        val request = object : StringRequest(Method.POST, "https://bitotsav.in/api/auth/verify",
            Response.Listener {response ->
                reg_two_progress_bar.visibility = View.GONE
                println("response: $response")
                try {
                    val res = JSONObject(response)
                    val status = res.get("status")
                    println("status: $status")
                    when (status) {
                        200 -> {
                            val user = User(
                                -1,
                                name,
                                email,
                                phone,
                                gender,
                                clgName,
                                clgCity,
                                clgState,
                                clgId,
                                res.getBoolean("isVerified"),
                                false,
                                null,
                                null,
                                null
                            )
                            SharedPrefUtils(context!!).setToken(res.getString("token"))
                            println("token: ${res.get("token")}, isVerified: ${res.get("isVerified")}")
                            checkVerificationStatusAndSave(user)
                        }
                        else -> {
                            println("message: ${res.get("message")}")
                            Snackbar.make(parent_register_two_frag, res.getString("message"), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(activity!!, "Unknown error occurred!!", Toast.LENGTH_SHORT).show()
                }
        }, Response.ErrorListener {
                reg_two_progress_bar.visibility = View.GONE
                println("Unknown error occurred!!")
                Snackbar.make(parent_register_two_frag, "Unknown error occurred!!", Snackbar.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "x-access-token" to token
                )
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                val body = JSONObject()
                body.put("emailOTP", emailOtp)
                body.put("mobileOTP", mobileOtp)
                body.put("gender", gender)
                body.put("name", name)
                body.put("clgName", clgName)
                body.put("clgCity", clgCity)
                body.put("clgState", clgState)
                body.put("clgId", clgId)
                body.put("client", "app")
                body.put("captchaToken", captchaToken)
                return body.toString().toByteArray(Charsets.UTF_8)
            }
        }

        VolleyService.getRequestQueue(context!!).add(request)
    }

    private fun checkVerificationStatusAndSave(user: User) {
        reg_two_progress_bar.visibility = View.VISIBLE
        // TODO: Replace with /getUserDashboard endpoint
        val request = object : StringRequest(Method.GET, "https://bitotsav.in/api/auth/getUserState",
            Response.Listener { response ->
                reg_two_progress_bar.visibility = View.GONE
                println(response)
                // TODO: Inside try-catch
                val res = JSONObject(response)
                if (res.get("verified") != null) {
                    saveAndNavigate(user)
                } else {
                    Snackbar.make(parent_register_two_frag, "Unknown error occurred!!", Snackbar.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                reg_two_progress_bar.visibility = View.GONE
                println("step two: error occurred - ${it.message}")
                Snackbar.make(parent_register_two_frag, "Unknown error occurred!!", Snackbar.LENGTH_SHORT).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "x-access-token" to token
                )
            }
        }
        VolleyService.getRequestQueue(context!!).add(request)
    }

    // TODO: replace with just navigation after /getUserDashboard endpoint is active
    private fun saveAndNavigate(user: User) {
        SharedPrefUtils(context!!).setUser(user)
        navController.navigate(R.id.action_registerFragmentStepTwo_to_profileFragment)
    }

    // TODO: redundant - remove it
    private fun navigateToProfile() {
        navController.navigate(R.id.action_registerFragmentStepTwo_to_profileFragment)
    }
}
