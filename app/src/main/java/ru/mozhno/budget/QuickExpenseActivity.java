package ru.mozhno.budget;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.*;

public class QuickExpenseActivity extends Activity {
 final int green=Color.rgb(24,92,75);
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}

 TextView text(LinearLayout parent,String value,int size){
  TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(Color.DKGRAY);view.setPadding(0,dp(6),0,dp(6));parent.addView(view);return view;
 }

 public void onCreate(Bundle state){
  super.onCreate(state);
  setTitle("Быстрый расход");
  LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(24),dp(14),dp(24),dp(14));setContentView(page);
  Store store=new Store(this);
  if(!store.configured()){
   text(page,"Сначала настройте общий бюджет в приложении.",17);
   Button setup=new Button(this);setup.setText("Открыть настройку");setup.setTextColor(green);page.addView(setup);
   setup.setOnClickListener(v->{startActivity(new Intent(this,MainActivity.class));finish();});return;
  }

  text(page,"Введите сумму. Описание можно оставить пустым — расход сохранится как «Прочее».",15);
  EditText amount=new EditText(this);amount.setHint("Сумма, ₽");amount.setSingleLine();amount.setTextSize(24);amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);page.addView(amount,new LinearLayout.LayoutParams(-1,-2));
  EditText title=new EditText(this);title.setHint("Описание или категория");title.setSingleLine();page.addView(title,new LinearLayout.LayoutParams(-1,-2));

  LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);page.addView(actions,new LinearLayout.LayoutParams(-1,-2));
  Button cancel=new Button(this);cancel.setText("Отмена");cancel.setTextColor(Color.DKGRAY);actions.addView(cancel,new LinearLayout.LayoutParams(0,-2,1));
  Button save=new Button(this);save.setText("Добавить");save.setTextColor(green);actions.addView(save,new LinearLayout.LayoutParams(0,-2,1));
  cancel.setOnClickListener(v->finish());
  save.setOnClickListener(v->{
   try{
    long value=BudgetMath.parse(amount.getText().toString());if(value==0)throw new IllegalArgumentException();
    String description=title.getText().toString().trim();if(description.isEmpty())description="Прочее";
    store.expense(description,value);store.save();Toast.makeText(this,"Расход добавлен",Toast.LENGTH_SHORT).show();finish();
   }catch(Exception e){amount.setError("Введите сумму больше нуля, не более двух знаков после запятой");amount.requestFocus();}
  });
  amount.requestFocus();getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
 }
}
