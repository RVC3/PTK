package ru.ppr.chit.helpers;

import java.io.IOException;

import ru.ppr.chit.api.entity.FileEntity;
import ru.ppr.utils.FileUtils2;

/**
 * Маппит друг в друга разные клсассы File
 *
 * @author Dmitry Nevolin
 */
public class FileMapper {

    public static FileEntity javaToModel(java.io.File javaFile) throws IOException {
        FileEntity modelFile = new FileEntity();

        modelFile.setName(javaFile.getName());
        modelFile.setData(FileUtils2.readFileContent(javaFile));

        return modelFile;
    }

}
