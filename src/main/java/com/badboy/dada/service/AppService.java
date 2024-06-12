package com.badboy.dada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.badboy.dada.model.dto.app.AppQueryRequest;
import com.badboy.dada.model.entity.App;
import com.badboy.dada.model.vo.AppVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 应用接口服务
 *
 * 
 */
public interface AppService {

    /**
     * 校验数据
     *
     * @param app
     * @param add 对创建的数据进行校验
     */
    void validApp(App app, boolean add);

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);
    
    /**
     * 获取应用接口封装
     *
     * @param app
     * @param request
     * @return
     */
    AppVO getAppVO(App app, HttpServletRequest request);

    /**
     * 分页获取应用接口封装
     *
     * @param appPage
     * @param request
     * @return
     */
    Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request);
}
