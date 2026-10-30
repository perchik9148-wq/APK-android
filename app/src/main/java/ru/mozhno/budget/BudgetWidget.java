package ru.mozhno.budget;
import android.app.*;import android.appwidget.*;import android.content.*;import android.widget.RemoteViews;
public class BudgetWidget extends AppWidgetProvider {
 public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,m,id);}
 public void onReceive(Context c,Intent i){super.onReceive(c,i);if(!AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(i.getAction()))refresh(c);}
 static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);for(int id:m.getAppWidgetIds(new ComponentName(c,BudgetWidget.class)))update(c,m,id);}
 static void update(Context c,AppWidgetManager m,int id){Store s=new Store(c);RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget);v.setTextViewText(R.id.widget_amount,s.configured()?Store.money(s.daily()):"Настройте бюджет");v.setTextViewText(R.id.widget_detail,s.configured()?"Свободно: "+Store.money(s.free())+"\n"+s.next():"Нажмите, чтобы начать");Intent open=new Intent(c,MainActivity.class);v.setOnClickPendingIntent(R.id.widget_root,PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT));Intent add=new Intent(c,MainActivity.class).putExtra("expense",true);v.setOnClickPendingIntent(R.id.widget_add,PendingIntent.getActivity(c,1,add,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT));m.updateAppWidget(id,v);}
}
