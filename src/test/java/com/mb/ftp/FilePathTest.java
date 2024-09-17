package com.mb.ftp;

import lombok.extern.slf4j.Slf4j;
import mb.dnm.util.FileUtil;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FilePathTest {

    @Test
    public void path_test() {
        Path local = Paths.get("C:\\FTPdownload\\");
        Path parentDir = Paths.get(FileUtil.replaceToOSFileSeparator("inner/test/sasd.txt/")).getParent();
        log.info("{}", "\\".length());
        log.info("Filename: {}", local);
        log.info("Parent: {}", parentDir);
        log.info("Local Path: {}", Paths.get(local.toString(), parentDir.toString()));
        log.info("Ends with: {}", parentDir.toString().endsWith("\\"));

    }
}
