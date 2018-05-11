package com.ahqmrf.excel2text;

/**
 * Created by BS179 on 5/11/2018.
 */

public class ColumnItem {
    private boolean checked;
    private String columnText;

    public ColumnItem(boolean checked, String columnText) {
        this.checked = checked;
        this.columnText = columnText;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getColumnText() {
        return columnText;
    }

    public void setColumnText(String columnText) {
        this.columnText = columnText;
    }
}
