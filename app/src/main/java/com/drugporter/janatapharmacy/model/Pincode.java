
package com.drugporter.janatapharmacy.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Pincode {

    @SerializedName("PincodeData")
    private List<PincodeDatum> mPincodeData;
    @SerializedName("ResponseCode")
    private String mResponseCode;
    @SerializedName("ResponseMsg")
    private String mResponseMsg;
    @SerializedName("Result")
    private String mResult;

    public List<PincodeDatum> getPincodeData() {
        return mPincodeData;
    }

    public void setPincodeData(List<PincodeDatum> pincodeData) {
        mPincodeData = pincodeData;
    }

    public String getResponseCode() {
        return mResponseCode;
    }

    public void setResponseCode(String responseCode) {
        mResponseCode = responseCode;
    }

    public String getResponseMsg() {
        return mResponseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        mResponseMsg = responseMsg;
    }

    public String getResult() {
        return mResult;
    }

    public void setResult(String result) {
        mResult = result;
    }

}
