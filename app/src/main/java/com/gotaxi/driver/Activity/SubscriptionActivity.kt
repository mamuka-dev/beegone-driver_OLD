package com.gotaxi.driver.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cheetah.driver.Model.subsc_model.SubscriptionResponse
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants.response
import com.gotaxi.driver.API.RetrofitClient
import com.gotaxi.driver.Helper.CustomDialog
import com.gotaxi.driver.Helper.SharedHelper
import com.gotaxi.driver.Helper.URLHelper.base
import com.gotaxi.driver.R
import com.gotaxi.driver.Utilities.Utilities
import com.gotaxi.driver._base.BaseActivity
import kotlinx.android.synthetic.main.activity_subscription.*
import kotlinx.android.synthetic.main.item_subsc.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.cheetah.driver.Model.subsc_model.Package
class SubscriptionActivity : BaseActivity(), OnItemClickListener {
    var customDialog: CustomDialog? = null
    var adapter: SubscAdapter? = null
    var context: Context = this@SubscriptionActivity
    var packages: List<Package>? = ArrayList<Package>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        setupRv()

        btn_subscribe.setOnClickListener {
            if (adapter?.selectedPackage?.isSub == true) {

                // displayMessage()
                // var amount = adapter!!.selectedPackage.amount.toString().replace("$", "")
                showCnfrmDialog("subscribe")
            } else {
                displayMessage(getString(R.string.str_sel_pkg))
            }

        }
        btn_cancel.setOnClickListener {
            showCnfrmDialog("cancel_sub")
        }

    }

    fun setupRv() {
        adapter = SubscAdapter(packages, this@SubscriptionActivity)
        val mLayoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(context)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        getSubscList()

    }


    private fun getSubscList() {
        customDialog = CustomDialog(this)
        customDialog?.show()

        val token = "Bearer" + " " + SharedHelper.getKey(context, "access_token")
        val call = RetrofitClient.getInstance(base).api.getSubList(token)
        call.enqueue(object : Callback<SubscriptionResponse?> {
            override fun onResponse(
                call: Call<SubscriptionResponse?>,
                response: Response<SubscriptionResponse?>
            ) {
                customDialog?.dismiss()
                handleResponseData(response)
                Utilities.print("Response",response.toString());
            }

            override fun onFailure(call: Call<SubscriptionResponse?>, t: Throwable) {
                customDialog?.dismiss()
                Log.v("TAG", t.localizedMessage.toString())
            }
        })

    }
    private fun setSubscription(pkgId: String?) {
        customDialog = CustomDialog(this)
        customDialog?.show()

        val token = "Bearer" + " " + SharedHelper.getKey(context, "access_token")
        val call = RetrofitClient.getInstance(base).api.setSubsc(token, pkgId)
        call.enqueue(object : Callback<SubscriptionResponse?> {
            override fun onResponse(
                call: Call<SubscriptionResponse?>,
                response: retrofit2.Response<SubscriptionResponse?>
            ) {
                Utilities.print("Response", response.body().toString())
                customDialog?.dismiss()
                if (response.body()?.status == 200) {
                    updateCancelVisiblity(true)
                }
                else if(response.body()?.status == 405){
                    startActivity(Intent(this@SubscriptionActivity, ManageCardActivity::class.java))
                }
                response.body()?.message?.let { displayMessage(it) }
            }

            override fun onFailure(call: Call<SubscriptionResponse?>, t: Throwable) {
                customDialog?.dismiss()
                Utilities.print("Response", response.toString())
                displayMessage(getString(R.string.something_went_wrong))
            }
        })
    }

    private fun cancelSubscription() {
        customDialog = CustomDialog(this)
        customDialog?.show()

        val token = "Bearer" + " " + SharedHelper.getKey(context, "access_token")
        val call = RetrofitClient.getInstance(base).api.cancelSubsc(token)
        call.enqueue(object : Callback<SubscriptionResponse?> {
            override fun onResponse(
                call: Call<SubscriptionResponse?>,
                response: retrofit2.Response<SubscriptionResponse?>
            ) {
                Utilities.print("Response", response.body().toString())
                customDialog?.dismiss()
                if (response.body()?.status == 200) {
                    getSubscList()
                    updateCancelVisiblity(false)

                }
                response.body()?.message?.let { displayMessage(it) }
            }

            override fun onFailure(call: Call<SubscriptionResponse?>, t: Throwable) {
                customDialog?.dismiss()
                Log.v("TAG", t.localizedMessage.toString())
                Utilities.print("Response", t.localizedMessage.toString())
                displayMessage(getString(R.string.something_went_wrong))
            }
        })
    }
    fun handleResponseData(response: Response<SubscriptionResponse?>) {

        if (response.isSuccessful) {
            Utilities.print("Response", response.body().toString())
            val packages = response.body()?.packages

            if (packages!!.isNotEmpty()) {
                errorLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                lay_sub.visibility = View.VISIBLE
                adapter?.updateList(packages)

                if(adapter?.isSubscribe() == true)
                    updateCancelVisiblity(true)
            } else {
                errorLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                lay_sub.visibility = View.GONE

            }

        } else {
            errorLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            lay_sub.visibility = View.GONE
        }
    }

    override fun onSubscItemClick(itemPackage: Package?) {
//        itemPackage?.let {
//            displayMessage(it.title.toString())
//        }
    }

    override fun updateCancelVisiblity(isCancelVisible: Boolean) {
        if (isCancelVisible) {
            btn_cancel.visibility = View.VISIBLE
            space.visibility = View.VISIBLE
        } else {
            btn_cancel.visibility = View.GONE
            space.visibility = View.GONE
        }
    }




    fun showCnfrmDialog(type: String) {
        val msg: String
        msg = if (type == "cancel_sub")
            getString(R.string.str_cnfrm_cancel_subsc)
        else
            getString(R.string.str_cnfrm_subsc)


        val builder = AlertDialog.Builder(this@SubscriptionActivity)
        builder.setMessage(msg).setCancelable(false)
        builder.setNegativeButton(
            getString(R.string.no)
        ) { dialog, which -> dialog.dismiss() }
        builder.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, which ->
            if (type == "cancel_sub")
                cancelSubscription()
            else
                setSubscription(adapter!!.selectedPackage.id.toString())
        }
        builder.show()
    }

    class SubscAdapter(var packages: List<Package>?, var listener: OnItemClickListener) :
        RecyclerView.Adapter<SubscAdapter.MyViewHolder>() {

        var selectedPackage = Package()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_subsc, parent, false)
            return MyViewHolder(v)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            packages?.let { holder.setData(it[position]) }

        }

        override fun getItemCount(): Int {
            return packages!!.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun setData(pkg: Package) {
                itemView.tv_pkg_title.text = "${pkg.title} ${pkg.amount}"
                itemView.btn_radio.isChecked = pkg.isSub == true
                if (pkg.isSub == true) {
                    selectedPackage = pkg
                   // listener.updateCancelVisiblity(true)
                }

                itemView.setOnClickListener {
                    pkg.isSub = true
                    selectedPackage = pkg
                    checkSelectedPackage()
                    notifyDataSetChanged()
                }


            }
        }

        fun updateList(packages: List<Package>?) {
            this.packages = null
            this.packages = packages
            notifyDataSetChanged()
        }

        fun checkSelectedPackage() {
            for (pkg in packages!!) {
                pkg.isSub = pkg.pkgId == selectedPackage.pkgId

            }
        }
        fun isSubscribe():Boolean {
            for (pkg in packages!!) {
                if(pkg.isSub==true)
                    return true
            }
            return false
        }
    }


}

interface OnItemClickListener {
    fun onSubscItemClick(item: Package?)
    fun updateCancelVisiblity(isCancelVisible: Boolean)
}