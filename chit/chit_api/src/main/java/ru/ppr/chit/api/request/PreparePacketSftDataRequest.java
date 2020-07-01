package ru.ppr.chit.api.request;

import java.util.List;

import ru.ppr.chit.api.entity.FileEntity;

/**
 * @author Dmitry Nevolin
 */
public class PreparePacketSftDataRequest extends BaseRequest {

    /**
     * Список файлов запроса
     */
    private List<FileEntity> fileRequestList;
    /**
     * Список файлов в директории In
     */
    private List<String> keyFileList;

    public List<FileEntity> getFileRequestList() {
        return fileRequestList;
    }

    public void setFileRequestList(List<FileEntity> fileRequestList) {
        this.fileRequestList = fileRequestList;
    }

    public List<String> getKeyFileList() {
        return keyFileList;
    }

    public void setKeyFileList(List<String> keyFileList) {
        this.keyFileList = keyFileList;
    }

}
