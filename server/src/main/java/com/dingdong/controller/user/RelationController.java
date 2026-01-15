package com.dingdong.controller.user;

import com.dingdong.common.Result;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.dto.user.RelationDisplayDTO;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.service.user.IUserRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户关系控制器
 * 提供关系邀请、确认、查询等接口
 * 
 * @author Antigravity
 */
@RestController
@RequestMapping("/relation")
@RequiredArgsConstructor
public class RelationController {

    private final IUserRelationService relationService;

    /**
     * 直接绑定关系（用于确认邀请时）
     *
     * @param bindDTO 绑定请求
     * @return 是否成功
     */
    @PostMapping("/bind")
    public Result<Boolean> bind(@RequestBody BindDTO bindDTO) {
        return Result.success(relationService.bindSupervised(bindDTO));
    }

    /**
     * 发送绑定邀请
     *
     * @param relation 关系实体
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
     * 拒绝绑定邀请
     *
     * @param id 关系记录ID
     * @return 是否成功
     */
    @PostMapping("/reject")
    public Result<Boolean> reject(@RequestParam Long id) {
        return Result.success(relationService.rejectInvite(id));
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
        return Result.success(relationService.getRelationsWithUserInfo(userId));
    }
}
