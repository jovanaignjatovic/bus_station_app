package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TicketAdapter extends ArrayAdapter<Ticket> {

    private Context mContext;
    private List<Ticket> mTicketList;

    public TicketAdapter(@NonNull Context context, ArrayList<Ticket> ticketList) {
        super(context, 0, ticketList);
        this.mContext = context;
        this.mTicketList = ticketList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        }

        Ticket currentTicket = mTicketList.get(position);

        TextView tvStartStation = listItem.findViewById(R.id.tvStartStation);
        TextView tvEndStation = listItem.findViewById(R.id.tvEndStation);
        TextView tvDate = listItem.findViewById(R.id.tvDate);
        TextView tvTime = listItem.findViewById(R.id.tvTime);
        TextView tvPrice = listItem.findViewById(R.id.tvPrice);

        tvStartStation.setText("Od: " + currentTicket.getStartstation());
        tvEndStation.setText("Do: " + currentTicket.getEndstation());
        tvDate.setText("Datum: " + currentTicket.getFormattedDate());
        tvTime.setText("Vreme: " + currentTicket.getFormattedTime());
        tvPrice.setText("Cena: " + currentTicket.getPrice() + " RSD");

        return listItem;
    }
}