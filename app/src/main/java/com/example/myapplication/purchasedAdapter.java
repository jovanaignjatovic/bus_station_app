package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class purchasedAdapter extends RecyclerView.Adapter<purchasedAdapter.PurchasedTicketViewHolder> {

    private List<Ticket> purchasedTicketList;

    public purchasedAdapter(List<Ticket> purchasedTicketList) {
        this.purchasedTicketList = purchasedTicketList;
    }

    @NonNull
    @Override
    public PurchasedTicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ticket_item, parent, false);
        return new PurchasedTicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchasedTicketViewHolder holder, int position) {
        Ticket ticket = purchasedTicketList.get(position);
        if (ticket != null) {
            holder.startStationTv.setText("Od: " + ticket.getStartstation());
            holder.endStationTv.setText("Do: " + ticket.getEndstation());
            holder.dateTv.setText("Datum: " + ticket.getFormattedDate());
            holder.timeTv.setText("Vreme: " + ticket.getFormattedTime());

            holder.priceTv.setText("Cena: " + String.format( "%d RSD", ticket.getPrice()));
        }
    }

    @Override
    public int getItemCount() {
        return purchasedTicketList.size();
    }
    public static class PurchasedTicketViewHolder extends RecyclerView.ViewHolder {
        TextView startStationTv;
        TextView endStationTv;
        TextView dateTv;
        TextView timeTv;
        TextView priceTv;

        public PurchasedTicketViewHolder(@NonNull View itemView) {
            super(itemView);

            startStationTv = itemView.findViewById(R.id.itemStartStation);
            endStationTv = itemView.findViewById(R.id.itemEndStation);
            dateTv = itemView.findViewById(R.id.itemDate);
            timeTv = itemView.findViewById(R.id.itemTime);
            priceTv = itemView.findViewById(R.id.itemPrice);
        }
    }
}