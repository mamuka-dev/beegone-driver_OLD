package com.gotaxi.driver.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.gotaxi.driver.Models.CardDetails;
import com.gotaxi.driver.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NewPaymentListAdapter extends ArrayAdapter<CardDetails>  {
    ClickListenerInterface clickListenerInterface;

    List<CardDetails> list;

    Context context;

    Activity activity;

    public NewPaymentListAdapter(Context context, List<CardDetails> list, Activity activity, ClickListenerInterface clickListenerInterface) {
        super(context, R.layout.manage_card_list_item, list);
        this.context = context;
        this.list = list;
        this.activity = activity;
        this.clickListenerInterface = clickListenerInterface;

    }

    @NotNull
    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("ViewHolder") View itemView = inflater.inflate(R.layout.manage_card_list_item, parent, false);

        RelativeLayout root = itemView.findViewById(R.id.root);
        ImageView paymentTypeImg = itemView.findViewById(R.id.paymentTypeImg);
        ImageView tickImg = itemView.findViewById(R.id.img_tick);
        ImageView delImg = itemView.findViewById(R.id.img_del);
        TextView cardNumber = itemView.findViewById(R.id.cardNumber);


        try {

            if (list.get(position).getBrand().equalsIgnoreCase("MasterCard")) {
                paymentTypeImg.setImageResource(R.drawable.master_card_logo_svg);
            } else if (list.get(position).getBrand().equalsIgnoreCase("Visa")) {
                paymentTypeImg.setImageResource(R.drawable.visa);
            }
            else if (list.get(position).getBrand().equalsIgnoreCase("American Express")) {
                paymentTypeImg.setImageResource(R.drawable.img_ae);
            }
            else {
                paymentTypeImg.setImageResource(R.drawable.ic_credit_card_black_24dp);
            }
            cardNumber.setText("xxxx - xxxx - xxxx - " + list.get(position).getLast_four());
            if (list.get(position).is_default.equals("1")) {
                tickImg.setVisibility(View.VISIBLE);
                delImg.setVisibility(View.INVISIBLE);
            } else {
                tickImg.setVisibility(View.GONE);
                delImg.setVisibility(View.VISIBLE);
            }


        } catch (Exception e) {

            e.printStackTrace();

        }
        delImg.setOnClickListener(view -> {
            clickListenerInterface.itemClick(view.getId(), position);
        });
        root.setOnClickListener(view -> {
            clickListenerInterface.itemClick(view.getId(), position);
        });
        return itemView;
    }



    public interface ClickListenerInterface {
          void itemClick(int id,int position);
    }
}
