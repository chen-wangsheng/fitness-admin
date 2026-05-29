package com.fitness.admin.community.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_post")
public class CommunityPost extends BaseEntity {

    private Long userId;
    private String content;
    private String images;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private Integer isTop;
    private Integer isEssence;
}
