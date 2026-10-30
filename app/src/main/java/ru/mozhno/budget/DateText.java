package ru.mozhno.budget;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public final class DateText {
 private static final DateTimeFormatter FORMAT=DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);
 public static String display(LocalDate date){return FORMAT.format(date);}
 public static LocalDate parse(String value){return LocalDate.parse(value,FORMAT);}
 public static String mask(String value){
  String digits=value.replaceAll("[^0-9]","");
  if(digits.length()>8)digits=digits.substring(0,8);
  StringBuilder out=new StringBuilder();
  for(int i=0;i<digits.length();i++){if(i==2||i==4)out.append('-');out.append(digits.charAt(i));}
  return out.toString();
 }
}
