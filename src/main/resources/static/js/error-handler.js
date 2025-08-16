/**
 * 全局错误处理脚本
 * 为考试座位管理系统提供统一的错误处理机制
 */

(function() {
    'use strict';

    // 配置对象
    const ErrorHandlerConfig = {
        timeout: 10000, // 10秒超时
        retryAttempts: 3,
        retryDelay: 1000
    };

    // 错误类型定义
    const ErrorTypes = {
        NETWORK: 'NETWORK_ERROR',
        TIMEOUT: 'TIMEOUT_ERROR',
        SERVER: 'SERVER_ERROR',
        VALIDATION: 'VALIDATION_ERROR',
        AUTH: 'AUTHENTICATION_ERROR',
        UNKNOWN: 'UNKNOWN_ERROR'
    };

    // 错误消息映射
    const ErrorMessages = {
        [ErrorTypes.NETWORK]: '网络连接错误，请检查网络后重试',
        [ErrorTypes.TIMEOUT]: '请求超时，请稍后重试',
        [ErrorTypes.SERVER]: '服务器错误，请稍后重试',
        [ErrorTypes.VALIDATION]: '数据验证失败，请检查输入内容',
        [ErrorTypes.AUTH]: '请先登录系统',
        [ErrorTypes.UNKNOWN]: '发生未知错误，请稍后重试'
    };

    // 显示错误提示
    function showErrorToast(message, type = 'error') {
        // 创建错误提示元素
        const toast = document.createElement('div');
        toast.className = `alert alert-${type === 'error' ? 'danger' : 'warning'} alert-dismissible fade show position-fixed`;
        toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        toast.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(toast);
        
        // 3秒后自动移除
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 3000);
    }

    // 显示成功提示
    function showSuccessToast(message) {
        const toast = document.createElement('div');
        toast.className = 'alert alert-success alert-dismissible fade show position-fixed';
        toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        toast.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 3000);
    }

    // 网络连接检查
    function checkNetworkConnection() {
        if (!navigator.onLine) {
            showErrorToast('网络连接已断开，请检查网络连接');
            return false;
        }
        return true;
    }

    // 处理AJAX错误
    function handleAjaxError(error) {
        console.error('AJAX Error:', error);

        if (error.code === 'ECONNABORTED') {
            showErrorToast(ErrorMessages[ErrorTypes.TIMEOUT]);
            return ErrorTypes.TIMEOUT;
        }

        if (error.response) {
            const status = error.response.status;
            const message = error.response.data || '操作失败';

            switch (status) {
                case 400:
                    showErrorToast(ErrorMessages[ErrorTypes.VALIDATION] + ': ' + message);
                    return ErrorTypes.VALIDATION;
                case 401:
                    showErrorToast(ErrorMessages[ErrorTypes.AUTH]);
                    setTimeout(() => window.location.href = '/login', 2000);
                    return ErrorTypes.AUTH;
                case 403:
                    showErrorToast('您没有权限执行此操作');
                    return ErrorTypes.AUTH;
                case 404:
                    showErrorToast('请求的资源不存在');
                    return ErrorTypes.SERVER;
                case 500:
                    showErrorToast(ErrorMessages[ErrorTypes.SERVER]);
                    return ErrorTypes.SERVER;
                default:
                    showErrorToast(ErrorMessages[ErrorTypes.UNKNOWN] + ': ' + message);
                    return ErrorTypes.UNKNOWN;
            }
        } else if (error.request) {
            showErrorToast(ErrorMessages[ErrorTypes.NETWORK]);
            return ErrorTypes.NETWORK;
        } else {
            showErrorToast(ErrorMessages[ErrorTypes.UNKNOWN] + ': ' + error.message);
            return ErrorTypes.UNKNOWN;
        }
    }

    // 创建加载指示器
    function createLoadingIndicator() {
        const loading = document.createElement('div');
        loading.id = 'global-loading';
        loading.className = 'position-fixed top-50 start-50 translate-middle';
        loading.style.cssText = 'z-index: 9999;';
        loading.innerHTML = `
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">加载中...</span>
            </div>
            <div class="mt-2 text-center">处理中...</div>
        `;
        return loading;
    }

    // 显示加载状态
    function showLoading() {
        const existing = document.getElementById('global-loading');
        if (!existing) {
            document.body.appendChild(createLoadingIndicator());
        }
    }

    // 隐藏加载状态
    function hideLoading() {
        const loading = document.getElementById('global-loading');
        if (loading) {
            loading.parentNode.removeChild(loading);
        }
    }

    // 初始化全局错误处理
    function initGlobalErrorHandling() {
        // 设置全局AJAX配置
        if (typeof axios !== 'undefined') {
            // 设置超时
            axios.defaults.timeout = ErrorHandlerConfig.timeout;

            // 请求拦截器
            axios.interceptors.request.use(
                function (config) {
                    if (checkNetworkConnection()) {
                        showLoading();
                        return config;
                    }
                    return Promise.reject(new Error('Network offline'));
                },
                function (error) {
                    hideLoading();
                    return Promise.reject(error);
                }
            );

            // 响应拦截器
            axios.interceptors.response.use(
                function (response) {
                    hideLoading();
                    return response;
                },
                function (error) {
                    hideLoading();
                    handleAjaxError(error);
                    return Promise.reject(error);
                }
            );
        }

        // 全局错误监听
        window.addEventListener('error', function(event) {
            console.error('Global error:', event.error);
            showErrorToast('发生错误: ' + event.message);
        });

        // 未处理的Promise拒绝
        window.addEventListener('unhandledrejection', function(event) {
            console.error('Unhandled promise rejection:', event.reason);
            showErrorToast('操作失败: ' + (event.reason?.message || '未知错误'));
        });
    }

    // 工具函数：验证表单数据
    function validateFormData(formData, rules) {
        const errors = [];
        
        for (const [field, rule] of Object.entries(rules)) {
            const value = formData[field];
            
            if (rule.required && (!value || value.trim() === '')) {
                errors.push(rule.message || `${field}不能为空`);
            }
            
            if (rule.pattern && value && !rule.pattern.test(value)) {
                errors.push(rule.patternMessage || `${field}格式不正确`);
            }
        }
        
        if (errors.length > 0) {
            errors.forEach(error => showErrorToast(error));
            return false;
        }
        
        return true;
    }

    // 工具函数：重试机制
    async function retryOperation(operation, maxAttempts = ErrorHandlerConfig.retryAttempts) {
        for (let attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return await operation();
            } catch (error) {
                if (attempt === maxAttempts) {
                    throw error;
                }
                
                console.log(`重试 ${attempt}/${maxAttempts}...`);
                await new Promise(resolve => setTimeout(resolve, ErrorHandlerConfig.retryDelay));
            }
        }
    }

    // 暴露全局API
    window.ErrorHandler = {
        showError: showErrorToast,
        showSuccess: showSuccessToast,
        checkNetwork: checkNetworkConnection,
        handleError: handleAjaxError,
        validateForm: validateFormData,
        retry: retryOperation,
        init: initGlobalErrorHandling
    };

    // 页面加载完成后初始化
    document.addEventListener('DOMContentLoaded', function() {
        initGlobalErrorHandling();
        console.log('全局错误处理已初始化');
    });

})();