package com.fitness.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.admin.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_role")
public class AdminRole extends BaseEntity {

    private String name;
    private String code;
    private String description;

    /**
     * 入参/响应使用的 permissions(数组形态)。
     * 读侧由 service.refreshPermissionsFromRaw() 在出参前刷新。
     */
    @TableField(exist = false)
    private List<String> permissions;

    /**
     * 数据库列 admin_role.permissions(JSON 数组字符串)。
     * 入参不会带上该字段,service 写库前从 permissions 序列化得到。
     * WRITE_ONLY:响应时不返回数据库原始串,只给前端 permissions 数组。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField("permissions")
    private String permissionsRaw;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    /**
     * 把 {@code permissionsRaw}(JSON 数组)拆成 List 赋给 {@code permissions}。
     * 读侧(详情/列表)使用,异常时回退到空列表避免影响主流程。
     */
    public void refreshPermissionsFromRaw() {
        if (permissionsRaw == null || permissionsRaw.isBlank()) {
            this.permissions = Collections.emptyList();
            return;
        }
        try {
            List<String> list = MAPPER.readValue(permissionsRaw, LIST_TYPE);
            this.permissions = list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            this.permissions = Collections.emptyList();
        }
    }

    /**
     * 把 {@code permissions} 序列化为 JSON 数组字符串赋给 {@code permissionsRaw}。
     * 写侧使用,异常时写入空数组保证列非空。
     */
    public void writePermissionsAsRaw() {
        if (permissions == null || permissions.isEmpty()) {
            this.permissionsRaw = "[]";
            return;
        }
        try {
            this.permissionsRaw = MAPPER.writeValueAsString(permissions);
        } catch (Exception e) {
            this.permissionsRaw = "[]";
        }
    }
}
