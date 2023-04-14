package com.cnuhci.nightlynudge.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AlarmReceiver extends BroadcastReceiver {

   public static SharedPreferences timeData;

    @Override
    public void onReceive(Context context, Intent intent) {

        timeData = context.getSharedPreferences("timeData", Context.MODE_PRIVATE);

        if(intent.getAction().equals("com.cnuhci.nightlynudge.action.SleepAlarm")){

        }

        throw new UnsupportedOperationException("Not yet implemented");
    }
}