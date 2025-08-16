package com.example.exam.controller;

import com.example.exam.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理数据库访问异常
     */
    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ModelAndView handleDatabaseException(Exception ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("数据库操作异常: {}", ex.getMessage(), ex);
        
        // 如果是AJAX请求，返回JSON
        if (isAjaxRequest(request)) {
            ModelAndView mav = new ModelAndView("error/ajax-error");
            mav.addObject("errorMessage", "数据库操作失败，请稍后重试");
            mav.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return mav;
        }
        
        // 如果是重定向，添加Flash属性
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "数据库操作失败，请稍后重试");
            return new ModelAndView("redirect:" + referer);
        }
        
        // 否则返回错误页面
        ModelAndView mav = new ModelAndView("error/error-page");
        mav.addObject("errorMessage", "数据库操作失败，请稍后重试");
        mav.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("errorDetails", ex.getMessage());
        return mav;
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        logger.error("访问被拒绝: {}", ex.getMessage(), ex);
        
        // 如果是AJAX请求，返回JSON
        if (isAjaxRequest(request)) {
            ModelAndView mav = new ModelAndView("error/ajax-error");
            mav.addObject("errorMessage", "您没有权限执行此操作");
            mav.addObject("statusCode", HttpStatus.FORBIDDEN.value());
            return mav;
        }
        
        // 否则返回错误页面
        ModelAndView mav = new ModelAndView("error/error-page");
        mav.addObject("errorMessage", "您没有权限执行此操作");
        mav.addObject("statusCode", HttpStatus.FORBIDDEN.value());
        mav.addObject("errorDetails", ex.getMessage());
        return mav;
    }

    /**
     * 处理业务逻辑异常
     */
    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(BusinessException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("业务逻辑异常: {}", ex.getMessage(), ex);
        
        // 如果是AJAX请求，返回JSON
        if (isAjaxRequest(request)) {
            ModelAndView mav = new ModelAndView("error/ajax-error");
            mav.addObject("errorMessage", ex.getMessage());
            mav.addObject("statusCode", ex.getCode());
            return mav;
        }
        
        // 如果是重定向，添加Flash属性
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return new ModelAndView("redirect:" + referer);
        }
        
        // 否则返回错误页面
        ModelAndView mav = new ModelAndView("error/error-page");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("statusCode", ex.getCode());
        return mav;
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.error("发生未知错误: {}", ex.getMessage(), ex);
        
        // 如果是AJAX请求，返回JSON
        if (isAjaxRequest(request)) {
            ModelAndView mav = new ModelAndView("error/ajax-error");
            mav.addObject("errorMessage", "操作失败，请稍后重试");
            mav.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            return mav;
        }
        
        // 如果是重定向，添加Flash属性
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "操作失败，请稍后重试");
            return new ModelAndView("redirect:" + referer);
        }
        
        // 否则返回错误页面
        ModelAndView mav = new ModelAndView("error/error-page");
        mav.addObject("errorMessage", "操作失败，请稍后重试");
        mav.addObject("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("errorDetails", ex.getMessage());
        return mav;
    }

    /**
     * 判断是否是AJAX请求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}