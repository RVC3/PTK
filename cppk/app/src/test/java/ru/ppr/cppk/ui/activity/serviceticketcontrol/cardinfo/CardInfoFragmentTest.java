package ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CardInfoFragmentTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void one(){
        System.out.println(test.substring(0,4));
        System.out.println("тип карты " + test.substring(4,6));

        System.out.println("region " + test.substring(6,8));
        int day = Integer.parseInt(test.substring(8,10));
        System.out.println("mf " + (day > 50 ? "m":"f"));
        if(day > 50)
            day -= 50;
        System.out.println(day + "." + test.substring(10,12) + "." + test.substring(12,14));
        System.out.println("CHECK " + Check(test));
    }

    public static boolean Check(String ccNumber)
    {
        if(!ccNumber.matches("[0-9]+"))
            return false;
        if(ccNumber.length() < 15)
            return false;

        int sum = 0;
        boolean alternate = false;
        for (int i = ccNumber.length() - 1; i >= 0; i--)
        {
            int n = Integer.parseInt(ccNumber.substring(i, i + 1));
            if (alternate)
            {
                n *= 2;
                if (n > 9)
                {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    String test = "9643909021085452825";
}