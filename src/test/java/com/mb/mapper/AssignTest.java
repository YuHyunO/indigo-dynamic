package com.mb.mapper;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AssignTest {

    @Setter @Getter
    class FileInfo {
        final String fileName;
        final long size;

        FileInfo(File file) {
            this.fileName = file.getName();
            this.size = file.length();
        }

    }

    @Test
    public void testAssign() {
        String DLIS_IFID = "RF1092";

        List<FileInfo> oldFileInfos = new ArrayList<>();
        File[] files = new File("C:\\tibco\\FTP_HOME\\lmsbkup\\jjc\\rec\\cur\\20160927").listFiles();

        //FTP Dir
        for (File file : files) {

            //FileInfo - 파일인 경우에만 FileInfo에 담음
            if (file.isFile()) {
                //FileInfo
                oldFileInfos.add(new FileInfo(file));
            }
        }




        List<FileInfo> assignedFileInfos = new ArrayList<>();
        //FileCheck
        int fileInfoSize = oldFileInfos.size();
        for (int i = 0; i < fileInfoSize; i++) {
            if (oldFileInfos.get(i).getFileName().startsWith(DLIS_IFID)) {
                //파일명이 DLIS_IFID 로 시작되는 요소는 새로운 FileInfoㄴ에 지정한다.
                assignedFileInfos.add(oldFileInfos.get(i));
            } else {
                //아닌경우에는 Pass
            }
        }


        //필터링된 파일목록을 표시
        for (FileInfo fileInfo : assignedFileInfos) {
            System.out.println(fileInfo.getFileName());
        }


    }
}
