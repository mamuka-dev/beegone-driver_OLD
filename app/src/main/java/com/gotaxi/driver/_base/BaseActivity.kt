package com.gotaxi.driver._base

import android.graphics.Color
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.gotaxi.driver.R
import com.gotaxi.driver.Utilities.Utilities
import kotlinx.android.synthetic.main.login_otp_screen_activity.*
import java.util.*

open class BaseActivity : AppCompatActivity(){


    fun ShowHidePass(view: View) {
        if (view.id === R.id.show_pass_btn) {
            if (pinView.transformationMethod.equals(PasswordTransformationMethod.getInstance())) {
                (view as ImageView).setImageResource(R.drawable.ic_visibility_off)
                //Show Password
                pinView.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                (view as ImageView).setImageResource(R.drawable.ic_visibility)
                //Hide Password
                pinView.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }
    }
    fun onbackImgClick(view: View) {
        super.onBackPressed()
    }

    open fun displayMessage(toastString: String) {
        Utilities.print("displayMessage", "" + toastString)
        try {
            Objects.requireNonNull(currentFocus)?.let {
                Snackbar.make(it, toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).setTextColor(Color.WHITE).show()
            }
        } catch (e: Exception) {
            try {
                Toast.makeText(BaseActivity@this, "" + toastString, Toast.LENGTH_SHORT).show()
            } catch (ee: Exception) {
                ee.printStackTrace()
            }
        }
    }
}