package com.badboy.dada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.badboy.dada.dao.UserAnswerDao;
import com.badboy.dada.dao.UserDao;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.constant.CommonConstant;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.mapper.UserAnswerMapper;
import com.badboy.dada.model.dto.userAnswer.UserAnswerQueryRequest;
import com.badboy.dada.model.entity.UserAnswer;
import com.badboy.dada.model.entity.User;
import com.badboy.dada.model.vo.UserAnswerVO;
import com.badboy.dada.model.vo.UserVO;
import com.badboy.dada.service.UserAnswerService;
import com.badboy.dada.service.UserService;
import com.badboy.dada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户答案接口服务实现
 *
 * 
 */
@Service
@Slf4j
public class UserAnswerServiceImpl implements UserAnswerService {

    @Resource
    private UserService userService;

    @Autowired
    private UserDao userDao;

    /**
     * 校验数据
     *
     * @param userAnswer
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUserAnswer(UserAnswer userAnswer, boolean add) {
        Long id = userAnswer.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
    }

    /**
     * 获取查询条件
     *
     * @param userAnswerQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest userAnswerQueryRequest) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (userAnswerQueryRequest == null) {
            return queryWrapper;
        }
        //  从对象中取值
        Long appId = userAnswerQueryRequest.getAppId();
        Integer appType = userAnswerQueryRequest.getAppType();
        Integer scoringStrategy = userAnswerQueryRequest.getScoringStrategy();
        String resultName = userAnswerQueryRequest.getResultName();
        String resultDesc = userAnswerQueryRequest.getResultDesc();
        Long userId = userAnswerQueryRequest.getUserId();
        String sortField = userAnswerQueryRequest.getSortField();
        String sortOrder = userAnswerQueryRequest.getSortOrder();
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取用户答案接口封装
     *
     * @param userAnswer
     * @param request
     * @return
     */
    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer userAnswer, HttpServletRequest request) {
        // 对象转封装类
        UserAnswerVO userAnswerVO = UserAnswerVO.objToVo(userAnswer);

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = userAnswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userDao.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userAnswerVO.setUser(userVO);
        // endregion

        return userAnswerVO;
    }

    /**
     * 分页获取用户答案接口封装
     *
     * @param userAnswerPage
     * @param request
     * @return
     */
    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> userAnswerPage, HttpServletRequest request) {
        List<UserAnswer> userAnswerList = userAnswerPage.getRecords();
        Page<UserAnswerVO> userAnswerVOPage = new Page<>(userAnswerPage.getCurrent(), userAnswerPage.getSize(), userAnswerPage.getTotal());
        if (CollUtil.isEmpty(userAnswerList)) {
            return userAnswerVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserAnswerVO> userAnswerVOList = userAnswerList.stream().map(userAnswer ->
             UserAnswerVO.objToVo(userAnswer)).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // endregion

        userAnswerVOPage.setRecords(userAnswerVOList);
        return userAnswerVOPage;
    }

}
