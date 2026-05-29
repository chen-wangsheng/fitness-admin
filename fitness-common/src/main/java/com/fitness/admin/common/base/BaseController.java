package com.fitness.admin.common.base;

import com.fitness.admin.common.result.R;
import com.fitness.admin.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public class BaseController {

    protected <T> R<T> success(T data) {
        return R.ok(data);
    }

    protected R<Void> success() {
        return R.ok();
    }

    protected <T> R<PageResult<T>> page(Page<T> page) {
        return R.ok(PageResult.of(page));
    }

    protected <T> R<List<T>> list(List<T> list) {
        return R.ok(list);
    }
}
