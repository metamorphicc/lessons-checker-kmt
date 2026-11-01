package ru.is232.lessons;

import android.app.job.*;
import android.content.*;
import android.os.PersistableBundle;
import android.os.Handler;
import android.os.Looper;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Network work runs in a JobService, never inside an app-widget broadcast. */
public class LiveSyncService extends JobService {
    private static final int PERIODIC=23201,NOW=23202;
    private final Set<JobParameters> active=ConcurrentHashMap.newKeySet();
    private final Handler main=new Handler(Looper.getMainLooper());
    public static void ensureScheduled(Context c) {
        JobScheduler scheduler=c.getSystemService(JobScheduler.class);
        if(scheduler.getPendingJob(PERIODIC)==null) scheduler.schedule(new JobInfo.Builder(PERIODIC,new ComponentName(c,LiveSyncService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(ScheduleRepository.REFRESH_MS).setPersisted(true).build());
    }
    public static void requestNow(Context c,boolean force) {
        if(!ScheduleRepository.due(c,force)) return;
        JobScheduler scheduler=c.getSystemService(JobScheduler.class);
        if(scheduler.getPendingJob(NOW)!=null) return;
        PersistableBundle extras=new PersistableBundle(); extras.putBoolean("force",force);
        scheduler.schedule(new JobInfo.Builder(NOW,new ComponentName(c,LiveSyncService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setExtras(extras).build());
    }
    @Override public boolean onStartJob(JobParameters params) {
        active.add(params);
        ScheduleRepository.refreshAsync(this,params.getExtras().getBoolean("force",false),()->main.post(()->{
            LessonsWidget.updateAll(this);
            if(active.remove(params)) jobFinished(params,false);
        }));
        return true;
    }
    @Override public boolean onStopJob(JobParameters params) {active.remove(params);return true;}
}
