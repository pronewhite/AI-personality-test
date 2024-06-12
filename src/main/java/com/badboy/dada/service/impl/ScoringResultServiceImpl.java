package com.badboy.dada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.badboy.dada.dao.UserDao;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.constant.CommonConstant;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.model.dto.scoringResult.ScoringResultQueryRequest;
import com.badboy.dada.model.entity.ScoringResult;
import com.badboy.dada.model.entity.User;
import com.badboy.dada.model.vo.ScoringResultVO;
import com.badboy.dada.model.vo.UserVO;
import com.badboy.dada.service.ScoringResultService;
import com.badboy.dada.service.UserService;
import com.badboy.dada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 得分结果接口服务实现
 *
 * 
 */
@Service
@Slf4j
public class ScoringResultServiceImpl implements ScoringResultService {

    @Resource
    private UserService userService;

    @Autowired
    private UserDao userDao;

    /**
     * 校验数据
     *
     * @param scoringResult
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validScoringResult(ScoringResult scoringResult, boolean add) {

    }

    /**
     * 获取查询条件
     *
     * @param scoringResultQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest scoringResultQueryRequest) {
        QueryWrapper<ScoringResult> queryWrapper = new QueryWrapper<>();
        if (scoringResultQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        String resultName = scoringResultQueryRequest.getResultName();
        String resultDesc = scoringResultQueryRequest.getResultDesc();
        Long appId = scoringResultQueryRequest.getAppId();
        Long userId = scoringResultQueryRequest.getUserId();
        String sortField = scoringResultQueryRequest.getSortField();
        String sortOrder = scoringResultQueryRequest.getSortOrder();

        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取得分结果接口封装
     *
     * @param scoringResult
     * @param request
     * @return
     */
    @Override
    public ScoringResultVO getScoringResultVO(ScoringResult scoringResult, HttpServletRequest request) {
        // 对象转封装类
        ScoringResultVO scoringResultVO = ScoringResultVO.objToVo(scoringResult);

        //  可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = scoringResult.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userDao.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        scoringResultVO.setUser(userVO);
        // endregion

        return scoringResultVO;
    }

    /**
     * 分页获取得分结果接口封装
     *
     * @param scoringResultPage
     * @param request
     * @return
     */
    @Override
    public Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> scoringResultPage, HttpServletRequest request) {
        List<ScoringResult> scoringResultList = scoringResultPage.getRecords();
        Page<ScoringResultVO> scoringResultVOPage = new Page<>(scoringResultPage.getCurrent(), scoringResultPage.getSize(), scoringResultPage.getTotal());
        if (CollUtil.isEmpty(scoringResultList)) {
            return scoringResultVOPage;
        }
        // 对象列表 => 封装对象列表
        List<ScoringResultVO> scoringResultVOList = scoringResultList.stream().map(scoringResult ->
             ScoringResultVO.objToVo(scoringResult)).collect(Collectors.toList());

        //  可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // endregion

        scoringResultVOPage.setRecords(scoringResultVOList);
        return scoringResultVOPage;
    }

}
