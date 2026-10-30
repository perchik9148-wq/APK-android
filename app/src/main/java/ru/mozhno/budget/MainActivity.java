package ru.mozhno.budget;

import android.app.*;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.widget.*;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import org.json.*;
import java.time.LocalDate;

public class MainActivity extends Activity {
 Store s;
 LinearLayout page;
 final int green=Color.rgb(24,92,75);

 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(green);getWindow().setNavigationBarColor(green);show();handle(getIntent());}
 protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);show();handle(i);}
 void handle(Intent i){if(i.getBooleanExtra("expense",false)){i.removeExtra("expense");if(s.configured())expense();else settings();}}
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
 static String formatDate(LocalDate date){return DateText.display(date);}
 static LocalDate parseDate(EditText field){return DateText.parse(field.getText().toString());}

 TextView text(LinearLayout p,String t,int size,int color){
  TextView v=new TextView(this);v.setText(t);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,dp(7),0,dp(7));p.addView(v);return v;
 }
 void button(LinearLayout p,String t,Runnable r){
  Button b=new Button(this);b.setText(t);b.setTextColor(green);p.addView(b,new LinearLayout.LayoutParams(-1,-2));b.setOnClickListener(v->r.run());
 }

 void show(){
  s=new Store(this);
  ScrollView scroll=new ScrollView(this);
  scroll.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
  page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(22),dp(28),dp(22),dp(32));page.setBackgroundColor(Color.rgb(246,247,241));scroll.addView(page);setContentView(scroll);
  text(page,"МОЙ ОБЩИЙ БЮДЖЕТ",12,green);text(page,"Можно потратить",30,green);
  if(!s.configured()){
   text(page,"Знайте, сколько свободно сегодня. Запланированные платежи и резерв уже учтены.",18,Color.DKGRAY);
   button(page,"Настроить бюджет",()->settings());return;
  }

  LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(18),dp(12),dp(18),dp(14));card.setBackgroundColor(green);page.addView(card);
  text(card,"СЕГОДНЯ",12,Color.WHITE);text(card,Store.money(s.daily()),36,Color.WHITE);
  text(card,"Свободно до дохода: "+Store.money(s.free()),16,Color.WHITE);
  text(card,"Ближайший доход: "+formatDate(s.income())+" · дней: "+BudgetMath.days(LocalDate.now(),s.income()),13,Color.WHITE);
  if(!s.income().isAfter(LocalDate.now()))text(page,"Дата дохода наступила. Подтвердите получение или обновите дату в настройках.",15,Color.RED);
  if(s.free()<0)text(page,"На обязательства и резерв не хватает "+Store.money(-s.free()),16,Color.RED);
  text(page,"Баланс: "+Store.money(s.value("balance"))+"\nРезерв: "+Store.money(s.value("reserve"))+"\nПлатежи до дохода: "+Store.money(s.bills()),16,Color.DKGRAY);

  button(page,"＋ Расход",()->expense());
  button(page,"＋ Пополнить сейчас",()->incomeNow());
  button(page,"＋ Запланировать доход",()->plannedIncome());
  button(page,"＋ Предстоящий платёж",()->bill());
  button(page,"Настройки бюджета",()->settings());

  showPlannedIncomes();
  showBills();
  showExpenses();
  text(page,"Для ежемесячной записи подтвердите оплату или получение — приложение перенесёт её на тот же день следующего месяца. Если такого дня нет, будет выбран последний день месяца.",13,Color.GRAY);
  text(page,"Виджет: удерживайте свободное место на главном экране → Виджеты → Можно потратить. Обновление после изменений и периодически; Android может задерживать его в режиме экономии энергии.",13,Color.GRAY);
 }

 void showPlannedIncomes(){
  text(page,"Запланированные доходы",22,green);
  JSONArray rows=s.rows("planned_incomes");
  if(rows.length()==0)text(page,"Пока нет. Можно один раз настроить зарплату или другое ежемесячное пополнение.",15,Color.GRAY);
  for(int i=0;i<rows.length();i++){
   final int index=i;JSONObject row=rows.optJSONObject(i);boolean monthly=Store.recurring(row);
   LocalDate date=LocalDate.parse(row.optString("date"));
   text(page,row.optString("title")+" · +"+Store.money(row.optLong("amount"))+"\n"+formatDate(date)+(monthly?" · ежемесячно":""),17,Color.DKGRAY);
   String receive=monthly?"Получено — перенести на "+formatDate(Store.nextDate(row)):"Получено — добавить к балансу";
   button(page,"Получено / удалить",()->new AlertDialog.Builder(this).setTitle(row.optString("title")).setItems(new String[]{receive,"Удалить без пополнения"},(d,n)->{
    JSONArray a=s.rows("planned_incomes");
    if(n==0){s.put("balance",s.value("balance")+row.optLong("amount"));if(monthly)Store.advance(row);else a.remove(index);}
    else a.remove(index);
    s.put("planned_incomes",a);save();
   }).show());
  }
 }

 void showBills(){
  text(page,"Предстоящие платежи",22,green);
  JSONArray rows=s.rows("bills");
  if(rows.length()==0)text(page,"Пока нет. Добавьте аренду, связь или подписку.",15,Color.GRAY);
  for(int i=0;i<rows.length();i++){
   final int index=i;JSONObject row=rows.optJSONObject(i);boolean monthly=Store.recurring(row);
   LocalDate date=LocalDate.parse(row.optString("date"));
   text(page,row.optString("title")+" · "+Store.money(row.optLong("amount"))+"\n"+formatDate(date)+(monthly?" · ежемесячно":""),17,Color.DKGRAY);
   String pay=monthly?"Оплатить и перенести на "+formatDate(Store.nextDate(row)):"Оплатить — списать с баланса";
   button(page,"Оплатить / удалить",()->new AlertDialog.Builder(this).setTitle(row.optString("title")).setItems(new String[]{pay,"Удалить без списания"},(d,n)->{
    JSONArray a=s.rows("bills");
    if(n==0){s.expense(row.optString("title"),row.optLong("amount"));if(monthly)Store.advance(row);else a.remove(index);}
    else a.remove(index);
    s.put("bills",a);save();
   }).show());
  }
 }

 void showExpenses(){
  text(page,"История расходов",22,green);JSONArray rows=s.rows("expenses");
  if(rows.length()==0)text(page,"Ваши расходы появятся здесь",15,Color.GRAY);
  for(int i=rows.length()-1;i>=Math.max(0,rows.length()-50);i--){
   final int index=i;JSONObject row=rows.optJSONObject(i);LocalDate date=LocalDate.parse(row.optString("date"));
   TextView view=text(page,row.optString("title")+"   −"+Store.money(row.optLong("amount"))+"\n"+formatDate(date),16,Color.DKGRAY);
   view.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("Отменить расход и вернуть сумму на баланс? Отменённый платёж нужно запланировать заново.").setPositiveButton("Отменить расход",(d,w)->{s.put("balance",s.value("balance")+row.optLong("amount"));JSONArray a=s.rows("expenses");a.remove(index);s.put("expenses",a);save();}).setNegativeButton("Назад",null).show());
  }
 }

 void save(){try{s.save();show();}catch(Exception e){Toast.makeText(this,"Не удалось сохранить данные",Toast.LENGTH_LONG).show();}}
 LinearLayout form(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(24),dp(8),dp(24),0);return l;}
 EditText field(LinearLayout l,String hint,String value,boolean number){EditText e=new EditText(this);e.setHint(hint);e.setText(value);e.setSingleLine();if(number)e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);l.addView(e);return e;}
 EditText dateField(LinearLayout l,LocalDate value){
  EditText e=field(l,"ДД-ММ-ГГГГ",formatDate(value),false);e.setInputType(InputType.TYPE_CLASS_NUMBER);
  e.addTextChangedListener(new TextWatcher(){boolean changing;public void beforeTextChanged(CharSequence x,int a,int b,int c){}public void onTextChanged(CharSequence x,int a,int b,int c){}public void afterTextChanged(Editable editable){
   if(changing)return;String out=DateText.mask(editable.toString());
   if(!out.equals(editable.toString())){changing=true;e.setText(out);e.setSelection(out.length());changing=false;}
  }});return e;
 }
 CheckBox monthly(LinearLayout l){CheckBox c=new CheckBox(this);c.setText("Повторять каждый месяц");c.setTextColor(Color.DKGRAY);c.setPadding(0,dp(8),0,dp(8));l.addView(c);return c;}
 void dialog(String title,LinearLayout l,Runnable action){AlertDialog d=new AlertDialog.Builder(this).setTitle(title).setView(l).setPositiveButton("Сохранить",null).setNegativeButton("Отмена",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{try{action.run();d.dismiss();}catch(Exception e){Toast.makeText(this,"Проверьте сумму, название и дату ДД-ММ-ГГГГ",Toast.LENGTH_LONG).show();}}));d.show();}
 long amount(EditText e){long n=BudgetMath.parse(e.getText().toString());if(n==0)throw new IllegalArgumentException();return n;}
 String title(EditText e){String t=e.getText().toString().trim();if(t.isEmpty())throw new IllegalArgumentException();return t;}

 void settings(){
  LinearLayout l=form();text(l,"Укажите фактический остаток денег на всех счетах и наличными. Резерв не расходуется. Если есть запланированные доходы, ближайший из них задаёт конец расчётного периода.",14,Color.DKGRAY);
  EditText balance=field(l,"Текущий баланс, ₽",s.configured()?java.math.BigDecimal.valueOf(s.value("balance"),2).toPlainString():"",true);
  EditText reserve=field(l,"Резерв, ₽",java.math.BigDecimal.valueOf(s.value("reserve"),2).toPlainString(),true);
  EditText date=dateField(l,s.fallbackIncome());
  dialog("Настройка бюджета",l,()->{long b=BudgetMath.parse(balance.getText().toString()),r=BudgetMath.parse(reserve.getText().toString());LocalDate day=parseDate(date);if(!day.isAfter(LocalDate.now()))throw new IllegalArgumentException();s.put("balance",b);s.put("reserve",r);s.put("income",day.toString());save();});
 }
 void expense(){LinearLayout l=form();EditText a=field(l,"Сумма, ₽","",true),t=field(l,"Категория / описание","",false);dialog("Новый расход",l,()->{long n=amount(a);String name=title(t);s.expense(name,n);save();});}
 void incomeNow(){LinearLayout l=form();EditText a=field(l,"Сумма дохода, ₽","",true);dialog("Пополнить общий бюджет",l,()->{s.put("balance",s.value("balance")+amount(a));save();});}
 void plannedIncome(){
  LinearLayout l=form();EditText t=field(l,"Название дохода","Зарплата",false),a=field(l,"Сумма, ₽","",true),date=dateField(l,LocalDate.now().plusDays(1));CheckBox recurring=monthly(l);
  dialog("Запланировать доход",l,()->{String name=title(t);long n=amount(a);LocalDate day=parseDate(date);JSONArray rows=s.rows("planned_incomes");rows.put(Store.entry(name,n,day,recurring.isChecked()));s.put("planned_incomes",rows);save();});
 }
 void bill(){
  LinearLayout l=form();EditText t=field(l,"Название платежа","",false),a=field(l,"Сумма, ₽","",true),date=dateField(l,LocalDate.now().plusDays(1));CheckBox recurring=monthly(l);
  dialog("Предстоящий платёж",l,()->{String name=title(t);long n=amount(a);LocalDate day=parseDate(date);JSONArray rows=s.rows("bills");rows.put(Store.entry(name,n,day,recurring.isChecked()));s.put("bills",rows);save();});
 }
}
