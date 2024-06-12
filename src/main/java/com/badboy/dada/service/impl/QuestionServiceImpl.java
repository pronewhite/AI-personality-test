package com.badboy.dada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.badboy.dada.dao.UserDao;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.constant.CommonConstant;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.mapper.QuestionMapper;
import com.badboy.dada.model.dto.question.QuestionQueryRequest;
import com.badboy.dada.model.entity.Question;
import com.badboy.dada.model.entity.User;
import com.badboy.dada.model.vo.QuestionVO;
import com.badboy.dada.model.vo.UserVO;
import com.badboy.dada.service.QuestionService;
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
 * 问题接口服务实现
 *
 * 
 */
@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private UserService userService;

    @Autowired
    private UserDao userDao;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
//        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
//        // todo 从对象中取值
//        String title = question.getTitle();
//        // 创建数据时，参数不能为空
//        if (add) {
//            // todo 补充校验规则
//            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
//        }
//        // 修改数据时，有参数则校验
//        // todo 补充校验规则
//        if (StringUtils.isNotBlank(title)) {
//            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
//        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        //  从对象中取值
        Long appId = questionQueryRequest.getAppId();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取问题接口封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userDao.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // endregion
        return questionVO;
    }

    /**
     * 分页获取问题接口封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(question ->
             QuestionVO.objToVo(question)).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

}
