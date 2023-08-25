package report;

import java.util.Arrays;

public enum ReportType {
    SIMPLE_MONTHLY, COLLECTION_AGGREGATION;

    public static ReportType getDefault(){ return SIMPLE_MONTHLY; }

    /**
     * If the type parameter is invalid then return the default.
     */
    public static ReportType getOrDefault(String type){
        try{
            return ReportType.valueOf(type.toUpperCase());
        }
        catch (Exception ignored){
            return getDefault();
        }
    }
}
