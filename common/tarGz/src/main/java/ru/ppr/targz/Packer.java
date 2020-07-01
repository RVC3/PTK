package ru.ppr.targz;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;


/**
 * Created by USERNAME on 27.02.2015.
 */
public class Packer {
    private Context c;

    /**
     * @param c Application Context
     */
    public Packer(@NonNull Context c) {
        if (null == c)
            throw new IllegalArgumentException("Argument cannot be null");
        this.c = c;
    }

    /**
     * Unpack *.tar.gz file in to selected directory, owerrite files if exsist.
     *
     * @param inputFile
     * @param outputDirectoryPath
     * @return true, if unpacked successful and there is no errors
     */
    public boolean unpack(File inputFile, String outputDirectoryPath) {
        if (null == inputFile || TextUtils.isEmpty(outputDirectoryPath))
            return false;
        try {
            return unGz(inputFile, true, outputDirectoryPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean unTar(File inputFile, String outputDirectoryPath) {
        if (null == inputFile || TextUtils.isEmpty(outputDirectoryPath))
            return false;

        try {
            if (outputDirectoryPath.endsWith("/"))
                outputDirectoryPath = outputDirectoryPath.substring(0, outputDirectoryPath.length() - 1);
            // Create a TarInputStream
            TarInputStream tis = null;

            tis = new TarInputStream(new BufferedInputStream(new FileInputStream(inputFile)));

            TarEntry entry;

            while ((entry = tis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[2048];
                File outputFile = new File(outputDirectoryPath + "/" + entry.getName());
                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos);

                while ((count = tis.read(data)) != -1) {
                    dest.write(data, 0, count);
                }

                dest.flush();
                dest.close();
            }

            tis.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

        return false;
    }

    /**
     * Unpack *.gz or *.tar.gz file.
     *
     * @param inputFile
     * @param unTar               if true, run unTar function.
     * @param outputDirectoryPath
     * @return true, if unpacked successful and there is no errors
     */
    private boolean unGz(File inputFile, boolean unTar, String outputDirectoryPath) {
        if (null == inputFile || TextUtils.isEmpty(outputDirectoryPath))
            return false;

        File tmpFile = null;
        try {

            // Create a TarInputStream
            GZIPInputStream gzis = null;

            gzis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(inputFile)));

            String filename = String.valueOf(System.nanoTime()) + ".tmp";
            tmpFile = new File(c.getCacheDir(), filename);

            int count;
            byte data[] = new byte[2048];
            FileOutputStream fos = new FileOutputStream(tmpFile);
            BufferedOutputStream dest = new BufferedOutputStream(fos);

            while ((count = gzis.read(data)) != -1) {
                dest.write(data, 0, count);
            }

            dest.flush();
            dest.close();

            gzis.close();

            if (unTar)
                return unTar(tmpFile, outputDirectoryPath);

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != tmpFile)
                tmpFile.delete();
        }

        return false;
    }
}