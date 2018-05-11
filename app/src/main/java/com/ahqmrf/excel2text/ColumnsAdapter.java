package com.ahqmrf.excel2text;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Created by BS179 on 5/11/2018.
 */

public class ColumnsAdapter extends RecyclerView.Adapter {

    private ArrayList<ColumnItem> items;

    public ColumnsAdapter(ArrayList<ColumnItem> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.column_view, parent, false);
        return new ColumnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ColumnViewHolder) holder).bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ColumnViewHolder extends RecyclerView.ViewHolder {
        AppCompatEditText columnText;
        AppCompatCheckBox checkBox;

        public ColumnViewHolder(View itemView) {
            super(itemView);
            columnText = itemView.findViewById(R.id.columnText);
            checkBox = itemView.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ColumnItem item = items.get(getAdapterPosition());
                    item.setChecked(isChecked);
                    items.set(getAdapterPosition(), item);
                }
            });

            columnText.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ColumnItem item = items.get(getAdapterPosition());
                    item.setColumnText(s.toString());
                    items.set(getAdapterPosition(), item);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }
            });
        }

        void bind(ColumnItem item) {
            columnText.setText(item.getColumnText());
            checkBox.setChecked(item.isChecked());
        }
    }
}
