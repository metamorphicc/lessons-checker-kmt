package ru.is232.lessons;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.content.res.ColorStateList;
import android.os.*;
import android.net.Uri;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class MainActivity extends Activity {
    private int PAPER, INK, MUTED, ACCENT, SURFACE, TINT, RULE, ERROR;
    private Typeface face;
    private boolean sourceExpanded;
    private android.window.OnBackInvokedCallback settingsBack;
    static final Locale RU=Locale.forLanguageTag("ru");
    private Settings settings;
    private LocalDate selected;
    private boolean followToday=true, editing=false;
    private boolean resumed;
    private LinearLayout column;
    private ScrollView screen;
    private Integer pendingScroll;
    private LocalDate renderedDate;
    private EditText[][] bellFields;
    private Switch zoneField;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable tick=new Runnable() { public void run() {
        if (!editing) { if (followToday) selected=LocalDate.now(settings.zone()); show(); }
        handler.postDelayed(this, 30_000);
    }};

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        PAPER=getColor(R.color.paper); INK=getColor(R.color.ink); MUTED=getColor(R.color.muted);
        ACCENT=getColor(R.color.accent); SURFACE=getColor(R.color.surface); TINT=getColor(R.color.tint);
        RULE=getColor(R.color.rule); ERROR=getColor(R.color.error); face=getResources().getFont(R.font.manrope);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|
            (Build.VERSION.SDK_INT>=27?View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR:0));
        if(Build.VERSION.SDK_INT>=33) settingsBack=this::show;
        settings=new Settings(this);
        sourceExpanded=state!=null && state.getBoolean("sourceExpanded",false);
        selected=LocalDate.now(settings.zone());
        if (state!=null) { selected=LocalDate.parse(state.getString("date",selected.toString())); followToday=state.getBoolean("follow",true); }
        if(state!=null && state.getBoolean("editing",false)) {
            configure();
            String[] draft=state.getStringArray("draftTimes");
            if(draft!=null && draft.length==10) for(int i=0;i<5;i++) for(int j=0;j<2;j++) bellFields[i][j].setText(draft[i*2+j]);
            zoneField.setChecked(state.getBoolean("draftZone",settings.deviceZone()));
        } else show();
        if(state!=null) restoreScroll(state.getInt("scroll",0));
    }
    @Override protected void onResume() {
        super.onResume(); resumed=true; LiveSyncService.ensureScheduled(this);
        LessonsWidget.updateAll(this); handler.post(tick); refreshLive(false);
    }
    @Override protected void onPause() { resumed=false;handler.removeCallbacks(tick); super.onPause(); }
    void refreshLive(boolean force) {
        ScheduleRepository.refreshAsync(this,force,()->handler.post(()->{
            LessonsWidget.updateAll(getApplicationContext());
            if(resumed && !editing) show();
        }));
        if(!editing) show();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("date",selected.toString()); state.putBoolean("follow",followToday); super.onSaveInstanceState(state);
        state.putBoolean("sourceExpanded",sourceExpanded);
        state.putBoolean("editing",editing); state.putInt("scroll",screen==null?0:screen.getScrollY());
        if(editing && bellFields!=null) {
            String[] draft=new String[10];for(int i=0;i<5;i++) for(int j=0;j<2;j++) draft[i*2+j]=bellFields[i][j].getText().toString();
            state.putStringArray("draftTimes",draft);state.putBoolean("draftZone",zoneField.isChecked());
        }
    }
    int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
    LinearLayout vertical() { LinearLayout v=new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    GradientDrawable background(int color, int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    TextView text(String value, int size, int color, boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(face); t.setFontVariationSettings(bold ? "'wght' 650" : "'wght' 450");
        t.setFontFeatureSettings("tnum"); t.setIncludeFontPadding(false);
        t.setLineSpacing(dp(3),1); t.setPadding(0,dp(4),0,dp(4)); return t;
    }
    void gap(int height) { Space s=new Space(this); column.addView(s,new LinearLayout.LayoutParams(1,dp(height))); }
    android.graphics.drawable.Drawable controlBackground(int color, int radius) {
        StateListDrawable states=new StateListDrawable();
        GradientDrawable focus=background(color,radius); focus.setStroke(dp(2),ACCENT);
        states.addState(new int[]{android.R.attr.state_focused},focus);
        states.addState(new int[]{},background(color,radius));
        return new RippleDrawable(ColorStateList.valueOf(getColor(R.color.pressed)),states,background(SURFACE,radius));
    }
    Button button(String label, Runnable action) {
        Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(14);
        b.setTypeface(face); b.setFontVariationSettings("'wght' 650"); b.setTextColor(new ColorStateList(
            new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},new int[]{MUTED,ACCENT}));
        b.setMinHeight(dp(48)); b.setMinimumWidth(0); b.setMinWidth(0); b.setSingleLine(true);
        b.setPadding(dp(16),dp(8),dp(16),dp(8)); b.setStateListAnimator(null); b.setElevation(0);
        b.setBackground(controlBackground(SURFACE,12)); b.setOnClickListener(v->action.run()); return b;
    }
    ImageButton icon(int resource, String label, Runnable action) {
        ImageButton b=new ImageButton(this); b.setImageResource(resource); b.setContentDescription(label);
        b.setPadding(dp(13),dp(13),dp(13),dp(13)); b.setBackground(controlBackground(SURFACE,24));
        b.setOnClickListener(v->action.run()); b.setLayoutParams(new LinearLayout.LayoutParams(dp(48),dp(48))); return b;
    }
    LinearLayout row() { LinearLayout r=new LinearLayout(this); r.setGravity(Gravity.CENTER_VERTICAL); return r; }
    void fill(LinearLayout parent,View child) {parent.addView(child,new LinearLayout.LayoutParams(0,-2,1));}
    void separator(LinearLayout parent,int inset) {
        View line=new View(this); line.setBackgroundColor(RULE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1)); lp.setMargins(dp(inset),0,dp(16),0);parent.addView(line,lp);
    }
    LinearLayout surface() {
        LinearLayout v=vertical(); v.setBackground(background(SURFACE,20));return v;
    }
    void frame() {
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(PAPER);
        scroll.setVerticalScrollBarEnabled(false); screen=scroll;
        FrameLayout container=new FrameLayout(this);
        column=vertical(); column.setPadding(dp(20),dp(12),dp(20),dp(32));
        FrameLayout.LayoutParams content=new FrameLayout.LayoutParams(
            Math.min(getResources().getDisplayMetrics().widthPixels,dp(600)),-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        container.addView(column,content); scroll.addView(container);
        scroll.setOnApplyWindowInsetsListener((v,insets)->{
            v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        }); setContentView(scroll); scroll.requestApplyInsets();
    }
    void selectDate(LocalDate date) {
        selected=date; followToday=date.equals(LocalDate.now(settings.zone())); sourceExpanded=false; show();
    }
    void dayNavigation() {
        LinearLayout nav=row();
        nav.addView(icon(R.drawable.ic_previous,"Предыдущий день",()->selectDate(selected.minusDays(1))));
        if(selected.equals(LocalDate.now(settings.zone()))) {
            TextView today=text("Сегодня",14,MUTED,true);today.setGravity(Gravity.CENTER);
            fill(nav,today);
        } else {
            Button today=button("На сегодня",()->selectDate(LocalDate.now(settings.zone())));
            today.setContentDescription("Вернуться к сегодняшнему дню");
            today.setBackground(controlBackground(PAPER,12));fill(nav,today);
        }
        nav.addView(icon(R.drawable.ic_next,"Следующий день",()->selectDate(selected.plusDays(1))));
        column.addView(nav);
    }
    void dateStrip() {
        HorizontalScrollView strip=new HorizontalScrollView(this);strip.setHorizontalScrollBarEnabled(false);
        LinearLayout dates=row();LocalDate monday=ScheduleEngine.monday(selected);
        int width=Math.max(48,(Math.min(getResources().getConfiguration().screenWidthDp,600)-40)/7);
        String[] labels={"ПН","ВТ","СР","ЧТ","ПТ","СБ","ВС"};
        for(int i=0;i<7;i++) {
            LocalDate date=monday.plusDays(i);boolean chosen=date.equals(selected);
            LinearLayout cell=vertical();cell.setGravity(Gravity.CENTER);cell.setPadding(dp(2),dp(8),dp(2),dp(8));
            cell.setBackground(controlBackground(chosen?INK:PAPER,16));
            TextView label=text(labels[i],10,chosen?SURFACE:MUTED,true);label.setGravity(Gravity.CENTER);
            TextView number=text(Integer.toString(date.getDayOfMonth()),19,chosen?SURFACE:date.equals(LocalDate.now(settings.zone()))?ACCENT:INK,true);number.setGravity(Gravity.CENTER);
            cell.addView(label);cell.addView(number);cell.setMinimumHeight(dp(72));cell.setFocusable(true);cell.setClickable(true);
            cell.setContentDescription(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM",RU))+(chosen?", выбрано":""));
            cell.setSelected(chosen);cell.setOnClickListener(v->selectDate(date));
            dates.addView(cell,new LinearLayout.LayoutParams(dp(width),-2));
        }
        strip.addView(dates);column.addView(strip);
        strip.post(()->strip.scrollTo(dp(width)*Math.max(0,selected.getDayOfWeek().getValue()-4),0));
    }
    void source(ScheduleRepository.Day day) {
        boolean base="base".equals(day.source()), cache="cache".equals(day.source());
        String label=base ? "База · замены не подтверждены" : cache ? "Кэш · сохранённое расписание" : "Сайт КМТ · обновлено";
        if(day.failed()) label=base?"База · сайт недоступен":"Кэш · сайт недоступен";
        LinearLayout line=row();
        Button details=button(label,()->{sourceExpanded=!sourceExpanded;show();});
        details.setTextSize(11);details.setPadding(0,0,0,0);details.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        details.setTextColor(MUTED);details.setBackground(controlBackground(PAPER,12));
        details.setContentDescription(label+". "+(sourceExpanded?"Скрыть подробности":"Показать подробности"));
        fill(line,details);
        ImageButton refresh=icon(R.drawable.ic_refresh,"Обновить с сайта",()->refreshLive(true));
        boolean loading=ScheduleRepository.isRefreshing();refresh.setEnabled(!loading);refresh.setAlpha(loading?0.4f:1f);
        if(loading) refresh.setContentDescription("Проверяем сайт…");line.addView(refresh);column.addView(line);
        if(sourceExpanded) {
            column.addView(text(day.badge(settings.zone()),12,MUTED,false));
            column.addView(text(base?"Расписание из PDF. Замены на эту дату не подтверждены.":"Сайт указывает преподавателей и аудитории, без названий предметов.",12,MUTED,false));gap(8);
        }
    }
    void show() {
        int previousScroll=!editing && selected.equals(renderedDate) && screen!=null ? pendingScroll!=null?pendingScroll:screen.getScrollY() : 0;
        if(editing && Build.VERSION.SDK_INT>=33) getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(settingsBack);
        editing=false; frame();
        LinearLayout heading=row();
        fill(heading,text("Расписание",16,INK,true)); heading.addView(icon(R.drawable.ic_settings,"Настройки",this::configure));column.addView(heading);gap(12);
        TextView date=text(selected.format(DateTimeFormatter.ofPattern("d MMMM",RU)),32,INK,true);date.setLetterSpacing(-0.035f);column.addView(date);
        column.addView(text(selected.format(DateTimeFormatter.ofPattern("EEEE",RU))+" · ИС-232",13,MUTED,false));gap(16);
        dayNavigation();dateStrip();gap(8);
        ScheduleRepository.Day day=ScheduleRepository.day(this,selected,settings);source(day);gap(12);
        List<ScheduleEngine.Lesson> lessons=day.lessons();
        ScheduleEngine.Moment moment=ScheduleEngine.moment(lessons,LocalTime.now(settings.zone()),settings.bells());
        boolean isToday=selected.equals(LocalDate.now(settings.zone())), distance=day.remoteOnly();
        ScheduleEngine.Lesson active=moment.current()!=null ? moment.current() : moment.next();
        if(distance || lessons.isEmpty() || isToday) {
            LinearLayout summary=surface();summary.setPadding(dp(20),dp(16),dp(20),dp(16));
            String headline=distance?"Дистанционно":isToday?moment.label():"Без пар";
            summary.addView(text(headline,12,ACCENT,true));
            if(isToday && active!=null) {
                boolean ambiguous=active.week()!=null && settings.week(selected)==null;
                summary.addView(text(ambiguous?"Зависит от чётности недели":active.shortTitle(),24,INK,true));
                summary.addView(text(ScheduleEngine.room(active)+" · "+settings.bellLabel(active.slot()),13,MUTED,false));
            } else {
                summary.addView(text(distance?"Сегодня в ДО":lessons.isEmpty()?"Очных пар нет":settings.bells().isEmpty()?"Время не задано":"Пары закончились",24,INK,true));
                summary.addView(text(distance?"В колледж ехать не нужно.":lessons.isEmpty()?("base".equals(day.source())?"По базовому расписанию. Замены не подтверждены.":"По опубликованному расписанию."):settings.bells().isEmpty()?"Добавь звонки в настройках.":"Расписание дня — ниже.",13,MUTED,false));
            }
            column.addView(summary);gap(24);
        }
        if(!lessons.isEmpty()) {
            LinearLayout section=row(); fill(section,text("Пары на день",14,INK,true));
            section.addView(text(Integer.toString((int)lessons.stream().map(ScheduleEngine.Lesson::slot).distinct().count()),13,MUTED,false));column.addView(section);gap(8);
            LinearLayout agenda=surface();
            for(int i=0;i<lessons.size();i++) {
                ScheduleEngine.Lesson lesson=lessons.get(i);
                boolean current=isToday && moment.current()!=null && lesson.slot()==moment.current().slot();
                LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.TOP);item.setPadding(dp(16),dp(16),dp(16),dp(16));
                LinearLayout times=vertical();
                if(!settings.bells().isEmpty()) {
                    ScheduleEngine.Bell bell=settings.bells().get(lesson.slot()-1);
                    times.addView(text(bell.start().format(Settings.TIME),14,current?ACCENT:INK,true));
                    times.addView(text(bell.end().format(Settings.TIME),11,MUTED,false));
                } else times.addView(text(lesson.slot()+" пара",12,MUTED,true));
                item.addView(times,new LinearLayout.LayoutParams(dp(64),-2));
                LinearLayout info=vertical();
                String variant=lesson.week()!=null && settings.week(selected)==null?" · "+ScheduleEngine.weekLabel(lesson.week()).toLowerCase(RU):"";
                info.addView(text(lesson.slot()+" пара"+variant+(current?" · идёт":""),11,current?ACCENT:MUTED,true));
                info.addView(text(lesson.title(),16,INK,true));
                info.addView(text(ScheduleEngine.room(lesson),13,ACCENT,true));
                if(!lesson.teacher().isEmpty()) info.addView(text(lesson.teacher(),12,MUTED,false));
                fill(item,info);agenda.addView(item);if(i<lessons.size()-1)separator(agenda,80);
            }
            column.addView(agenda);gap(20);
        }
        column.addView(button("Добавить виджет",this::pinWidget));gap(12);
        TextView footer=text("КМТ · ИС-232 · 1 семестр 2026–2027",11,MUTED,false);footer.setGravity(Gravity.CENTER);column.addView(footer);
        renderedDate=selected;restoreScroll(previousScroll);
    }
    void restoreScroll(int y) {
        ScrollView target=screen;pendingScroll=y;
        target.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                target.getViewTreeObserver().removeOnPreDrawListener(this);target.scrollTo(0,y);
                if(target==screen) pendingScroll=null;
                return true;
            }
        });
    }
    void pinWidget() {
        AppWidgetManager manager=getSystemService(AppWidgetManager.class);
        if (manager.isRequestPinAppWidgetSupported()) manager.requestPinAppWidget(new ComponentName(this,LessonsWidget.class),null,null);
        else new AlertDialog.Builder(this).setMessage("Зажми пустое место на главном экране → Виджеты → Пары · ИС-232. Выбери виджет и перетащи на экран.").setPositiveButton("Понятно",null).show();
    }
    // API 33+ uses the platform callback; this override supports Android 8–12.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() {
        if(Build.VERSION.SDK_INT<33 && editing) show(); else super.onBackPressed();
    }
    void section(String label) {gap(20);column.addView(text(label,13,MUTED,true));gap(8);}
    void configure() {
        if(!editing && Build.VERSION.SDK_INT>=33) getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
            android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,settingsBack);
        editing=true; frame();
        LinearLayout heading=row();heading.addView(icon(R.drawable.ic_previous,"Назад к расписанию",this::show));
        TextView title=text("Настройки",26,INK,true);title.setPadding(dp(12),0,0,0);fill(heading,title);column.addView(heading);
        section("Главный экран");
        column.addView(button("Добавить виджет 2 × 2",this::pinWidget));
        if(Build.VERSION.SDK_INT>=31) {
            boolean exact=getSystemService(AlarmManager.class).canScheduleExactAlarms();
            section("Обновление виджета");
            LinearLayout updates=surface();updates.setPadding(dp(16),dp(12),dp(16),dp(12));
            updates.addView(text(exact?"Точно по звонку":"В момент звонка",16,INK,true));
            updates.addView(text(exact?"Разрешение на точные обновления включено.":"Разреши точные обновления, чтобы текущая пара переключалась вовремя.",13,MUTED,false));
            updates.addView(button(exact?"Настроить разрешение":"Разрешить обновления",()->{
                try {startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}
                catch(ActivityNotFoundException e) {new AlertDialog.Builder(this).setMessage("Найди приложение «Пары · ИС-232» в настройках Android → Специальный доступ → Будильники и напоминания.").setPositiveButton("Понятно",null).show();}
            }));column.addView(updates);
        }
        section("Часовой пояс");
        LinearLayout timezone=surface();timezone.setPadding(dp(16),dp(12),dp(16),dp(12));
        LinearLayout zoneRow=row();fill(zoneRow,text("Как на телефоне",15,INK,true));
        Switch zone=new Switch(this);zone.setChecked(settings.deviceZone());zone.setMinHeight(dp(48));zone.setContentDescription("Часовой пояс телефона");
        GradientDrawable thumb=background(SURFACE,24);thumb.setSize(dp(24),dp(24));thumb.setStroke(dp(1),RULE);zone.setThumbDrawable(thumb);
        GradientDrawable track=background(RULE,24);track.setSize(dp(48),dp(28));zone.setTrackDrawable(track);
        zone.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{ACCENT,RULE}));
        zone.setSwitchMinWidth(dp(52));zoneRow.addView(zone,new LinearLayout.LayoutParams(dp(56),dp(48)));timezone.addView(zoneRow);
        timezone.addView(text("По умолчанию — Новосибирск, UTC+7.",12,MUTED,false));column.addView(timezone);zoneField=zone;
        section("Расписание звонков");
        LinearLayout bellsGroup=surface();bellsGroup.setPadding(dp(16),dp(12),dp(16),dp(12));
        LinearLayout labels=row();labels.addView(text("Пара",11,MUTED,false),new LinearLayout.LayoutParams(dp(44),-2));
        fill(labels,text("Начало",11,MUTED,false));fill(labels,text("Конец",11,MUTED,false));bellsGroup.addView(labels);
        EditText[][] fields=new EditText[5][2];bellFields=fields;
        for(int i=0;i<5;i++) {
            LinearLayout times=row();times.setPadding(0,dp(6),0,dp(6));
            times.addView(text(Integer.toString(i+1),16,INK,true),new LinearLayout.LayoutParams(dp(44),-2));
            for(int j=0;j<2;j++) {
                EditText input=new EditText(this);input.setSingleLine();input.setTextSize(17);input.setTypeface(face);input.setFontVariationSettings("'wght' 450");
                input.setFontFeatureSettings("tnum");input.setTextColor(INK);input.setHintTextColor(MUTED);
                input.setText(settings.time(i+1,j==1));input.setHint(j==0?"08:30":"10:00");input.setSelectAllOnFocus(true);
                input.setPadding(dp(8),dp(8),dp(8),dp(8));input.setGravity(Gravity.CENTER);input.setMinimumWidth(0);
                StateListDrawable states=new StateListDrawable();
                GradientDrawable normal=background(PAPER,10);normal.setStroke(dp(1),RULE);
                GradientDrawable focused=background(PAPER,10);focused.setStroke(dp(1),ACCENT);
                states.addState(new int[]{android.R.attr.state_focused},focused);states.addState(new int[]{},normal);input.setBackground(states);
                input.setContentDescription((i+1)+" пара, "+(j==0?"начало":"конец"));
                input.setInputType(InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_TIME);
                input.setImeOptions(i==4 && j==1?android.view.inputmethod.EditorInfo.IME_ACTION_DONE:android.view.inputmethod.EditorInfo.IME_ACTION_NEXT);
                fields[i][j]=input;LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);
                if(j==0)lp.setMarginEnd(dp(8));times.addView(input,lp);
            }
            bellsGroup.addView(times);
        }
        column.addView(bellsGroup);gap(8);
        column.addView(text("Формат ЧЧ:ММ. Чтобы видеть только номера пар, очисти все поля.",12,MUTED,false));
        TextView error=text("",13,ERROR,false);error.setMinHeight(dp(44));
        error.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);column.addView(error);
        Button save=button("Сохранить",()->{
            List<ScheduleEngine.Bell> bells=new ArrayList<>();boolean any=false;
            for(EditText[] pair:fields)for(EditText input:pair)if(!input.getText().toString().trim().isEmpty())any=true;
            if(any) {
                try {
                    for(EditText[] pair:fields) {
                        String start=pair[0].getText().toString().trim(),end=pair[1].getText().toString().trim();
                        if(!start.matches("([01]\\d|2[0-3]):[0-5]\\d") || !end.matches("([01]\\d|2[0-3]):[0-5]\\d"))throw new IllegalArgumentException();
                        bells.add(new ScheduleEngine.Bell(LocalTime.parse(start),LocalTime.parse(end)));
                    }
                    if(!ScheduleEngine.validBells(bells))throw new IllegalArgumentException();
                }catch(RuntimeException e) {
                    error.setText("Проверь все 5 пар: ЧЧ:ММ, начало раньше конца, пары не пересекаются.");
                    error.requestRectangleOnScreen(new android.graphics.Rect(0,0,error.getWidth(),error.getHeight()),false);return;
                }
            }
            settings.save(null,zone.isChecked(),bells);LessonsWidget.updateAll(this);
            android.view.inputmethod.InputMethodManager keyboard=getSystemService(android.view.inputmethod.InputMethodManager.class);
            keyboard.hideSoftInputFromWindow(screen.getWindowToken(),0);
            selected=LocalDate.now(settings.zone());followToday=true;show();
        });
        save.setBackground(controlBackground(ACCENT,14));save.setTextColor(SURFACE);column.addView(save);gap(8);
        Button cancel=button("Отмена",this::show);cancel.setBackground(controlBackground(PAPER,14));column.addView(cancel);
    }
}
