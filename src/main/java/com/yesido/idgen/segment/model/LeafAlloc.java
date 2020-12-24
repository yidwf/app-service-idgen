package com.yesido.idgen.segment.model;

public class LeafAlloc {

    private String biz_tag;
    private long max_id;
    private int step;
    private String updatetime;

    public String getBiz_tag() {
        return biz_tag;
    }

    public void setBiz_tag(String biz_tag) {
        this.biz_tag = biz_tag;
    }

    public long getMax_id() {
        return max_id;
    }

    public void setMax_id(long max_id) {
        this.max_id = max_id;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

}
