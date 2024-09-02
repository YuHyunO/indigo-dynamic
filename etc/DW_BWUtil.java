package com.mb;

/**
 * This is java class for BW Utility
 * If you add a method, you should obey following rules.
 * 1. no use throw exception
 * 2. no use array type
 * 3. all method have 'public static'
 * 4. method must have return value
 **/

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.UUID;

/*import com.tibco.security.AXSecurityException;
import com.tibco.security.ObfuscationEngine;*/


/** Dongwon Designer CustomFunction
 *
 * @author jwjeon
 *
 */
public class DW_BWUtil {

    public static final String[][] HELP_STRINGS = {
            /* 도움말 작성
             * { <function name>, <help string>,
             *   <example input 1>, <exampleoutput 1>, <example input 2>, <example output 2>, ...}
             */
            {"getPasswordType", "Global Value Password Type의 Real Password 값을 알아오기 위해 사용합니다.",
                    "getPasswordType('CONN/SFTP/LOCAL/Password' - (글로벌 변수의 위치를 입력해야 합니다. ex. [ 'CONN/SFTP/LOCAL/Password ])", "cspi00(Real Password)",
                    "getPasswordType('CONN/SFTP/LOCAL/Password123' - (잘못된 글로벌 변수 위치를 입력한 경우))", "글로벌 변수위치를 확인해주세요. 해당 위치 ( CONN/SFTP/LOCAL/Password123 ) 의 글로벌 변수 값이 null인 것으로 판별되었습니다."},
            {"getUUID", "로깅시 사용할 36자리 유니크한 아이디를  생성할 때 주로 사용합니다.",
                    "getUUID()", "e2eea16c-ce2c-4684-9ea0-70cb16efaa73"},
            {"getIF_seq", "DB Insert 시  사용할 5자리 유니크 아이디를 생성할 때 사용합니다.(유니크5자리)",
                    "getIF_seq('0')", "A0000"},
            {"getDayofWeek", "배치 스케줄러 실행 시 한 주의 요일을 추출하기 위해서 사용됩니다.",
                    "getDayofWeek()", "1(일요일), 2(월요일) ---- 6(금요일), 7(토요일)"},
            {"getAdminPasswod", "어드민 패스워드를 잊어버렸을 시 어드민 패스워드를 가져옵니다.(TRA_HOME/DOMAIN/DOMAIN_NAME/AuthorizationDomain.properties -> Credential)",
                    "getAdminPasswod(Credential propery)", "cspi00"},
            {"substringKO", "한글(MS949)이 포함된 문자열을 필요한 길이만큼 추출합니다.",
                    "substringKO('한AB글테스트',1, 4)","한AB" },
            {"stringLengthKO", "문자열의 길이(byte)를 추출합니다.",
                    "stringLengthKO('ABC^@^한글이다^@^1234^@^####')","28" },
            {"padKO", "문자열의 뒷부분을 필요한 길이만큼 필요한 문자로 채운다.",
                    "padKO('테스트',50,'|')", "테스트||||||||||||||||||||||||||||||||||||||||||||" },
            {"padKOfront", "문자열의 앞부분을 필요한 길이만큼 필요한 문자로 채운다.",
                    "padKOfront('테스트',50,'|')", "||||||||||||||||||||||||||||||||||||||||||||테스트" },
            {"getIPAddress", "ip 주소를 가져올 때 사용합니다.",
                    "getIPAddress()", "127.0.0.1" },
            {"replace", "문자열 치환",
                    "replace('sample', 'sam', 'app')", "apple" },
    };

    public DW_BWUtil(){
    }

    /**
     * 글로벌 변수 중 Password Type의 Real Password를 가져옵니다
     * 패스워드 타입을 mapping 할 때 주로 사용합니다.
     *
     * @return 	String	Real Password
     */
    /*public static String getPasswordType(String Global_Variable_Location) {
        String realPass = com.tibco.pe.plugin.PluginProperties.getProperty("tibco.clientVar."+ Global_Variable_Location);

        if(realPass == null){
            realPass = "글로벌 변수위치를 확인해주세요. 해당 위치 ( "+Global_Variable_Location+" ) 의 글로벌 변수 값이 null인 것으로 판별되었습니다.";
        }

        return realPass;
    }*/

    /**
     * 유니크 ID 36자리를 생성합니다.
     * 로깅 시 사용할 유니크 아이디를 생성할 때 사용합니다.
     *
     * @return 	String	유니크 아이디 36자리
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

//	/**
//     * Interface ID 16자리를 생성합니다.
//     * DB Insert 시  사용할 유니크 아이디를 생성할 때 사용합니다.
//     *
//     * @return 	String	유니크 아이디 16자리
//     */
//	public static String getInterfaceID() {
//    	long dt = System.currentTimeMillis();
//    	SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
//    	Date dTime = new Date(dt);
//    	String sTime = sdf.format(dTime);
//
//    	String uuid = UUID.randomUUID().toString().substring(0, 4);
//
//    	String interfaceID = "E" + sTime + "-" + uuid;
//
//    	return interfaceID;
//	}

    /**
     * 유니크 아이디 5자리를 생성합니다.
     * DB Insert 시  사용할 유니크 아이디를 생성할 때 사용합니다.
     *
     * @return 	String	유니크 아이디 5자리
     */
    public static String getIF_seq(String position)
    {
        char chrword;
        if(position.length() <= 6) {
            while (position.length() != 6) position = "0" + position;
        }

        int intword = Integer.parseInt(position.substring(0, position.length() - 4));
        String seq = position.substring(position.length() - 4, position.length());

        if(intword >= 26) {
            intword = intword + 6 + 65;
        } else {
            intword = intword + 65;
        }

        chrword = (char)intword;

        return chrword + seq;
    }

    /**
     * 한 주의 요일을 숫자로 추출합니다.
     * 배치 스케줄러 실행 시 요일을 추출하기 위해서 사용됩니다.
     *
     * @return 	int	각 요일별 숫자(일요일1 ---- 토요일7)
     */
    public static int getDayofWeek(){
        Calendar today = Calendar.getInstance();

        int _dayOfTheWeek = today.get(Calendar.DAY_OF_WEEK);

        return _dayOfTheWeek;
    }

    /**
     * 어드민 패스워드를 잊어버렸을 시 어드민 패스워드를 가져옵니다.(TRA_HOME/DOMAIN/DOMAIN_NAME/AuthorizationDomain.properties -> Credential)
     *
     * @return 	String	admin Password
     */
    /*public static String getAdminPasswod(String adminPassword){
        String password = "";

        try {
            password= new String(ObfuscationEngine.decrypt(adminPassword.replace("\\", "")));
        } catch (AXSecurityException e) {
            e.printStackTrace();
        }
        return password;
    }*/

    /**
     * 한글이 포함된 문자열을 필요한 길이만큼 추출합니다.
     *
     * @param 	String	한글이 포함된 문자열
     * @param 	int		추출하고자 하는 문자열의 시작점
     * @param 	int		추출하고자 하는 문자열의 길이
     * @return 	String	추출된 문자열
     */
/*
    public static String substringKO(String str, int startLen, int endLen){
    	try{
    		if(str == null) return "";

    		byte abyte0[] = str.getBytes("MS949");

    		if( abyte0.length <= startLen )
    			return "";

    		if( abyte0.length <= endLen )
    			endLen = abyte0.length;

    		int count = 0;

    		byte abyte1[] = new byte[endLen];

    		for (int l = startLen; l < endLen + 1; l++) {
				abyte1[count++] = abyte0[l-1];
			}

    		return new String(abyte1, "MS949");
		} catch(Exception e) {
			return null;
		}
	}
*/
    public static String substringKO(String str, int startLen, int substrLen){
        int endLen;
        endLen = startLen + substrLen - 1;

        try{
            if(str == null) return "";

            byte abyte0[] = str.getBytes("MS949");

            if( abyte0.length <= startLen )
                return "";

//    		if( abyte0.length <= endLen )
//    			endLen = abyte0.length;
            if( abyte0.length <= endLen ) {
                endLen = abyte0.length;
                substrLen = abyte0.length - startLen + 1;
            }

            int count = 0;

            byte abyte1[] = new byte[substrLen];

            for (int l = startLen; l < endLen  + 1; l++) {
                abyte1[count++] = abyte0[l - 1];
            }

            return new String(abyte1, "MS949");
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * 문자열의 길이(byte)를 추출합니다.
     *
     * @param 	String	문자열
     * @return 	String	문자열의 길이(byte)
     */
    public static String stringLengthKO(String s){
        int strlen = 0;

        byte abyte0[] = s.getBytes();
        strlen = abyte0.length;

        return Integer.toString(strlen);
    }

    /**
     * 문자열의 뒷부분을 필요한 길이만큼 필요한 문자로 채운다.
     *
     * @param 	String	한글이 포함된 문자열
     * @param 	int		채우고자 하는 총 문자열 길이
     * @param 	String	채우고자하는 문자
     * @return 	String	추출된 문자열
     */
    public static String padKO(String str, int totLen, String padStr){
        String result = "";

        if(str == null) return "";

        byte abyte0[] = str.getBytes();
        byte abyte1[] = new byte[totLen];

        int j = abyte0.length;

        byte byte0 = padStr.getBytes()[0];

        if(j >= totLen)
            return str;

        for (int k = 0; k < j; k++) {
            abyte1[k] = abyte0[k];
        }

        for (int l = j; l < totLen; l++) {
            abyte1[l] = byte0;
        }

        result = new String(abyte1);

        return result;
    }

    /**
     * 문자열의 앞부분을 필요한 길이만큼 필요한 문자로 채운다.
     *
     * @param 	String	한글이 포함된 문자열
     * @param 	int		채우고자 하는 총 문자열 길이
     * @param 	String	채우고자하는 문자
     * @return 	String	추출된 문자열
     */
    public static String padKOfront(String str, int totLen, String padStr){
        String result = "";

        if(str == null) return "";

        byte abyte0[] = str.getBytes();

        int j = abyte0.length;

        int point = totLen - j;

        if (j >= totLen) {
            return str;
        }

        byte abyte1[] = new byte[point];

        byte byte0 = padStr.getBytes()[0];

        for (int k = 0; k < point; k++) {
            abyte1[k] = byte0;
        }

        result = new String(abyte1) + str;

        return result;
    }

    /**
     * Ip Address를 가져온다.
     *
     * @return 	String IP Address
     */
    public static String getIPAddress() {
        String returnValue = null;
        try {
            returnValue = ((Inet4Address)InetAddress.getLocalHost()).getHostAddress();
        } catch (UnknownHostException e) {
            returnValue = "UnknownHost";
        }
        return returnValue;
    }

    public static String replace(String source, String fromCharList, String toCharList) {
        String result = "";
        result = source.replaceAll(fromCharList, toCharList);
        return result;
    }
}
