package ru.mozhno.budget;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
public final class BudgetMath {
 public static long parse(String s) { long n=new BigDecimal(s.trim().replace(',','.')).setScale(2,RoundingMode.UNNECESSARY).movePointRight(2).longValueExact(); if(n<0 || n>100000000000000L) throw new IllegalArgumentException(); return n; }
 public static long days(LocalDate now,LocalDate income){return Math.max(1,ChronoUnit.DAYS.between(now,income));}
 public static long free(long balance,long reserve,long bills){return balance-reserve-bills;}
 public static long daily(long free,LocalDate now,LocalDate income){return Math.max(0,free)/days(now,income);}
 public static LocalDate nextMonthly(LocalDate current,int day){
  YearMonth next=YearMonth.from(current).plusMonths(1);
  return next.atDay(Math.min(Math.max(day,1),next.lengthOfMonth()));
 }
}
