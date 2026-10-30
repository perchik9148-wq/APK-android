package ru.mozhno.budget;
import java.time.LocalDate;
public class BudgetMathTest {
 static int checks;
 static void eq(long a,long b){checks++;if(a!=b)throw new AssertionError(a+" != "+b);}
 public static void main(String[] args){LocalDate today=LocalDate.of(2026,10,29),income=today.plusDays(10);
 eq(BudgetMath.parse("12,34"),1234);eq(BudgetMath.parse("0.01"),1);
 eq(BudgetMath.free(1000000,100000,200000),700000);eq(BudgetMath.daily(700000,today,income),70000);
 // Paying an already reserved bill decreases balance and liabilities equally.
 eq(BudgetMath.free(1000000-200000,100000,0),700000);
 eq(BudgetMath.daily(-100,today,income),0);eq(BudgetMath.days(today,today),1);eq(BudgetMath.days(today,today.minusDays(1)),1);
 eq(BudgetMath.daily(100,today,today.plusDays(3)),33);
 if(!BudgetMath.nextMonthly(LocalDate.of(2026,1,31),31).equals(LocalDate.of(2026,2,28)))throw new AssertionError("January 31 recurrence");checks++;
 if(!BudgetMath.nextMonthly(LocalDate.of(2026,2,28),31).equals(LocalDate.of(2026,3,31)))throw new AssertionError("Preserve day 31");checks++;
 if(!BudgetMath.nextMonthly(LocalDate.of(2027,1,29),29).equals(LocalDate.of(2027,2,28)))throw new AssertionError("Short February");checks++;
 if(!DateText.mask("05012027").equals("05-01-2027"))throw new AssertionError("Date mask");checks++;
 if(!DateText.mask("05-01-2027").equals("05-01-2027"))throw new AssertionError("Existing separators");checks++;
 if(!DateText.parse("29-02-2028").equals(LocalDate.of(2028,2,29)))throw new AssertionError("Date parse");checks++;
 boolean invalidDate=false;try{DateText.parse("31-02-2027");}catch(Exception e){invalidDate=true;}if(!invalidDate)throw new AssertionError("Strict date validation");checks++;
 for(String invalid:new String[]{"-1","1.001","NaN","","1000000000000000"}){boolean failed=false;try{BudgetMath.parse(invalid);}catch(Exception e){failed=true;}if(!failed)throw new AssertionError(invalid);checks++;}
 System.out.println("Passed "+checks+" budget checks");}
}
