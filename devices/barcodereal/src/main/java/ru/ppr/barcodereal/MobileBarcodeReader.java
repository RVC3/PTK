package ru.ppr.barcodereal;


import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.ppr.barcode.file.BarcodeReaderFile;
import ru.ppr.logger.Logger;

public class MobileBarcodeReader {
    private static final MobileBarcodeReader ourInstance = new MobileBarcodeReader();
    private boolean isLastMobileCode;

    public String getLastCode() {
        return lastCode;
    }

    private String lastCode="200067500405000011533070530232320879438754802371";
    private static final String alphabet = "stm7ng1rf0cp96wzhae2vq485lyd3xkjbiuo";
    private static final String TAG = Logger.makeLogTag(BarcodeReaderFile.class);
    public Map<String, List<Long>> used_keys;


    public static MobileBarcodeReader getInstance() {
        return ourInstance;
    }

    private MobileBarcodeReader() {
        isLastMobileCode = false;



        used_keys = new LinkedHashMap<>();
    }

    public void setLastMobileCode() {
        isLastMobileCode = true;
    }

    public boolean getIfLastCodeMobile(){
        boolean b = isLastMobileCode;
        isLastMobileCode = false;
        return b;
    }


    public boolean getIfLastCodeMobileWithoutStatusClean(){
        return isLastMobileCode;
    }

    public boolean checkMobileAuthorizations(){
        boolean b = isLastMobileCode;
        isLastMobileCode = false;
        return b;
    }




    // распутывание
    private String decrypt(String input, int key) {
        StringBuilder output=new StringBuilder("");
        for(int i =0; i< input.length(); i++) {
            char c = input.charAt(i);
            int position = (alphabet.indexOf(c) - (key % alphabet.length())
                    + alphabet.length()) % alphabet.length();
            output.append(alphabet.charAt(position));
        }
        return output.toString();
    }

    public static class Validation{
        public String cache;
        public Long unix_time;

        public Validation(String c, Long time){
            cache = c;
            unix_time = time;
        }
    }

    public void setLastCode(String lastCode) {
        StringBuilder sb = new StringBuilder(lastCode);
        sb.deleteCharAt(10);
        sb.deleteCharAt(20);
        sb.deleteCharAt(30);
        sb.deleteCharAt(40);
        lastCode = sb.toString();
        this.lastCode = lastCode;
        Logger.info(TAG, "Code before decode:" + lastCode);
        String decoded;
/*
        try {

            File f = new File("/data/data/ru.ppr.cppk/files/common_settings/qr_checks");
            Logger.info(TAG, "add data to " + f.getAbsolutePath() );

//            Gson gson  = new Gson();

            FileUtils.writeLines(f,"UTF-8", Arrays.asList(lastCode + " " + new Date().getTime()),true);

        } catch (IOException e) {
            Logger.error(TAG, "IOException" + e.getLocalizedMessage());
        }*/

        // проверяем, что ключ запутан
        if(lastCode.matches(".*[a-zA-Z]+.*")) {

            //пробуем через текущую дату
            int dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) % 10 ;
            Logger.info(TAG, "dayof month:" + dayOfMonth);
            decoded = decrypt(lastCode, dayOfMonth);
            if (!decoded.matches(".*[a-zA-Z]+.*")) {
                Logger.info(TAG, "decoded на текущую дату:" + decoded);
                this.lastCode = decoded;
                return;
            }

            // проверяем есть ли ранее активированные билеты по всем датам
            LinkedHashMap<Integer, String> decoded_map = new LinkedHashMap<>();
            //рапутывание
            for (int i = 0; i < 10; i++) {
                String s = decrypt(lastCode, i);
                Logger.info(TAG, "i=" +i +" s=" +s);
                if (!s.matches(".*[a-zA-Z]+.*")) {
                    decoded_map.put(i, s);
                    this.lastCode = s;
                }
            }
            Logger.info(TAG, "На текущую дату ключа нет, найдены ключи: " + decoded_map.size());
            for (int i : decoded_map.keySet()) {
                Logger.info(TAG, "ключ[" + i + "]" + decoded_map.get(i));
            }
        }

        Logger.info(TAG, "last Code:" + this.lastCode);
        if(this.lastCode.matches(".*[a-zA-Z]+.*"))
            this.lastCode = "000000000000000000000000000000000000000000000000";

        List<Long> list = used_keys.get(this.lastCode);
        if(list == null) {
            list = new LinkedList<>();
            used_keys.put(this.lastCode, list);
        }
        list.add(new Date().getTime());
    }

    public Long getLastStationCode(){
        String s = lastCode.substring(0,7);
        Logger.debug(TAG, "getLastStationCode: String is " + s);
        if(s.isEmpty())
            s="000000";
        long l = Long.valueOf(s);
        Logger.info(TAG, "Station Code:" + s + "  " + l);
        return l;
    }

    public String getLastTime() {
        String s = lastCode.substring(16,10+16);
        if(s.isEmpty())
            s="0";
        Logger.info(TAG," Time is: "+s);

        long timeStamp = Long.parseLong(s);

        Date time=new java.util.Date((long)timeStamp*1000);

        DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM");

        return dateFormat.format(time);
    }

    public String getUniqueKey(String sequence){
        return sequence.substring(27);
    }
}
