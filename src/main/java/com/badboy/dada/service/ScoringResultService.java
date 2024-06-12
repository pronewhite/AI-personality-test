package com.badboy.dada.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.badboy.dada.model.dto.scoringResult.ScoringResultQueryRequest;
import com.badboy.dada.model.entity.ScoringResult;
import com.badboy.dada.model.vo.ScoringResultVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 得分结果接口服务
 *
 * 
 */
public interface ScoringResultService  {

    /**
     * 校验数据
     *
     * @param scoringResult
     * @param add 对创建的数据进行校验
     */
    void validScoringResult(ScoringResult scoringResult, boolean add);

    /**
     * 获取查询条件
     *
     * @param scoringResultQueryRequest
     * @return
     */
    QueryWrapper<ScoringResult> getQueryWrapper(ScoringResultQueryRequest scoringResultQueryRequest);
    
    /**
     * 获取得分结果接口封装
     *
     * @param scoringResult
     * @param request
     * @return
     */
    ScoringResultVO getScoringResultVO(ScoringResult scoringResult, HttpServletRequest request);

    /**
     * 分页获取得分结果接口封装
     *
     * @param scoringResultPage
     * @param request
     * @return
     */
    Page<ScoringResultVO> getScoringResultVOPage(Page<ScoringResult> scoringResultPage, HttpServletRequest request);
}
