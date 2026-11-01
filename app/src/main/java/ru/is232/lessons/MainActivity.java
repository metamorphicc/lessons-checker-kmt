package ru.is232.lessons;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.net.Uri;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class MainActivity extends Activity {
    static final int PAPER=Color.rgb(245,244,239), INK=Color.rgb(30,44,35), MUTED=Color.rgb(87,99,91),
            ACCENT=Color.rgb(53,92,67), SURFACE=Color.WHITE, TINT=Color.rgb(228,236,223);
    static final Locale RU=Locale.forLanguageTag("ru");
    private Settings settings;
    private LocalDate selected;
    private boolean followToday=true, editing=false;
    private LinearLayout column;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable tick=new Runnable() { public void run() {
        if (!editing) { if (followToday) selected=LocalDate.now(settings.zone()); show(); }
        handler.postDelayed(this, 30_000);
    }};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        settings=new Settings(this);
        selected=LocalDate.now(settings.zone());
        if (state!=null) { selected=LocalDate.parse(state.getString("date",selected.toString())); followToday=state.getBoolean("follow",true); }
        show();
    }
    @Override protected void onResume() { super.onResume(); LessonsWidget.updateAll(this); handler.post(tick); }
    @Override protected void onPause() { handler.removeCallbacks(tick); super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("date",selected.toString()); state.putBoolean("follow",followToday); super.onSaveInstanceState(state);
    }
    int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
    LinearLayout vertical() { LinearLayout v=new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    GradientDrawable background(int color, int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    TextView text(String value, int size, int color, boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setFontFeatureSettings("tnum"); if (bold) t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setPadding(0,dp(4),0,dp(4)); return t;
    }
    void gap(int height) { Space s=new Space(this); column.addView(s,new LinearLayout.LayoutParams(1,dp(height))); }
    Button button(String label, Runnable action) {
        Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(ACCENT); b.setMinHeight(dp(48));
        b.setOnClickListener(v->action.run()); return b;
    }
    void frame() {
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(PAPER);
        column=vertical(); column.setPadding(dp(22),dp(18),dp(22),dp(28)); scroll.addView(column);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        }); setContentView(scroll); scroll.requestApplyInsets();
    }
    void show() {
        editing=false; frame();
        LinearLayout heading=new LinearLayout(this); heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=text("Пары / ИС-232",20,INK,true);
        heading.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        heading.addView(button("Настройки",this::configure)); column.addView(heading); gap(20);
        column.addView(text(selected.format(DateTimeFormatter.ofPattern("EEEE",RU)),16,MUTED,false));
        column.addView(text(selected.format(DateTimeFormatter.ofPattern("d MMMM",RU)),36,INK,true));
        column.addView(text("Корпус 1 · Очные пары",14,MUTED,false));
        LinearLayout nav=new LinearLayout(this);
        Button prev=button("←",()->{selected=selected.minusDays(1);followToday=false;show();}); prev.setContentDescription("Предыдущий день");
        Button today=button("Сегодня",()->{selected=LocalDate.now(settings.zone());followToday=true;show();});
        Button next=button("→",()->{selected=selected.plusDays(1);followToday=false;show();}); next.setContentDescription("Следующий день");
        for (Button b : new Button[]{prev,today,next}) nav.addView(b,new LinearLayout.LayoutParams(0,-2,1));
        column.addView(nav); gap(16);
        List<ScheduleEngine.Lesson> lessons=settings.lessons(selected);
        ScheduleEngine.Moment moment=ScheduleEngine.moment(lessons,LocalTime.now(settings.zone()),settings.bells());
        boolean isToday=selected.equals(LocalDate.now(settings.zone()));
        LinearLayout summary=vertical(); summary.setPadding(dp(18),dp(14),dp(18),dp(14)); summary.setBackground(background(ACCENT,20));
        boolean distance=selected.getDayOfWeek()==DayOfWeek.SATURDAY;
        String headline=distance ? "Очных пар нет · ДО" : isToday ? moment.label() : lessons.isEmpty() ? "Без пар" : lessons.stream().map(ScheduleEngine.Lesson::slot).distinct().count()+" пары по расписанию";
        summary.addView(text(headline,18,Color.WHITE,true));
        ScheduleEngine.Lesson active=moment.current()!=null ? moment.current() : moment.next();
        if (isToday && active!=null) {
            boolean ambiguous=active.week()!=null && settings.week(selected)==null;
            summary.addView(text(ambiguous ? "Зависит от чётности недели" : active.shortTitle(),24,Color.WHITE,true));
            summary.addView(text(ScheduleEngine.room(active)+" · "+settings.bellLabel(active.slot()),14,Color.WHITE,false));
            if (moment.current()!=null && moment.next()!=null) summary.addView(text("Далее: "+moment.next().slot()+" пара · "+moment.next().shortTitle(),14,Color.WHITE,false));
        } else summary.addView(text(distance ? "Субботние занятия — дистанционно." : lessons.isEmpty() ? "Можно выдохнуть. Проверь только замены." : "Все пары дня — ниже",15,Color.WHITE,false));
        column.addView(summary); gap(20);
        for (ScheduleEngine.Lesson lesson : lessons) {
            LinearLayout card=vertical(); card.setPadding(dp(16),dp(12),dp(16),dp(14));
            card.setBackground(background(isToday && moment.current()!=null && lesson.slot()==moment.current().slot() ? TINT : SURFACE,16));
            String variant=lesson.week()!=null && settings.week(selected)==null ? " · "+ScheduleEngine.weekLabel(lesson.week()).toLowerCase(RU)+" неделя" : "";
            card.addView(text(lesson.slot()+" пара · "+settings.bellLabel(lesson.slot())+variant,13,ACCENT,true));
            card.addView(text(lesson.title(),19,INK,true));
            card.addView(text(ScheduleEngine.room(lesson),17,ACCENT,true));
            card.addView(text(lesson.teacher(),14,MUTED,false)); column.addView(card); gap(10);
        }
        gap(8);
        column.addView(button("Добавить виджет",this::pinWidget));
        column.addView(text("1 семестр 2026–2027 · Корпус 1\nСтатичное расписание из PDF, стр. 23. Замены, каникулы и праздники проверяй отдельно.\nЧасовой пояс: "+settings.zone().getId(),12,MUTED,false));
    }
    void pinWidget() {
        AppWidgetManager manager=getSystemService(AppWidgetManager.class);
        if (manager.isRequestPinAppWidgetSupported()) manager.requestPinAppWidget(new ComponentName(this,LessonsWidget.class),null,null);
        else new AlertDialog.Builder(this).setMessage("Зажми пустое место на главном экране → Виджеты → Пары · ИС-232. Выбери виджет и перетащи на экран.").setPositiveButton("Понятно",null).show();
    }
    void configure() {
        editing=true; frame();
        column.addView(text("Настройки",30,INK,true));
        if(Build.VERSION.SDK_INT>=31) {
            boolean exact=getSystemService(AlarmManager.class).canScheduleExactAlarms();
            column.addView(text(exact ? "Точные обновления разрешены" : "Обновление в момент звонка",18,INK,true));
            column.addView(text("Чтобы виджет переключал текущую пару вовремя, разреши точные будильники. Без этого Android может задерживать обновления; кнопка ↻ работает всегда.",14,MUTED,false));
            column.addView(button(exact ? "Настройки обновлений" : "Разрешить точные обновления",()->{
                try { startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()))); }
                catch(ActivityNotFoundException e) {new AlertDialog.Builder(this).setMessage("Найди приложение «Пары · ИС-232» в настройках Android → Специальный доступ → Будильники и напоминания.").setPositiveButton("Понятно",null).show();}
            })); gap(16);
        }
        Switch zone=new Switch(this); zone.setText("Часовой пояс телефона"); zone.setChecked(settings.deviceZone()); zone.setMinHeight(dp(48)); column.addView(zone);
        column.addView(text("По умолчанию: Новосибирск (UTC+7).",13,MUTED,false)); gap(16);
        column.addView(text("Звонки",22,INK,true));
        column.addView(text("Звонки уже заполнены по твоему скриншоту. Можно изменить все пять пар в формате 08:30 или очистить все поля, чтобы оставить только номера.",14,MUTED,false));
        EditText[][] fields=new EditText[5][2];
        for(int i=0;i<5;i++) {
            column.addView(text((i+1)+" пара",14,INK,true));
            LinearLayout row=new LinearLayout(this);
            for(int j=0;j<2;j++) {
                EditText input=new EditText(this); input.setSingleLine(); input.setTextSize(18);
                input.setText(settings.time(i+1,j==1)); input.setHint(j==0?"Начало":"Конец");
                input.setContentDescription((i+1)+" пара, "+(j==0?"начало":"конец")); input.setInputType(InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_TIME);
                fields[i][j]=input; row.addView(input,new LinearLayout.LayoutParams(0,dp(56),1));
            } column.addView(row);
        }
        TextView error=text("",14,Color.rgb(156,40,40),false); error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); column.addView(error);
        column.addView(button("Сохранить",()->{
            List<ScheduleEngine.Bell> bells=new ArrayList<>(); boolean any=false;
            for(EditText[] pair:fields) for(EditText input:pair) if(!input.getText().toString().trim().isEmpty()) any=true;
            if(any) {
                try {
                    for(EditText[] pair:fields) {
                        String start=pair[0].getText().toString().trim(),end=pair[1].getText().toString().trim();
                        if(!start.matches("([01]\\d|2[0-3]):[0-5]\\d") || !end.matches("([01]\\d|2[0-3]):[0-5]\\d")) throw new IllegalArgumentException();
                        bells.add(new ScheduleEngine.Bell(LocalTime.parse(start),LocalTime.parse(end)));
                    }
                    if(!ScheduleEngine.validBells(bells)) throw new IllegalArgumentException();
                } catch(RuntimeException e) {error.setText("Проверь все 5 пар: ЧЧ:ММ, начало раньше конца, пары не пересекаются."); return;}
            }
            settings.save(null,zone.isChecked(),bells);
            LessonsWidget.updateAll(this); selected=LocalDate.now(settings.zone()); followToday=true; show();
        }));
        column.addView(button("Отмена",this::show));
    }
}
