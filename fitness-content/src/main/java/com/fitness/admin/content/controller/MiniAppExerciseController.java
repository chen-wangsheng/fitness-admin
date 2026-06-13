package com.fitness.admin.content.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.R;
import com.fitness.admin.content.dto.FavoriteCheckResponse;
import com.fitness.admin.content.dto.FavoriteItem;
import com.fitness.admin.content.dto.FavoriteToggleRequest;
import com.fitness.admin.content.dto.FavoriteToggleResponse;
import com.fitness.admin.content.service.MiniAppExerciseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序动作库接口
 */
@Tag(name = "小程序-动作模块")
@RestController
@RequestMapping("/miniapp/exercise")
@RequiredArgsConstructor
@SaCheckLogin
public class MiniAppExerciseController extends BaseController {

    private final MiniAppExerciseService miniAppExerciseService;

    @Operation(summary = "检查收藏状态")
    @GetMapping("/favorite/check")
    public R<FavoriteCheckResponse> checkFavorite(@RequestParam Long exerciseId) {
        return success(miniAppExerciseService.checkFavorite(exerciseId));
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/favorite")
    public R<FavoriteToggleResponse> toggleFavorite(@RequestBody FavoriteToggleRequest request) {
        return success(miniAppExerciseService.toggleFavorite(request.getExerciseId()));
    }

    @Operation(summary = "收藏列表")
    @GetMapping("/favorite/list")
    public R<List<FavoriteItem>> getFavoriteList() {
        return success(miniAppExerciseService.getFavoriteList());
    }
}
