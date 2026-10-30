package ru.mozhno.budget;
import android.content.Context;
import org.json.*;
import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.Locale;
final class Store {
 final Context context; JSONObject data;
 Store(Context c){context=c;try{data=new JSONObject(c.getSharedPreferences("budget",0).getString("data","{}"));}catch(JSONException e){throw new IllegalStateException("Не удалось прочитать бюджет",e);}}
 long value(String key){return data.optLong(key,0);}
 boolean configured(){return data.has("income");}
 LocalDate fallbackIncome(){return LocalDate.parse(data.optString("income",LocalDate.now().plusDays(30).toString()));}
 LocalDate income(){
  LocalDate result=null;
  JSONArray planned=rows("planned_incomes");
  for(int i=0;i<planned.length();i++){
   LocalDate date=LocalDate.parse(planned.optJSONObject(i).optString("date"));
   if(result==null||date.isBefore(result))result=date;
  }
  return result==null?fallbackIncome():result;
 }
 JSONArray rows(String key){return data.optJSONArray(key)==null?new JSONArray():data.optJSONArray(key);}
 void put(String k,Object v){try{data.put(k,v);}catch(JSONException e){throw new IllegalStateException(e);}}
 void save(){if(!context.getSharedPreferences("budget",0).edit().putString("data",data.toString()).commit())throw new IllegalStateException("Не удалось сохранить данные");BudgetWidget.refresh(context);}
 long bills(){long sum=0;JSONArray a=rows("bills");for(int i=0;i<a.length();i++){JSONObject b=a.optJSONObject(i);if(!LocalDate.parse(b.optString("date")).isAfter(income()))sum+=b.optLong("amount");}return sum;}
 long free(){return BudgetMath.free(value("balance"),value("reserve"),bills());}
 long daily(){return BudgetMath.daily(free(),LocalDate.now(),income());}
 String next(){JSONObject first=null;JSONArray a=rows("bills");for(int i=0;i<a.length();i++){JSONObject b=a.optJSONObject(i);if(first==null||b.optString("date").compareTo(first.optString("date"))<0)first=b;}return first==null?"Предстоящих платежей нет":DateText.display(LocalDate.parse(first.optString("date")))+" · "+first.optString("title")+" · "+money(first.optLong("amount"));}
 static String money(long n){NumberFormat f=NumberFormat.getNumberInstance(new Locale("ru","RU"));f.setMaximumFractionDigits(2);return f.format(java.math.BigDecimal.valueOf(n,2))+" ₽";}
 static JSONObject entry(String title,long amount,LocalDate date){return entry(title,amount,date,false);}
 static JSONObject entry(String title,long amount,LocalDate date,boolean recurring){JSONObject o=new JSONObject();try{o.put("title",title);o.put("amount",amount);o.put("date",date.toString());o.put("recurring",recurring);if(recurring)o.put("day",date.getDayOfMonth());}catch(JSONException e){throw new IllegalStateException(e);}return o;}
 static boolean recurring(JSONObject row){return row.optBoolean("recurring",false);}
 static LocalDate nextDate(JSONObject row){LocalDate current=LocalDate.parse(row.optString("date"));return BudgetMath.nextMonthly(current,row.optInt("day",current.getDayOfMonth()));}
 static void advance(JSONObject row){try{row.put("date",nextDate(row).toString());}catch(JSONException e){throw new IllegalStateException(e);}}
 void expense(String title,long amount){put("balance",value("balance")-amount);JSONArray a=rows("expenses");a.put(entry(title,amount,LocalDate.now()));put("expenses",a);}
}
