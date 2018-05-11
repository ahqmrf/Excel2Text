package com.ahqmrf.excel2text;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

public class SelectColumnActivity extends AppCompatActivity {

    RecyclerView columns;
    ArrayList<ColumnItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_column);

        setTitle("Select columns");

        columns = (RecyclerView) findViewById(R.id.columnListView);

        ArrayList<String> extra = getIntent().getStringArrayListExtra("ColumnHeaders");

        items = new ArrayList<>();
        for (String s : extra) {
            items.add(new ColumnItem(true, s));
        }

        ColumnsAdapter adapter = new ColumnsAdapter(items);
        columns.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        columns.setAdapter(adapter);

        findViewById(R.id.doneBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultBack();
            }
        });
    }

    private void sendResultBack() {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<String> headers = new ArrayList<>();
        int id = 0;
        for (ColumnItem item : items) {
            if (item.isChecked()) {
                ids.add(id);
            }
            headers.add(item.getColumnText());
            id++;
        }

        Intent intent = new Intent();
        intent.putStringArrayListExtra("Columns", headers);
        intent.putIntegerArrayListExtra("Ids", ids);
        setResult(RESULT_OK, intent);
        finish();
    }
}
