package com.mb.ftp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.*;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class FTPConnectionTest {

    @Test
    public void getFTPConnection_test() {
        FTPClient ftp = new FTPClient();
        FTPClientConfig config = new FTPClientConfig();
        //config.setXXX(YYY); // change required options
        // for example config.setServerTimeZoneId("Pacific/Pitcairn")
        ftp.configure(config);


        String host = "52.78.68.15";
        String username = "oyh";
        String password = "oyh123";
        int port = 7021;
        boolean error = false;
        try {
            int reply;
            ftp.connect(host, port);
            ftp.enterLocalPassiveMode();
            ftp.setControlEncoding("UTF-8");

            System.out.println("Connected to " + host + ".");
            boolean loggedin = ftp.login(username, password);
            System.out.println("Is the user " + username + " logged in? " + loggedin);


            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if(!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }

            boolean changed = ftp.changeToParentDirectory();
            System.out.println("Changed " + changed);
            System.out.println("Working directory " + ftp.printWorkingDirectory());

            //boolean made = ftp.makeDirectory("/testsuccess");
            //System.out.println(made);
            FTPFile[] files = ftp.listFiles("/");
            for(FTPFile file : files) {
                System.out.println(file.getName());
            }
            String[] names = ftp.listNames();
            for(String name : names) {
                //System.out.println(name);
            }

            //... // transfer files
            ftp.logout();
        } catch(IOException e) {
            error = true;
            e.printStackTrace();
        } finally {
            if(ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch(IOException ioe) {
                    // do nothing
                }
            }
            System.exit(error ? 1 : 0);
        }
    }
}
