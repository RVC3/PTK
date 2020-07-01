package ru.ppr.chit.api.request;

/**
 * @author Dmitry Nevolin
 */
public class GetBoardingListRequest extends BaseRequest {

    private int minId;

    public int getMinId() {
        return minId;
    }

    public void setMinId(int minId) {
        this.minId = minId;
    }

}
