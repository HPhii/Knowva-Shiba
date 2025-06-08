package com.example.demo.model.io.request;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequest implements Pageable {
    Integer limit;
    Integer offset;
    private final Sort sort;

    public PageRequest(Integer limit, Integer offset, Sort sort) {
        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
    }

    public PageRequest(Integer limit, Integer offset) {
        this(limit, offset, Sort.unsorted());
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new PageRequest(getPageSize(), (int) (getOffset() + getPageSize()));
    }

    public Pageable previous() {
        return hasPrevious() ?
                new PageRequest(getPageSize(), (int) (getOffset() - getPageSize())) : this;
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new PageRequest(getPageSize(), 0);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new PageRequest(getPageSize(), pageNumber * getPageSize());
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }
}
