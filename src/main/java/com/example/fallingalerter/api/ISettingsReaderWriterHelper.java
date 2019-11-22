package com.example.fallingalerter.api;

/**
 * Created by bbuescu on 7/7/2016.
 */
public interface ISettingsReaderWriterHelper {
    public String getPhoneNumber();
    public String getPersonName();
    public String getValueByKey(String key);
    public void writeValue(String key, String value);

}
