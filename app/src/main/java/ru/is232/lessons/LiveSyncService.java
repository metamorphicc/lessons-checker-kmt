package ru.is232.lessons;

import android.app.job.*;
import android.content.*;
import android.os.PersistableBundle;

/** Network work runs in a JobService, never inside an app-widget broadcast. */
public class LiveSyncService extends JobService {
    private static final int PERIODIC=23201,NOW=23202;
    private volatile boolean stopped;
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
        stopped=false;
        ScheduleRepository.refreshAsync(this,params.getExtras().getBoolean("force",false),()->{
            LessonsWidget.updateAll(this);
            if(!stopped) jobFinished(params,false);
        });
        return true;
    }
    @Override public boolean onStopJob(JobParameters params) {stopped=true;return true;}
}
