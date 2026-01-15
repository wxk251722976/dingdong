package com.dingdong.controller.user;

import com.dingdong.common.Result;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.user.RelationDisplayDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.user.IUserRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户关系控制器
 * 提供关系邀请、确认、查询等接口
 */
@RestController
@RequestMapping("/relation")
@RequiredArgsConstructor
public class RelationController {

    private final IUserRelationService relationService;
    private final ISysUserService sysUserService;

    /**
     * 发送绑定邀请
     *
     * @param relation 关系实体（包含supervisedId, supervisorId, relationName）
     * @return 是否成功
     */
    @PostMapping("/invite")
    public Result<Boolean> invite(@RequestBody UserRelation relation) {
        return Result.success(relationService.sendInvite(relation));
    }

    /**
     * 接受绑定邀请
     *
     * @param id 关系记录ID
     * @return 是否成功
     */
    @PostMapping("/accept")
    public Result<Boolean> accept(@RequestParam Long id) {
        return Result.success(relationService.acceptInvite(id));
    }

    /**
     * 获取用户的所有关系（原始数据）
     *
     * @return 关系列表
     */
    @GetMapping("/myRelations")
    public Result<List<UserRelation>> getMyRelations() {
        Long userId = SystemContextHolder.getUserId();
        return Result.success(relationService.getMyRelations(userId));
    }

    /**
     * 获取用户的关系列表（带用户信息）
     * 返回分类好的关系数据，包含对方用户的昵称和头像
     *
     * @return 关系展示列表
     */
    @GetMapping("/listWithUserInfo")
    public Result<List<RelationDisplayDTO>> getRelationsWithUserInfo() {
        Long userId = SystemContextHolder.getUserId();

        // 1. 获取所有关系
        List<UserRelation> relations = relationService.getMyRelations(userId);
        if (relations.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 2. 收集所有需要的用户ID
        List<Long> userIds = new ArrayList<>();
        for (UserRelation r : relations) {
            if (r.getSupervisorId() != null && !r.getSupervisorId().equals(userId)) {
                userIds.add(r.getSupervisorId());
            }
            if (r.getSupervisedId() != null && !r.getSupervisedId().equals(userId)) {
                userIds.add(r.getSupervisedId());
            }
        }

        // 3. 批量查询用户信息
        Map<Long, SysUser> userMap = userIds.isEmpty() ? new java.util.HashMap<>()
                : sysUserService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        // 4. 组装DTO
        List<RelationDisplayDTO> result = new ArrayList<>();
        for (UserRelation r : relations) {
            RelationDisplayDTO dto = new RelationDisplayDTO();
            dto.setId(r.getId());
            dto.setSupervisedId(r.getSupervisedId());
            dto.setSupervisorId(r.getSupervisorId());
            dto.setRelationName(r.getRelationName());
            dto.setStatus(r.getStatus());

            // 判断当前用户角色
            if (r.getSupervisorId().equals(userId)) {
                // 我是监督者，对方是被监督者
                dto.setRole("SUPERVISOR");
                dto.setOtherUserId(r.getSupervisedId());
                SysUser other = userMap.get(r.getSupervisedId());
                dto.setOtherNickname(other != null ? other.getNickname() : "未知用户");
                dto.setOtherAvatar(other != null ? other.getAvatar() : null);
            } else {
                // 我是被监督者，对方是监督者
                dto.setRole("SUPERVISED");
                dto.setOtherUserId(r.getSupervisorId());
                SysUser other = userMap.get(r.getSupervisorId());
                dto.setOtherNickname(other != null ? other.getNickname() : "未知用户");
                dto.setOtherAvatar(other != null ? other.getAvatar() : null);
            }

            result.add(dto);
        }

        return Result.success(result);
    }
}
