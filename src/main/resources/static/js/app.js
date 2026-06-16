/**
 * 合同管理系统 - 全局JS
 * Contract Management System
 */

// ========================================
// 通用工具函数
// ========================================

/**
 * 显示提示消息
 */
function showMessage(message, type) {
    type = type || 'info';
    var alertClass = 'alert-' + type;
    var alertDiv = document.createElement('div');
    alertDiv.className = 'alert ' + alertClass;
    alertDiv.textContent = message;

    var container = document.querySelector('.main-content') || document.querySelector('.auth-card') || document.body;
    container.insertBefore(alertDiv, container.firstChild);

    setTimeout(function() {
        alertDiv.style.transition = 'opacity 0.3s';
        alertDiv.style.opacity = '0';
        setTimeout(function() {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 300);
    }, 3000);
}

/**
 * 使用fetch API提交表单
 */
function submitForm(url, data, callback) {
    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(result) {
        if (callback) {
            callback(result);
        }
    })
    .catch(function(error) {
        console.error('请求失败:', error);
        showMessage('操作失败，请稍后重试', 'danger');
    });
}

/**
 * 使用fetch API提交带文件的表单
 */
function submitFormWithFile(url, formData, callback) {
    fetch(url, {
        method: 'POST',
        body: formData
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(result) {
        if (callback) {
            callback(result);
        }
    })
    .catch(function(error) {
        console.error('请求失败:', error);
        showMessage('操作失败，请稍后重试', 'danger');
    });
}

/**
 * 使用fetch API发送GET请求
 */
function fetchData(url, callback) {
    fetch(url)
    .then(function(response) {
        return response.json();
    })
    .then(function(result) {
        if (callback) {
            callback(result);
        }
    })
    .catch(function(error) {
        console.error('请求失败:', error);
        showMessage('加载数据失败，请稍后重试', 'danger');
    });
}

/**
 * 使用fetch API发送DELETE请求
 */
function deleteData(url, callback) {
    if (!confirm('确定要删除吗？此操作不可恢复。')) {
        return;
    }
    fetch(url, {
        method: 'DELETE'
    })
    .then(function(response) {
        return response.json();
    })
    .then(function(result) {
        if (callback) {
            callback(result);
        }
    })
    .catch(function(error) {
        console.error('删除失败:', error);
        showMessage('删除失败，请稍后重试', 'danger');
    });
}

// ========================================
// 日期快捷操作
// ========================================

/**
 * 日期加减操作
 */
function adjustDate(inputId, years, months) {
    var input = document.getElementById(inputId);
    if (!input || !input.value) {
        input.value = new Date().toISOString().split('T')[0];
    }
    var date = new Date(input.value);
    date.setFullYear(date.getFullYear() + years);
    date.setMonth(date.getMonth() + months);
    input.value = date.toISOString().split('T')[0];
}

/**
 * 格式化日期
 */
function formatDate(dateStr) {
    if (!dateStr) return '';
    var date = new Date(dateStr);
    var y = date.getFullYear();
    var m = ('0' + (date.getMonth() + 1)).slice(-2);
    var d = ('0' + date.getDate()).slice(-2);
    return y + '-' + m + '-' + d;
}

// ========================================
// 表格工具
// ========================================

/**
 * 获取合同状态标签HTML
 */
function getContractStateBadge(state) {
    var badges = {
        '起草': 'badge-draft',
        '会签': 'badge-countersign',
        '定稿': 'badge-finalize',
        '审批': 'badge-approve',
        '签订': 'badge-sign',
        '已完成': 'badge-completed',
        '已否决': 'badge-rejected',
        '待审核': 'badge-pending',
        '已通过': 'badge-approved'
    };
    var cls = badges[state] || 'badge-draft';
    return '<span class="badge ' + cls + '">' + state + '</span>';
}

/**
 * 获取流程类型名称
 */
function getProcessTypeName(type) {
    var names = {1: '会签', 2: '审批', 3: '签订'};
    return names[type] || '未知';
}

/**
 * 获取流程状态名称
 */
function getProcessStateName(state) {
    var names = {0: '未完成', 1: '已完成', 2: '已否决'};
    return names[state] || '未知';
}

/**
 * 获取用户状态名称
 */
function getUserStatusName(status) {
    var names = {0: '待审核', 1: '已通过', 2: '已拒绝'};
    return names[status] || '未知';
}

// ========================================
// 全选/取消全选
// ========================================

/**
 * 全选/取消全选
 */
function toggleSelectAll(checkboxName, selectAllCheckbox) {
    var checkboxes = document.querySelectorAll('input[name="' + checkboxName + '"]');
    for (var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = selectAllCheckbox.checked;
    }
}

/**
 * 检查全选状态
 */
function checkSelectAll(checkboxName, selectAllId) {
    var checkboxes = document.querySelectorAll('input[name="' + checkboxName + '"]');
    var allChecked = true;
    for (var i = 0; i < checkboxes.length; i++) {
        if (!checkboxes[i].checked) {
            allChecked = false;
            break;
        }
    }
    var selectAll = document.getElementById(selectAllId);
    if (selectAll) {
        selectAll.checked = allChecked;
    }
}

// ========================================
// 文件上传
// ========================================

/**
 * 文件上传预览
 */
function handleFileSelect(input, previewId) {
    var preview = document.getElementById(previewId);
    if (!preview) return;

    if (input.files && input.files[0]) {
        var file = input.files[0];
        var allowedTypes = ['application/pdf', 'application/msword',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'image/jpeg', 'image/png', 'image/bmp', 'image/gif'];

        if (allowedTypes.indexOf(file.type) === -1) {
            showMessage('不支持的文件格式，请上传doc/jpg/jpeg/png/bmp/gif/pdf格式文件', 'danger');
            input.value = '';
            return;
        }

        var sizeMB = (file.size / 1024 / 1024).toFixed(2);
        preview.innerHTML = '<i class="fas fa-file"></i> ' + file.name + ' (' + sizeMB + ' MB)';
        preview.classList.add('file-info');
    }
}

// ========================================
// 模态框
// ========================================

/**
 * 打开模态框
 */
function openModal(modalId) {
    var modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'flex';
    }
}

/**
 * 关闭模态框
 */
function closeModal(modalId) {
    var modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// 点击遮罩关闭模态框
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.style.display = 'none';
    }
});

// ========================================
// 页面加载完成后执行
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    // 初始化日期输入框默认值
    var dateInputs = document.querySelectorAll('input[type="date"]');
    dateInputs.forEach(function(input) {
        if (!input.value && input.hasAttribute('data-default-today')) {
            input.value = new Date().toISOString().split('T')[0];
        }
    });
});
