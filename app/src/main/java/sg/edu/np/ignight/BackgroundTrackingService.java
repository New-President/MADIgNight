package sg.edu.np.ignight;

import static sg.edu.np.ignight.ActivityReport_Activity.IgNightCounter;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundTrackingService extends Service {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sPedit;
    public BackgroundTrackingService(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("IgNight",MODE_PRIVATE);
        sPedit = sharedPreferences.edit();
        TimerTask detectApp = new TimerTask() {
            @Override
            public void run() {
                sharedPreferences = getSharedPreferences("IgNight",MODE_PRIVATE);
                sPedit = sharedPreferences.edit();
                UsageStatsManager usageStatsManager = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
                long endTime = System.currentTimeMillis();
                long beginTime = endTime-(1000);
                List<UsageStats> usageStats = usageStatsManager
                        .queryUsageStats(UsageStatsManager
                                        .INTERVAL_DAILY
                                ,beginTime
                                ,endTime);
                if (usageStats!=null){
                    for(UsageStats usageStat:usageStats){
                        if(usageStat.getPackageName()
                                .toLowerCase()
                                .contains("sg.edu.np.ignight")){
                            sPedit.putLong(IgNightCounter,
                                    usageStat.getTotalTimeInForeground());
                        }
                        sPedit.apply();
                    }
                }
            }
        };
        Timer detectAppTimer = new Timer();
        detectAppTimer.scheduleAtFixedRate(detectApp,0,1000);
        return super.onStartCommand(intent,flags,startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
