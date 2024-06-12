package com.badboy.dada.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.badboy.dada.dao.AppDao;
import com.badboy.dada.dao.UserAnswerDao;
import com.badboy.dada.exception.BusinessException;
import com.badboy.dada.model.entity.App;
import com.badboy.dada.model.vo.UserVO;
import com.badboy.dada.strategy.ScoreStrategyContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badboy.dada.annotation.AuthCheck;
import com.badboy.dada.common.BaseResponse;
import com.badboy.dada.common.DeleteRequest;
import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.common.ResultUtils;
import com.badboy.dada.constant.UserConstant;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.model.dto.userAnswer.UserAnswerAddRequest;
import com.badboy.dada.model.dto.userAnswer.UserAnswerEditRequest;
import com.badboy.dada.model.dto.userAnswer.UserAnswerQueryRequest;
import com.badboy.dada.model.dto.userAnswer.UserAnswerUpdateRequest;
import com.badboy.dada.model.entity.UserAnswer;
import com.badboy.dada.model.entity.User;
import com.badboy.dada.model.vo.UserAnswerVO;
import com.badboy.dada.service.UserAnswerService;
import com.badboy.dada.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户答案接口接口
 *
 * @author badboy
 */
@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService userAnswerService;

    @Resource
    private UserService userService;

    @Autowired
    private UserAnswerDao userAnswerDao;
    @Autowired
    private ScoreStrategyContext scoreStrategyContext;
    @Autowired
    private AppDao appDao;

    // region 增删改查

    /**
     * 创建用户答案接口
     *
     * @param userAnswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<UserAnswerVO> addUserAnswer(@Valid @RequestBody UserAnswerAddRequest userAnswerAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userAnswerAddRequest == null, ErrorCode.PARAMS_ERROR);
        //  在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerAddRequest, userAnswer);
        userAnswer.setChoices(JSONUtil.toJsonStr(userAnswerAddRequest.getChoices()));
        // 校验 appId
        ThrowUtils.throwIf(userAnswer.getAppId() == null, ErrorCode.PARAMS_ERROR);
        App app = appDao.getById(userAnswer.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, true);
        //  填充默认值
        User loginUser = userService.getLoginUser(request);
        userAnswer.setUserId(loginUser.getId());
        // 调用判题策略进行判题
        UserAnswer aiValidJudgeResult = scoreStrategyContext.judge(userAnswer, app);
        aiValidJudgeResult.setUserId(loginUser.getId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(loginUser, userVO);
        // 写入数据库
        boolean result;
        try{
            result = userAnswerDao.save(aiValidJudgeResult);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        }catch (DuplicateKeyException e){
            // ignore
            log.info("不能多次提交答案哦", e);
        }
        UserAnswerVO userAnswerVO = UserAnswerVO.objToVo(aiValidJudgeResult);
        userAnswerVO.setUser(userVO);
        // 返回新写入的数据 id
        return ResultUtils.success(userAnswerVO);
    }

    /**
     * 删除用户答案接口
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = userAnswerDao.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userAnswerDao.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户答案接口（仅管理员可用）
     *
     * @param userAnswerUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest userAnswerUpdateRequest) {
        if (userAnswerUpdateRequest == null || userAnswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerUpdateRequest, userAnswer);
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, false);
        // 判断是否存在
        long id = userAnswerUpdateRequest.getId();
        UserAnswer oldUserAnswer = userAnswerDao.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = userAnswerDao.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户答案接口（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserAnswer userAnswer = userAnswerDao.getById(id);
        ThrowUtils.throwIf(userAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVO(userAnswer, request));
    }

    /**
     * 分页获取用户答案接口列表（仅管理员可用）
     *
     * @param userAnswerQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest) {
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 查询数据库
        Page<UserAnswer> userAnswerPage = userAnswerDao.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        return ResultUtils.success(userAnswerPage);
    }

    /**
     * 分页获取用户答案接口列表（封装类）
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
                                                               HttpServletRequest request) {
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> userAnswerPage = userAnswerDao.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户答案接口列表
     *
     * @param userAnswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest userAnswerQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(userAnswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        userAnswerQueryRequest.setUserId(loginUser.getId());
        long current = userAnswerQueryRequest.getCurrent();
        long size = userAnswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> userAnswerPage = userAnswerDao.page(new Page<>(current, size),
                userAnswerService.getQueryWrapper(userAnswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(userAnswerService.getUserAnswerVOPage(userAnswerPage, request));
    }

    /**
     * 编辑用户答案接口（给用户使用）
     *
     * @param userAnswerEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest userAnswerEditRequest, HttpServletRequest request) {
        if (userAnswerEditRequest == null || userAnswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserAnswer userAnswer = new UserAnswer();
        BeanUtils.copyProperties(userAnswerEditRequest, userAnswer);
        // 数据校验
        userAnswerService.validUserAnswer(userAnswer, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = userAnswerEditRequest.getId();
        UserAnswer oldUserAnswer = userAnswerDao.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userAnswerDao.updateById(userAnswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    @GetMapping("/generateId")
    public BaseResponse<Long> generateUserAnswerId(){
        return ResultUtils.success(IdUtil.getSnowflakeNextId());
    }
}
