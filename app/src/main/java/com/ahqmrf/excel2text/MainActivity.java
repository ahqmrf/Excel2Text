package com.ahqmrf.excel2text;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_CHOOSER_REQ = 33;
    private File file;
    private ProgressDialog dialog;
    XSSFSheet mySheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Converting to text file. Please wait...");

        findViewById(R.id.chooseFileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                chooseFile();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(getApplicationContext(), "Read file permission denied!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                        }).check();
            }
        });

        findViewById(R.id.getTextFileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readExcel();
            }
        });
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File"), FILE_CHOOSER_REQ);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_CHOOSER_REQ) {
                Uri uri = data.getData();
                try {
                    String filePath = RealPathUtil.getRealPath(getApplicationContext(), uri);
                    if (filePath != null) {
                        file = new File(filePath);
                        if (file.exists()) {
                            if (file.getName().endsWith(".xlsx")) {
                                Toast.makeText(this, "File chosen successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Invalid excel file!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(requestCode == 22) {
                doNext(data.getStringArrayListExtra("Columns"), data.getIntegerArrayListExtra("Ids"));
            }
        }
    }

    private void doNext(ArrayList<String> tmp, ArrayList<Integer> ids) {
        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + new Date().getTime() + ".txt";
            File textFile = new File(filePath);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();
            boolean hasHeader = ((AppCompatCheckBox)findViewById(R.id.checkbox)).isChecked();
            boolean isFirstRow = true;
            int row = 0;
            if(!hasHeader) row = 1;
            StringBuilder sb = new StringBuilder();
            HashSet<Integer> set = new HashSet<>();
            set.addAll(ids);

            while (rowIter.hasNext()) {
                XSSFRow myRow = (XSSFRow) rowIter.next();
                if(hasHeader && isFirstRow) {
                    isFirstRow = false;
                    row++;
                    continue;
                }
                sb.append(row);
                sb.append("\n");
                Iterator cellIter = myRow.cellIterator();

                int id = 0;
                while (cellIter.hasNext()) {
                    XSSFCell myCell = (XSSFCell) cellIter.next();
                    if(set.contains(id)) {
                        sb.append(tmp.get(id));
                        sb.append(": ");
                        sb.append(getCellValueAsString(myCell));
                        sb.append("\n");
                    }

                    id++;
                }
                sb.append("\n\n");
                row++;
            }

            System.out.println(sb.toString());
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(
                            textFile
                    ));
            writer.write(sb.toString());
            writer.close();
            Toast.makeText(getApplicationContext(), "File saved as " + textFile.getName() + " in the Downloads folder.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readExcel() {
        if (file == null) {
            return;
        }
        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        createTextFile();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getApplicationContext(), "Write file permission denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
    }

    @SuppressLint("StaticFieldLeak")
    private void createTextFile() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    // Create a POIFSFileSystem object
                    OPCPackage pkg = OPCPackage.open(file);

                    // Create a workbook using the File System
                    XSSFWorkbook myWorkBook = new XSSFWorkbook(pkg);

                    // Get the first sheet from workbook
                    mySheet = myWorkBook.getSheetAt(0);

                    /** We now need something to iterate through the cells.**/

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Iterator rowIter2 = mySheet.rowIterator();
                boolean hasHeader = ((AppCompatCheckBox)findViewById(R.id.checkbox)).isChecked();
                if(rowIter2.hasNext()) {
                    XSSFRow myRow = (XSSFRow) rowIter2.next();
                    Iterator cellIter = myRow.cellIterator();
                    int columns = myRow.getPhysicalNumberOfCells();
                    ArrayList<String> tmp = new ArrayList<>();
                    if(hasHeader) {
                        while (cellIter.hasNext()) {
                            XSSFCell myCell = (XSSFCell) cellIter.next();
                            tmp.add(getCellValueAsString(myCell));
                        }
                    } else {
                        for (int i = 0; i < columns; i++) tmp.add("");
                    }

                    Intent intent = new Intent(getApplicationContext(), SelectColumnActivity.class);
                    intent.putStringArrayListExtra("ColumnHeaders", tmp);
                    startActivityForResult(intent, 22);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }
        }.execute();
    }

    public String getCellValueAsString(Cell cell) {
        String strCellValue = null;
        if (cell != null) {
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    strCellValue = cell.toString();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                "dd/MM/yyyy");
                        strCellValue = dateFormat.format(cell.getDateCellValue());
                    } else {
                        Double value = cell.getNumericCellValue();
                        Long longValue = value.longValue();
                        strCellValue = new String(longValue.toString());
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    strCellValue = new String(new Boolean(
                            cell.getBooleanCellValue()).toString());
                    break;
                case Cell.CELL_TYPE_BLANK:
                    strCellValue = "";
                    break;
            }
        }
        return strCellValue;
    }
}
