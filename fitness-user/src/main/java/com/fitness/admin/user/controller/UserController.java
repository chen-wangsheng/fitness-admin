package com.fitness.admin.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.user.dto.UserQueryDTO;
import com.fitness.admin.user.dto.UserUpdateDTO;
import com.fitness.admin.user.entity.UserTag;
import com.fitness.admin.user.service.UserService;
import com.fitness.admin.user.service.UserTagService;
import com.fitness.admin.user.vo.UserDetailVO;
import com.fitness.admin.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final UserTagService userTagService;

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    public R<PageResult<UserVO>> list(UserQueryDTO queryDTO) {
        Page<UserVO> page = userService.queryUserPage(queryDTO);
        return page((Page) page);
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public R<UserDetailVO> getById(@PathVariable Long id) {
        return success(userService.getUserDetail(id));
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public R<Void> update(@RequestBody UserUpdateDTO updateDTO) {
        userService.updateUser(updateDTO);
        return success();
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        userService.updateStatus(id, parseStatus(body.get("status")));
        return success();
    }

    @Operation(summary = "批量更新用户状态")
    @PutMapping("/batch-status")
    public R<Void> batchStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = ((List<?>) body.get("user_ids")).stream()
                .map(item -> Long.valueOf(item.toString())).toList();
        userService.batchUpdateStatus(userIds, parseStatus(body.get("status")));
        return success();
    }

    private Integer parseStatus(Object value) {
        String s = value.toString();
        if ("active".equals(s) || "1".equals(s)) return 1;
        if ("disabled".equals(s) || "0".equals(s)) return 0;
        return Integer.valueOf(s);
    }

    @Operation(summary = "标签列表")
    @GetMapping("/tags")
    public R<List<UserTag>> listTags() {
        return success(userTagService.list());
    }

    @Operation(summary = "保存标签")
    @PostMapping("/tag")
    public R<Void> saveTag(@RequestBody UserTag tag) {
        userTagService.save(tag);
        return success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/tag/{id}")
    public R<Void> deleteTag(@PathVariable Long id) {
        userTagService.delete(id);
        return success();
    }
}
