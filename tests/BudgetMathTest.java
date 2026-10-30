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
 for(String invalid:new String[]{"-1","1.001","NaN","","1000000000000000"}){boolean failed=false;try{BudgetMath.parse(invalid);}catch(Exception e){failed=true;}if(!failed)throw new AssertionError(invalid);checks++;}
 System.out.println("Passed "+checks+" budget checks");}
}
