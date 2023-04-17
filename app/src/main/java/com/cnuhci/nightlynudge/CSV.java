package com.cnuhci.nightlynudge;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

public class CSV {
    public static File file = null;
    public static BufferedWriter bw = null;
    public static String filePath = String.format(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)
            + "/%s.csv", Data.FILE_NAME);

    public CSV(){
        try {
            file = new File(filePath);
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.write("TIMESTAMP,SLEEP_TIME,WAKE_TIME,BATTERY_STATUS,BATTERY_PERCENTAGE,BEDTIME_USAGE_TIME,TOTAL_USAGE_TIME,WINDOW_ON,ALARM,ACTION\n");
            bw.flush();
            bw.close();
        }catch (Exception e){
            e.getStackTrace();
        }

    }

    public static void writeCSV(@Nullable String data){

        try{
            file = new File(filePath);
            bw = new BufferedWriter(new FileWriter(file, true));
            data = Data.TIMESTAMP + "," + Data.SLEEP_TIME + "," + Data.WAKE_TIME + ","
                    + Data.BATTERY_STATUS + "," + Data.BATTERY_PERCENTAGE + ","
                    + Data.BEDTIME_USAGE_TIME + "," + Data.TOTAL_USAGE_TIME + ","
                    + Data.WINDOW_ON + "," + Data.ALARM + "," + Data.ACTION +  "\n";

            bw.write(data);
            bw.flush();
            bw.close();

            Log.d("TEST", "saved : " + data);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
