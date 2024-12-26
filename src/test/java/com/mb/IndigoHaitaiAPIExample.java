package com.mb;

//Indigo API 호출 예제 - 수불재고상세조회 (RH1_1433_D_API)
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class IndigoHaitaiAPIExample {
    private final static String INDIGO_API_BASE_URL = "http://58.120.165.53:28115/indigo/haitai-api/";
    private static int connectTimeout = 5000;
    private static int readTimeout = 30000;

    public static void main(String[] args) {
        String apiKey = "0e1ffA1A0d4Be6CE1f48fb6cD7Dd9eB6FB38-9baFaF6d67EFd85bBEa2bd0-bCf87AB"; //API 인증키
        String interfaceId = "RH1_1433_D_API";
        String method = "POST";

        //요청 본문 준비
        String ownerId = "1433";
        String subulDate = "20200901";
        String warehouseId = "3020";
        String ownerItemId = "000009";

        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{");
        requestBody.append("\"OWNERID\":\"" + ownerId + "\",");
        requestBody.append("\"SUBULDATE\":\"" + subulDate + "\",");
        requestBody.append("\"WAREHOUSEID\":\"" + warehouseId + "\",");
        requestBody.append("\"OWNERITEMID\":\"" + ownerItemId + "\"");
        requestBody.append("}");

        try {
            //인터페이스 별 API URL 생성
            URL apiUrl = getApiUrl(interfaceId, apiKey);

            //API 호츌
            String response = request(apiUrl, "POST", requestBody.toString());
            
            //호출 결과 출력
            System.out.println(response);

        } catch (MalformedURLException me) {
            System.out.println("잘못된 API URL 입니다.");
            me.printStackTrace();
        } catch (IOException ie) {
            System.out.println("API 요청/응답 실패");
            ie.printStackTrace();
        }



    }

    private static URL getApiUrl(String interfaceId, String apiKey) throws MalformedURLException {
        StringBuilder urlBd = new StringBuilder(INDIGO_API_BASE_URL);
        urlBd.append(interfaceId)
                .append("?")
                .append("apikey=")
                .append(apiKey);
        return new URL(urlBd.toString());
    }

    private static String request(URL apiUrl, String method, String requestBody) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        try {
            if (requestBody != null && !requestBody.isEmpty()) {
                try (BufferedOutputStream bos
                             = new BufferedOutputStream(conn.getOutputStream())) {
                    bos.write(requestBody.getBytes());
                } catch (IOException ie) {
                    throw ie;
                }
            } else {
                conn.connect();
            }

            return readResponse(conn);
        } finally {
            conn.disconnect();
        }
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        byte[] bodyData;

        try (InputStream is = conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST ? conn.getInputStream() : conn.getErrorStream();
             ByteArrayOutputStream bas = new ByteArrayOutputStream();
             BufferedInputStream bis = new BufferedInputStream(is)) {

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bas.write(buffer, 0, bytesRead);
            }
            bodyData = bas.toByteArray();

        } catch (IOException ie) {
            throw ie;
        }

        return new String(bodyData);
    }

}
