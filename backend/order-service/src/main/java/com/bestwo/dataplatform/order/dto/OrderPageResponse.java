package com.bestwo.dataplatform.order.dto;

import java.util.List;

public class OrderPageResponse {

    private List<OrderListItemResponse> list;
    private long pageNum;
    private long pageSize;
    private long total;

    public List<OrderListItemResponse> getList() {
        return list;
    }

    public void setList(List<OrderListItemResponse> list) {
        this.list = list;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
