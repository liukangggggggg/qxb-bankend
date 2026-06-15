
package com.qxb.consultation.service.impl;

import com.qxb.common.exception.ServiceException;
import com.qxb.consultation.domain.BizParentChildRelation;
import com.qxb.consultation.mapper.BizParentChildRelationMapper;
import com.qxb.consultation.service.IBizParentChildRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 亲子绑定关系 服务层实现
 */
@Service
public class BizParentChildRelationServiceImpl implements IBizParentChildRelationService {

    @Autowired
    private BizParentChildRelationMapper relationMapper;

    /**
     * 根据主键查询亲子关系
     */
    @Override
    public BizParentChildRelation selectById(Long relationId) {
        return relationMapper.selectById(relationId);
    }

    /**
     * 根据家长ID查询所有孩子列表
     */
    @Override
    public List<BizParentChildRelation> selectByParentUserId(Long parentUserId) {
        return relationMapper.selectByParentUserId(parentUserId);
    }

    /**
     * 根据孩子ID查询所有家长列表
     */
    @Override
    public List<BizParentChildRelation> selectByChildUserId(Long childUserId) {
        return relationMapper.selectByChildUserId(childUserId);
    }

    /**
     * 根据家长和孩子ID查询亲子关系
     */
    @Override
    public BizParentChildRelation selectByParentAndChild(Long parentUserId, Long childUserId) {
        return relationMapper.selectByParentAndChild(parentUserId, childUserId);
    }

    /**
     * 多条件查询亲子关系列表
     */
    @Override
    public List<BizParentChildRelation> selectList(BizParentChildRelation query) {
        return relationMapper.selectList(query);
    }

    /**
     * 查询亲子关系总数
     */
    @Override
    public int selectCount(BizParentChildRelation query) {
        return relationMapper.selectCount(query);
    }

    /**
     * 新增亲子绑定关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(BizParentChildRelation relation) {
        if (relation.getParentUserId() == null || relation.getChildUserId() == null) {
            throw new ServiceException("家长ID和孩子ID不能为空");
        }

        if (checkRelationExists(relation.getParentUserId(), relation.getChildUserId())) {
            throw new ServiceException("该亲子关系已存在");
        }

        if (relation.getParentUserId().equals(relation.getChildUserId())) {
            throw new ServiceException("家长和孩子不能是同一用户");
        }

        relation.setAuthStatus("0");
        relation.setCreateTime(LocalDateTime.now());
        return relationMapper.insertSelective(relation);
    }

    /**
     * 修改亲子绑定关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(BizParentChildRelation relation) {
        if (relation.getRelationId() == null) {
            throw new ServiceException("关系ID不能为空");
        }
        return relationMapper.updateByIdSelective(relation);
    }

    /**
     * 审核亲子关系（确认/拒绝）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditRelation(Long relationId, String authStatus) {
        if (relationId == null) {
            throw new ServiceException("关系ID不能为空");
        }

        if (!"1".equals(authStatus) && !"2".equals(authStatus)) {
            throw new ServiceException("审核状态只能是1(已确认)或2(已拒绝)");
        }

        BizParentChildRelation relation = relationMapper.selectById(relationId);
        if (relation == null) {
            throw new ServiceException("亲子关系不存在");
        }

        if (!"0".equals(relation.getAuthStatus())) {
            throw new ServiceException("该亲子关系已经审核过，不能重复审核");
        }

        LocalDateTime bindTime = "1".equals(authStatus) ? LocalDateTime.now() : null;
        return relationMapper.updateAuthStatus(relationId, authStatus, bindTime, LocalDateTime.now());
    }

    /**
     * 删除亲子绑定关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long relationId) {
        if (relationId == null) {
            throw new ServiceException("关系ID不能为空");
        }
        return relationMapper.deleteById(relationId);
    }

    /**
     * 解除亲子绑定关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindRelation(Long parentUserId, Long childUserId) {
        if (parentUserId == null || childUserId == null) {
            throw new ServiceException("家长ID和孩子ID不能为空");
        }
        return relationMapper.deleteByParentAndChild(parentUserId, childUserId);
    }

    /**
     * 校验亲子关系是否已存在
     */
    @Override
    public boolean checkRelationExists(Long parentUserId, Long childUserId) {
        BizParentChildRelation relation = relationMapper.selectByParentAndChild(parentUserId, childUserId);
        return relation != null;
    }

    /**
     * 统计家长的孩子数量
     */
    @Override
    public int countByParentUserId(Long parentUserId) {
        if (parentUserId == null) {
            return 0;
        }
        List<BizParentChildRelation> list = relationMapper.selectByParentUserId(parentUserId);
        return list != null ? list.size() : 0;
    }

    /**
     * 统计孩子的家长数量
     */
    @Override
    public int countByChildUserId(Long childUserId) {
        if (childUserId == null) {
            return 0;
        }
        List<BizParentChildRelation> list = relationMapper.selectByChildUserId(childUserId);
        return list != null ? list.size() : 0;
    }
}