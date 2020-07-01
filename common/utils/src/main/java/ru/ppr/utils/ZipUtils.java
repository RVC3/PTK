package ru.ppr.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ru.ppr.logger.Logger;

/**
 * Класс для работы с zip архивами
 *
 * @author G.Kashka
 */
public class ZipUtils {

    private static int BUFFER_SIZE = 1000;
    private static String TAG = "ZipUtils";

    /**
     * Упаковывает файлы в zip-архив
     *
     * @param files
     * @param zipFile
     * @throws IOException
     */
    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }
    }

    /**
     * распаковывает Файл, не использовать для директорий, не использовать для
     * распаковки больших файлов (больше 1 Mb) - для этих случаев использовать
     * gzip-упаковку
     *
     * @param zipAbsPath
     * @param unZipAbsPath
     * @return
     */
    /**
     * Unzip it
     *
     * @param zipFile      input zip file
     * @param outputFolder zip file output folder
     */
    public static boolean unZip(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024];

        try {

            // create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            // get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                // System.out.println("file unzip : "+
                // newFile.getAbsoluteFile());

                // create all non exists folders
                // else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            // System.out.println("Done");

            return true;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * Распаковывает gzip-файл. Принято решение не использовать упаковку zip для
     * больших файлов (больше 1 Mb) потому-что на андроиде неудается корректно
     * распоковать zip архив упакованный NET кодом - возвращается ошибка
     * контройльной суммы.
     *
     * @param gzipAbsPath
     * @param unZipAbsPath
     * @return
     */
    public static boolean unpackGZip(String gzipAbsPath, String unZipAbsPath) {
        InputStream is;
        GZIPInputStream gzis;
        File f = new File(unZipAbsPath);
        f.delete();
        try {
            is = new FileInputStream(gzipAbsPath);
            gzis = new GZIPInputStream(is);
            byte[] buffer = new byte[1024];
            int count;

            FileOutputStream fout = new FileOutputStream(unZipAbsPath);

            while ((count = gzis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.close();
            gzis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Возвращает список распакованных файлов. Вернет null в случае ошибки
     * распаковки.
     *
     * @param zipFile
     * @param location
     * @return
     * @throws IOException
     */
    public static ArrayList<File> unzip(String zipFile, String location) {
        ArrayList<File> unzippedFiles = new ArrayList<File>();
        try {
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();
                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream fout = new FileOutputStream(path, false);

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, len);
                        }
                        zin.closeEntry();
                        fout.close();
                        unzippedFiles.add(new File(path));
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Logger.error(TAG, "Unzip exception", e);
            return null;
        }
        return unzippedFiles;
    }

    /**
     * Zip directory
     *
     * @param zipFile output ZIP file location
     */
    public static boolean zipDir(String dir, String zipFile) {

        File sourceNode = new File(dir);

        List<String> fileList = new ArrayList<String>();
        generateFileList(sourceNode, sourceNode, fileList);

        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // System.out.println("Output to Zip : " + zipFile);

            for (String file : fileList) {

                // System.out.println("File Added : " + file);
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(dir + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            // remember close it
            zos.close();

            // System.out.println("Done");

            return true;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Traverse a directory and get all files, and add the file into fileList
     *
     * @param node file or directory
     */
    private static void generateFileList(File sourceNode, File node, List<String> fileList) {

        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(sourceNode, node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(sourceNode, new File(node, filename), fileList);
            }
        }

    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private static String generateZipEntry(File sourceNode, String file) {
        return file.substring(sourceNode.getPath().length() + 1, file.length());
    }


    /**
     * Архирует файл {@param srcFile} в архив {@param dstArchive}
     *
     * @param srcFile    Файл, который нужно архивировать
     * @param dstArchive Файл результирующего архива
     * @return {@code true} если всё прошло успешно, {@code false} если не удалось закрыть OutputStream.
     * @throws IOException Если возникет ошибка при создании архива
     */
    public static boolean zipFile(File srcFile, File dstArchive) throws IOException {

        if (srcFile == null) {
            throw new NullPointerException();
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException();
        }
        if (srcFile.isDirectory()) {
            throw new IllegalArgumentException("srcDir is directory, not file");
        }

        ZipOutputStream zipOutputStream = null;
        try {
            FileOutputStream fos = new FileOutputStream(dstArchive);
            zipOutputStream = new ZipOutputStream(fos);
            zipFile(srcFile, "", srcFile.getName(), zipOutputStream);
            zipOutputStream.closeEntry();
            return true;
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                    return false;
                }
            }
        }
    }

    /**
     * Архирует папку {@param srcDir} в архив {@param dstArchive}
     *
     * @param srcDir     Папка, которую нужно архивировать
     * @param dstArchive Файл результирующего архива
     * @param withRoot   {@code true}, если нужно заархивировать корневую папку
     * @return {@code true} если всё прошло успешно, {@code false} если не удалось закрыть OutputStream.
     * @throws IOException Если возникет ошибка при создании архива
     */
    public static boolean zipDir(File srcDir, File dstArchive, boolean withRoot) throws IOException {

        if (srcDir == null) {
            throw new NullPointerException();
        }
        if (!srcDir.exists()) {
            return false;
        }
        if (srcDir.isFile()) {
            throw new IllegalArgumentException("srcDir is file, not directory");
        }

        ZipOutputStream zipOutputStream = null;
        try {
            FileOutputStream fos = new FileOutputStream(dstArchive);
            zipOutputStream = new ZipOutputStream(fos);
            if (withRoot) {
                zipDirRecursively(srcDir, "", srcDir.getName(), zipOutputStream);
            } else {
                File[] filesInDir = srcDir.listFiles();

                for (File file : filesInDir) {
                    if (file.isDirectory()) {
                        if (!zipDirRecursively(file, "", file.getName(), zipOutputStream)) {
                            return false;
                        }
                    } else {
                        if (!zipFile(file, "", file.getName(), zipOutputStream)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                    return false;
                }
            }
        }
    }

    /**
     * @param src             файл, который нужно архивировать
     * @param dst             сущность архива, в которую будет записан файл.
     * @param zipOutputStream поток на запись в архив
     * @return {@code true} если всё прошло успешно
     * @throws IOException если возникет ошибка при создании архива
     */
    public static boolean zipFileToEntry(File src, ZipEntry dst, ZipOutputStream zipOutputStream) throws IOException {
        FileInputStream srcStream = null;

        try {
            if (src.isDirectory() || dst.isDirectory()) {
                if (src.isDirectory() && dst.isDirectory()) {
                    zipOutputStream.putNextEntry(dst);
                    zipOutputStream.closeEntry();
                    return true;
                } else {
                    throw new IllegalArgumentException("trying to write dir to non-dir: " + src.getAbsolutePath() + " | " + dst.getName());
                }
            }

            srcStream = new FileInputStream(src);
            zipOutputStream.putNextEntry(dst);

            int len;
            byte[] buf = new byte[8192];
            while ((len = srcStream.read(buf)) > 0) {
                zipOutputStream.write(buf, 0, len);
            }

            zipOutputStream.closeEntry();

            return true;
        } catch (Exception exception) {
            Logger.error(TAG, exception);
            throw exception;
        } finally {
            if (srcStream != null) {
                try {
                    srcStream.close();
                } catch (IOException exception) {
                    Logger.error(TAG, exception);
                }
            }
        }
    }

    /**
     * Пишет файл {@param srcFile} в архив по пути  {@param dstPath}
     *
     * @param srcFile         Файл, который нужно архивировать
     * @param dstPath         Путь в архиве, куда нужно положить файл. Должен заканчитваться на '/'
     * @param zipOutputStream Поток на запись в архив
     * @return {@code true} если всё прошло успешно, {@code false} если не удалось закрыть OutputStream.
     * @throws IOException Если возникет ошибка при создании архива
     */
    public static boolean zipFile(File srcFile, String dstPath, String dstName, ZipOutputStream zipOutputStream) throws IOException {
        FileInputStream srcStream = null;

        try {
            srcStream = new FileInputStream(srcFile);
            String name = dstPath + dstName;
            ZipEntry zipEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[1024];
            while ((len = srcStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }
            zipOutputStream.closeEntry();

            return true;
        } catch (Exception e) {
            Logger.error(TAG, "srcFile: " + srcFile, e);
            throw e;
        } finally {
            if (srcStream != null) {
                try {
                    srcStream.close();
                } catch (IOException e) {
                    Logger.error(TAG, "srcFile: " + srcFile, e);
                    return false;
                }
            }
        }
    }

    /**
     * Пишет папку {@param srcDir} в архив по пути  {@param dstPath}
     *
     * @param srcDir          Папка, которую нужно архивировать
     * @param dstPath         Путь в архиве, куда нужно положить папку. Должен заканчитваться на '/'
     * @param zipOutputStream Поток на запись в архив
     * @return {@code true} если всё прошло успешно, {@code false} если не удалось закрыть OutputStream.
     * @throws IOException Если возникет ошибка при создании архива
     */
    public static boolean zipDirRecursively(File srcDir, String dstPath, String dstName, ZipOutputStream zipOutputStream) throws IOException {
        String name = dstPath + dstName + "/";
        File[] filesInDir = srcDir.listFiles();

        if (filesInDir == null || filesInDir.length == 0) {
            // Добавляем пустую папку
            ZipEntry zipEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.closeEntry();
            return true;
        }

        for (File file : filesInDir) {
            if (file.isDirectory()) {
                if (!zipDirRecursively(file, name, file.getName(), zipOutputStream)) {
                    return false;
                }
            } else {
                if (!zipFile(file, name, file.getName(), zipOutputStream)) {
                    return false;
                }
            }
        }

        return true;
    }
}
