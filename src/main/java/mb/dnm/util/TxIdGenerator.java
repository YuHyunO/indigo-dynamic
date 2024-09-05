package mb.dnm.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TxIdGenerator {
    public static String generateTxId(String interfaceId, Date startTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(interfaceId);
        String initTime = new SimpleDateFormat("yyMMddHHmmssSSS", Locale.KOREA).format(startTime);
        int randomNum = 0;
        for(int i=3;i <= initTime.length();i++) {
            randomNum = randomNum + Integer.parseInt(initTime.substring(i-3,i));
        }
        randomNum = randomNum%1000;
        if(randomNum < 100) {
            randomNum += 100;
        }
        return sb.append(initTime)
                .append(randomNum)
                .toString();
    }
}
