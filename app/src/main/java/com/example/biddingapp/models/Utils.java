package com.example.biddingapp.models;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class Utils {

    public static final String DB_PROFILE = "profiles";
    public static final String DB_AUCTION = "auction";
    public static final String DB_TRANSACTION = "transaction";

    public static Double parseMoney(String money){
        Double fmoney;
        money = money.replace("$", "");
        try{
            fmoney = Double.parseDouble(money);
            if(fmoney < 1) throw new NumberFormatException();
        }catch (NumberFormatException exc){
            return null;
        }
        return fmoney;
    }

    /**
     * @param date
     * @return
     */
    public static String getPrettyTime(Date date) {
        PrettyTime time = new PrettyTime();
        return time.format(date);
    }

}
