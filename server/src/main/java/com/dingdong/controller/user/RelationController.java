package com.dingdong.controller.user;

import com.dingdong.common.Result;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.service.user.IUserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;

@RestController
@RequestMapping("/relation")
public class RelationController {

    @Autowired
    private IUserRelationService relationService;

    @PostMapping("/invite")
    public Result<Boolean> invite(@RequestBody UserRelation relation) {
        relation.setStatus(0); // Pending
        return Result.success(relationService.save(relation));
    }

    @PostMapping("/accept")
    public Result<Boolean> accept(@RequestParam Long id) {
        UserRelation relation = relationService.getById(id);
        if (relation != null) {
            relation.setStatus(1);
            return Result.success(relationService.updateById(relation));
        }
        return Result.error("Relation not found");
    }

    @GetMapping("/myRelations")
    public Result<List<UserRelation>> getMyRelations(@RequestParam Long userId) {
        // Get generic relations where user is either supervisor or subordinate
        // This might need a custom query or two queries.
        // For simplicity, let's return all where childId or elderId (mapped to
        // supervisor/subordinate) matches.
        // Note: Field names in UserRelation are still elderId/childId due to legacy.
        // elderId = subordinate, childId = supervisor.

        LambdaQueryWrapper<UserRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelation::getChildId, userId).or().eq(UserRelation::getElderId, userId);
        return Result.success(relationService.list(wrapper));
    }
}
