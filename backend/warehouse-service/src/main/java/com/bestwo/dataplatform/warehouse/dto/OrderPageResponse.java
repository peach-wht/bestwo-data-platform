package com.bestwo.dataplatform.warehouse.dto;

import java.util.List;

public class OrderPageResponse {

    private List<OrderDetailResponse> list;
    private int pageNum;
    private int pageSize;
    private long total;

    public List<OrderDetailResponse> getList() {
        return list;
    }

    public void setList(List<OrderDetailResponse> list) {
        this.list = list;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
