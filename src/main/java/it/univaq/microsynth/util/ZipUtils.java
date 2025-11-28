package it.univaq.microsynth.util;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipUtils {

    public static void zipFolder(Path sourceFolderPath, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            Files.walk(sourceFolderPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceFolderPath.relativize(path).toString());
                        try (InputStream is = Files.newInputStream(path)) {
                            zos.putNextEntry(zipEntry);
                            is.transferTo(zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }
}
