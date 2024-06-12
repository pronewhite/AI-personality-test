package com.badboy.dada.controller;

import cn.hutool.json.JSONUtil;
import com.badboy.dada.dao.AppDao;
import com.badboy.dada.dao.QuestionDao;
import com.badboy.dada.exception.BusinessException;
import com.badboy.dada.exception.ExceptionResponseEnum;
import com.badboy.dada.manager.AIManager;
import com.badboy.dada.model.dto.question.*;
import com.badboy.dada.model.entity.App;
import com.badboy.dada.model.enums.UserRoleEnum;
import com.badboy.dada.utils.AssertUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.badboy.dada.annotation.AuthCheck;
import com.badboy.dada.common.BaseResponse;
import com.badboy.dada.common.DeleteRequest;
import com.badboy.dada.common.ErrorCode;
import com.badboy.dada.common.ResultUtils;
import com.badboy.dada.constant.UserConstant;
import com.badboy.dada.exception.ThrowUtils;
import com.badboy.dada.model.entity.Question;
import com.badboy.dada.model.entity.User;
import com.badboy.dada.model.vo.QuestionVO;
import com.badboy.dada.service.QuestionService;
import com.badboy.dada.service.UserService;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 问题接口接口
 *
 * @author badboy
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private AppDao appDao;
    @Autowired
    private AIManager aiManager;
    @Autowired
    private Scheduler vipScheduler;

    private static final String ZHIPUAI_SYSTEM_PROMPT = "你是一个出题专家，根据我给你的信息进行出题：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目个数，\n" +
            "题目选项个数\n" +
            "```\n" +
            "\n" +
            "要求：\n" +
            "\t1. 出题要求尽量简短，不能带有额外的序号，生成的题目不能重复；\n" +
            "\t2. 返回题目时，必须按照以下格式：\n" +
            "\t```\n" +
            "\t[{ \"options\":[{\"key\":\"A\",\"value\":\"选项值\",\"result\":\"I\",\"score\":\"\"},{\"key\":\"B\",\"value\":\"选项值\",\"result\":\"E\",\"score\":\"\"}]，\"title\":\"标题\"}]\n" +
            "\t```\n" +
            "\t其中，title 表示题目的标题，options 表示题目的选项集合，options 中的 key 表示选项比如，A,B,C；value表示选项的内容, result 所赋的值必须是 MBTI 性格理论中的四个维度，并且要与你回答的选项的内容匹配，score 表示该选项对应的分数，分数必须是0到100以内的某个整数，并且只有问题的正确选项才设置分数，错误选项分数设置为0；options 中的选项以逗号分隔；\n" +
            "\t3. 注意 options 中 result 这个属性，只有【【【应用描述】】】包含 性格测试 时才赋值，而且必须是 MBTI 性格理论中的四个维度，即 I、E、S、N、T、F、J、P中的一种，并且要与你回答的选项的内容匹配；\n" +
            "\t4. 注意 options 中 score这个属性，只有【【【应用描述】】】不包含 性格测试时 才赋值，并且所有问题的所有正确选项的 score 总和必须等于100，不能大于或者小于100，你可以根据每一个 题目的难易程度来为该选项设置 score 的大小；但是一定要记住，所有问题的正确选项的 score 总和必须等于100，不能大于或者小于100；另外，题目的错误选项 score 都是 0，只有正确选项才可以设置 score；\n" +
            "\t示例：\n" +
            "\t```\n" +
            "\t[{\"title\":\"下列哪个选项是 Java 中的基本数据类型？\",\"options\":[{\"key\":\"A\",\"value\":\"String\",\"score\":\"0\"},{\"key\":\"B\",\"value\":\"int\",\"score\":\"10\"},{\"key\":\"C\",\"value\":\"ArrayList\",\"score\":\"0\"}]},{\"title\":\"Java 中的哪个关键字用于定义一个类？\",\"options\":[{\"key\":\"A\",\"value\":\"class\",\"score\":\"90\"},{\"key\":\"B\",\"value\":\"interface\",\"score\":\"0\"},{\"key\":\"C\",\"value\":\"enum\",\"score\":\"0\"}]}]\n" +
            "\t```\n" +
            "\t重点：所有题目对应的所有的正确选项对应的score总和必须为100，只有正确选项才能设置 score,错误选项 score 为0；\n" +
            "\t5. 如果【【【应用描述】】】不包含 性格测试，那么问题的选项中可以出现多选，即一个问题中可以出现多个正确选项，同时，答案的分布需要具备一定的随机性，而不能具有明显的规律性；\n" +
            "\t6. 对于 options 中的 score，再强调一下，在满足前面几条的基础上，必须保证题目对应的正确选项才设置对应的 score ，而错误选项 score 设置为0即可，你生成的所有问题正确答案的 score 总和必须为100；\n" +
            "\t7. 生成完题目之后，如果【【【应用描述】】】不包含 性格测试，那么你需要自己检查所生成的所有题目的所有正确答案对应的score的总和是不是等于100，如果不等于，重新分配正确答案对应的 score，直至所有正确答案的 score 总和等于100，否则直接返回。总之，你需要保证，所有问题正确答案对应的 score 总和必须等于100；\n" +
            "\t8. 当【【【应用描述】】】不包含 性格测试时，每一个问题的正确答案应该随机分布，即随机出现在 A、B、C、D中，而不能呈现一定的规律性，即不能所有问题的正确答案都是 A或者 B或者 C 或者 D；\n" +
            "\t9. 回答的内容必须以我给你的正确的 JSON 格式返回，即 JSON 数组；\n" +
            "返回你的结果之前，检查正确选项的分布是否具有随机性，如果没有，则调整正确答案的分布；";

    // region 增删改查

    /**
     * 创建问题接口
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody @Valid QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        //  在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        question.setQuestionContent(JSONUtil.toJsonStr(questionAddRequest.getQuestionContent()));
        // 数据校验
//        questionService.validQuestion(question, true);
        //  填充默认值
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionDao.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除问题接口
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody @Valid DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionDao.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionDao.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新问题接口（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        // 数据校验
//        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionDao.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionDao.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取问题接口（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionDao.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取问题接口列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> questionPage = questionDao.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取问题接口列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionDao.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的问题接口列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionDao.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 编辑问题接口（给用户使用）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        // 数据校验
//        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionDao.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionDao.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    // endregion

    // region AI 生成问题
    @PostMapping("/ai/generate")
    public BaseResponse<List<QuestionContentDTO>> aiGenerateQuestion(@RequestBody AIGenerateQuestionDTO aiGenerateQuestionDTO) {
        Long appId = aiGenerateQuestionDTO.getAppId();
//        AssertUtil.isEmpty(appId,"应用ID不能为空");
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        // 拿到应用信息
        App app = appDao.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        String userMessage = getUserMessage(app, aiGenerateQuestionDTO);
        // 生成问题
        String questionStr = aiManager.doSyncUnstableRequest(ZHIPUAI_SYSTEM_PROMPT, userMessage);
        int startIndex = questionStr.indexOf("[");
        int endIndex = questionStr.lastIndexOf("]");
        List<QuestionContentDTO> questionContentDTOS = JSONUtil.toList(questionStr.substring(startIndex, endIndex + 1), QuestionContentDTO.class);
        // AI 生成结果不需要入库，所以直接返回结果即可
        // 返回结果
        return ResultUtils.success(questionContentDTOS);
    }

    /**
     * 流式获取 AI 结果
     * @param aiGenerateQuestionDTO
     * @return
     */
    @PostMapping("/ai/generate/sse")
    public SseEmitter aiGenerateStreamQuestion(@RequestBody AIGenerateQuestionDTO aiGenerateQuestionDTO, HttpServletRequest request) {
        Long appId = aiGenerateQuestionDTO.getAppId();
//        AssertUtil.isEmpty(appId,"应用ID不能为空");
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        // 拿到应用信息
        App app = appDao.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        Scheduler scheduler = Schedulers.io();
        // 如果是 VIP 用户那么使用自定义的线程池执行生成问题任务，提高用户体验
        boolean isVip = UserRoleEnum.of(loginUser.getUserRole()).getValue().equals(UserRoleEnum.VIP.getValue());
        if(isVip){
            scheduler = vipScheduler;
        }
        String userMessage = getUserMessage(app, aiGenerateQuestionDTO);
        // 生成问题
        SseEmitter sseEmitter = new SseEmitter(0L);// 初始化过期时间为0L，表示永不过期
        StringBuilder sb = new StringBuilder();// 用于拼接 AI 回答的问题
        AtomicInteger flag = new AtomicInteger(0); // 用来标识是否已经生成了一道完整的题
        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(ZHIPUAI_SYSTEM_PROMPT, userMessage);
        modelDataFlowable.observeOn(scheduler) // 指定被观察者发送数据的线程
                .subscribeOn(scheduler) // 指定观察者发送数据的线程
                .map(modelData -> modelData.getChoices().get(0).getDelta().getContent()) // 获取数据
                .map(str -> str.replaceAll("\\s","")) // 替换掉结果中所有可能的特殊字符，比如空格，.等等
                .filter(str -> StringUtils.isNotBlank(str)) // 过滤掉空格
                // 将字符串转换为字符，为什么使用 flatMap，因为将 字符串转换为 字符涉及到一对多的转换，
                // 比如 "你好" 转换为 字符，需要转换为 ['你','好']，flatMap 正适合干这种事
                .flatMap(message ->  {
                    List<Character> messageChars = new ArrayList<>();
                    for (char c : message.toCharArray()) {
                        messageChars.add(c);
                    }
                    return Flowable.fromIterable(messageChars);
                })
                .doOnComplete(sseEmitter::complete)
                .subscribe(c -> {
                    if( c == '{'){
                        flag.addAndGet(1);
                    }
                    if(flag.get() > 0){
                        sb.append(c);
                    }
                    if(c == '}'){
                        flag.addAndGet(-1);
                        if(flag.get() == 0){
                            System.out.println("当前用户是：" + isVip + ",使用的线程是：" + Thread.currentThread().getName());
                            // 已经收集到一个完整的题目，可以向前端返回
                            // 非 VIP 用户延迟 10秒返回
                            if(!isVip){
                                Thread.sleep(10000);
                            }
                            sseEmitter.send(JSONUtil.toJsonStr(sb.toString()));
                            // 重置 sb
                            sb.setLength(0);
                        }
                    }

                });

        return sseEmitter;
    }

    private String getUserMessage(App app, AIGenerateQuestionDTO aiGenerateQuestionDTO){
        StringBuilder sb = new StringBuilder();
        sb.append(app.getAppName()).append(",").append("\n");
        sb.append(app.getAppDesc()).append(",").append("\n");
        sb.append(aiGenerateQuestionDTO.getQuestionCount()).append(",").append("\n");
        sb.append(aiGenerateQuestionDTO.getOptionsCount());
        return sb.toString();
    }
    // endregion
}
