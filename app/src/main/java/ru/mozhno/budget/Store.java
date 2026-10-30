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
 LocalDate income(){return LocalDate.parse(data.optString("income",LocalDate.now().plusDays(30).toString()));}
 JSONArray rows(String key){return data.optJSONArray(key)==null?new JSONArray():data.optJSONArray(key);}
 void put(String k,Object v){try{data.put(k,v);}catch(JSONException e){throw new IllegalStateException(e);}}
 void save(){if(!context.getSharedPreferences("budget",0).edit().putString("data",data.toString()).commit())throw new IllegalStateException("Не удалось сохранить данные");BudgetWidget.refresh(context);}
 long bills(){long sum=0;JSONArray a=rows("bills");for(int i=0;i<a.length();i++){JSONObject b=a.optJSONObject(i);if(!LocalDate.parse(b.optString("date")).isAfter(income()))sum+=b.optLong("amount");}return sum;}
 long free(){return BudgetMath.free(value("balance"),value("reserve"),bills());}
 long daily(){return BudgetMath.daily(free(),LocalDate.now(),income());}
 String next(){JSONObject first=null;JSONArray a=rows("bills");for(int i=0;i<a.length();i++){JSONObject b=a.optJSONObject(i);if(first==null||b.optString("date").compareTo(first.optString("date"))<0)first=b;}return first==null?"Предстоящих платежей нет":first.optString("date")+" · "+first.optString("title")+" · "+money(first.optLong("amount"));}
 static String money(long n){NumberFormat f=NumberFormat.getNumberInstance(new Locale("ru","RU"));f.setMaximumFractionDigits(2);return f.format(java.math.BigDecimal.valueOf(n,2))+" ₽";}
 static JSONObject entry(String title,long amount,LocalDate date){JSONObject o=new JSONObject();try{o.put("title",title);o.put("amount",amount);o.put("date",date.toString());}catch(JSONException e){throw new IllegalStateException(e);}return o;}
 void expense(String title,long amount){put("balance",value("balance")-amount);JSONArray a=rows("expenses");a.put(entry(title,amount,LocalDate.now()));put("expenses",a);}
}
