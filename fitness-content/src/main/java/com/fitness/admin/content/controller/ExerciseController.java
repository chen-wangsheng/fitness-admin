package com.fitness.admin.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.admin.common.base.BaseController;
import com.fitness.admin.common.result.PageResult;
import com.fitness.admin.common.result.R;
import com.fitness.admin.content.dto.ExerciseCreateDTO;
import com.fitness.admin.content.dto.ExerciseQueryDTO;
import com.fitness.admin.content.entity.BodyPart;
import com.fitness.admin.content.service.BodyPartService;
import com.fitness.admin.content.service.ExerciseService;
import com.fitness.admin.content.vo.ExerciseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "动作库管理")
@RestController
@RequestMapping("/exercise")
@RequiredArgsConstructor
public class ExerciseController extends BaseController {

    private final ExerciseService exerciseService;
    private final BodyPartService bodyPartService;

    @Operation(summary = "身体部位列表(小程序)")
    @GetMapping("/body-parts")
    public R<List<BodyPart>> bodyParts() {
        return success(bodyPartService.list());
    }

    @Operation(summary = "动作列表")
    @GetMapping("/list")
    public R<PageResult<ExerciseVO>> list(ExerciseQueryDTO queryDTO) {
        Page<ExerciseVO> page = exerciseService.queryPage(queryDTO);
        return page((Page) page);
    }

    @Operation(summary = "动作详情")
    @GetMapping("/{id}")
    public R<ExerciseVO> getById(@PathVariable Long id) {
        return success(exerciseService.getDetail(id));
    }

    @Operation(summary = "创建动作")
    @PostMapping
    public R<Void> create(@RequestBody ExerciseCreateDTO createDTO) {
        exerciseService.create(createDTO);
        return success();
    }

    @Operation(summary = "更新动作")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody ExerciseCreateDTO updateDTO) {
        exerciseService.update(id, updateDTO);
        return success();
    }

    @Operation(summary = "删除动作")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        exerciseService.delete(id);
        return success();
    }
}
