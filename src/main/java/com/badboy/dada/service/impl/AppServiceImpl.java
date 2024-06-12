package com.badboy.dada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.badboy.dada.dao.AppDao;
import com.badboy.dada.dao.UserDao;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.constant.CommonConstant;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.mapper.AppMapper;
import com.badboy.dada.model.dto.app.AppQueryRequest;
import com.badboy.dada.model.entity.App;
import com.badboy.dada.model.entity.User;
import com.badboy.dada.model.vo.AppVO;
import com.badboy.dada.model.vo.UserVO;
import com.badboy.dada.service.AppService;
import com.badboy.dada.service.UserService;
import com.badboy.dada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用接口服务实现
 *
 * 
 */
@Service
@Slf4j
public class AppServiceImpl implements AppService {

    @Resource
    private UserService userService;

    @Autowired
    private AppDao appDao;

    @Autowired
    private UserDao userDao;

    /**
     * 校验数据
     *
     * @param app
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validApp(App app, boolean add) {

    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        //  从对象中取值
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        String reviewMessage = appQueryRequest.getReviewMessage();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType).or();
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy).or();
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus).or();
        queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage).or();
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取应用接口封装
     *
     * @param app
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        // 对象转封装类
        AppVO appVO = AppVO.objToVo(app);

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userDao.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);
        return appVO;
    }

    /**
     * 分页获取应用接口封装
     *
     * @param appPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getRecords().size(), appPage.getTotal());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppVO> appVOList = appList.stream().map(app ->
             AppVO.objToVo(app)
        ).collect(Collectors.toList());

        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

}
