package com.dingdong.controller.user;

import com.dingdong.common.Result;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.service.user.IUserRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户关系控制器
 * 提供关系邀请、确认、查询等接口
 */
@RestController
@RequestMapping("/relation")
@RequiredArgsConstructor
public class RelationController {

    private final IUserRelationService relationService;

    /**
     * 发送绑定邀请
     * 
     * @param relation 关系实体（包含elderId, childId, relationName）
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
     * 获取用户的所有关系
     * 
     * @param userId 用户ID
     * @return 关系列表
     */
    @GetMapping("/myRelations")
    public Result<List<UserRelation>> getMyRelations(@RequestParam Long userId) {
        return Result.success(relationService.getMyRelations(userId));
    }
}
