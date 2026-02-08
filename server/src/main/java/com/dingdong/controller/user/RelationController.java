package com.dingdong.controller.user;

import com.dingdong.common.Result;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.dto.user.RelationDisplayDTO;
import com.dingdong.entity.user.RelationHistory;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.service.user.IRelationHistoryService;
import com.dingdong.service.user.IUserRelationService;
import com.dingdong.vo.user.RelationDisplayVO;
import com.dingdong.vo.user.UserRelationVO;
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
    private final IRelationHistoryService relationHistoryService;

    /**
     * 获取关系历史记录
     */
    @GetMapping("/history")
    public Result<List<RelationHistory>> getHistory(@RequestParam Long relationId) {
        return Result.success(relationHistoryService.getHistoryByRelationId(relationId));
    }

    /**
     * 发起解绑
     * 
     * @param request 请求体，包含 relationId 和 reason
     */
    @PostMapping("/unbind/initiate")
    public Result<Boolean> initiateUnbind(@RequestBody UnbindRequest request) {
        Long userId = SystemContextHolder.getUserId();
        return Result.success(relationService.initiateUnbind(request.getRelationId(), request.getReason(), userId));
    }

    /**
     * 撤回解绑
     */
    @PostMapping("/unbind/withdraw")
    public Result<Boolean> withdrawUnbind(@RequestParam Long relationId) {
        Long userId = SystemContextHolder.getUserId();
        return Result.success(relationService.withdrawUnbind(relationId, userId));
    }

    @lombok.Data
    public static class UnbindRequest {
        private Long relationId;
        private String reason;
    }

    /**
     * 直接绑定关系（用于确认邀请时）
     *
     * @param bindDTO 绑定请求
     * @return 是否成功
     */
    @PostMapping("/bind")
    public Result<Boolean> bind(@RequestBody BindDTO bindDTO) {
        // 验证当前用户是否是被邀请人
        Long currentUserId = SystemContextHolder.getUserId();
        if (!currentUserId.equals(bindDTO.getSupervisedId())) {
            return Result.error("只有被邀请人才能接受绑定");
        }
        // 验证不能自己邀请自己
        if (bindDTO.getSupervisedId().equals(bindDTO.getSupervisorId())) {
            return Result.error("不能与自己建立关系");
        }
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
    public Result<List<UserRelationVO>> getMyRelations() {
        Long userId = SystemContextHolder.getUserId();
        List<UserRelation> relations = relationService.getMyRelations(userId);

        List<UserRelationVO> vos = relations.stream().map(relation -> {
            UserRelationVO vo = new UserRelationVO();
            vo.setId(relation.getId());
            vo.setInitiatorId(relation.getInitiatorId());
            vo.setPartnerId(relation.getPartnerId());
            vo.setRelationName(relation.getRelationName());
            vo.setStatus(relation.getStatus());
            return vo;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(vos);
    }

    /**
     * 获取用户的关系列表（带用户信息）
     * 返回分类好的关系数据，包含对方用户的昵称和头像
     *
     * @return 关系展示列表
     */
    @GetMapping("/listWithUserInfo")
    public Result<List<RelationDisplayVO>> getRelationsWithUserInfo() {
        Long userId = SystemContextHolder.getUserId();
        List<RelationDisplayDTO> dtos = relationService.getRelationsWithUserInfo(userId);

        List<RelationDisplayVO> vos = dtos.stream().map(dto -> {
            RelationDisplayVO vo = new RelationDisplayVO();
            vo.setId(dto.getId());
            vo.setInitiatorId(dto.getInitiatorId());
            vo.setPartnerId(dto.getPartnerId());
            vo.setRelationName(dto.getRelationName());
            vo.setStatus(dto.getStatus());
            vo.setOtherUserId(dto.getOtherUserId());
            vo.setOtherNickname(dto.getOtherNickname());
            vo.setOtherAvatar(dto.getOtherAvatar());
            vo.setRole(dto.getRole());
            vo.setUnbindExpireTime(dto.getUnbindExpireTime());
            return vo;
        }).collect(java.util.stream.Collectors.toList());

        return Result.success(vos);
    }
}
