package ru.ppr.chit.api.request;

import ru.ppr.chit.api.entity.FileEntity;

/**
 * @author Dmitry Nevolin
 */
public class PreparePacketSftLicenseRequest extends BaseRequest {

    private FileEntity licenseRequest;

    public FileEntity getLicenseRequest() {
        return licenseRequest;
    }

    public void setLicenseRequest(FileEntity licenseRequest) {
        this.licenseRequest = licenseRequest;
    }

}
