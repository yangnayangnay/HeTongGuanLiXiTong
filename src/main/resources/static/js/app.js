/**
 * 合同管理系统 - 全局JS
 * Contract Management System
 */

// ========================================
// 通用工具函数
// ========================================

/**
 * 渲染合同内容（支持电子签名图片显示）
 * 将 [电子签名]\n<img ...>\n[/电子签名] 标记渲染为实际图片
 */
function renderContractContent(content) {
    if (!content) return '无';
    // 先提取所有img标签，用占位符替换，避免转义时破坏
    var imgTags = [];
    var result = content.replace(/<img\s+src="([^"]*)"[^>]*>/gi, function(match) {
        var idx = imgTags.length;
        imgTags.push(match);
        return '\n%%IMG_PLACEHOLDER_' + idx + '%%\n';
    });
    // 兼容旧格式：[电子签名]...[/电子签名]
    result = result.replace(/\[电子签名\]\n?([\s\S]*?)\n?\[\/电子签名\]/g, function(match, inner) {
        var idx = imgTags.length;
        imgTags.push(inner.trim());
        return '\n%%IMG_PLACEHOLDER_' + idx + '%%\n';
    });
    // 转义剩余HTML特殊字符
    result = result.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    // 将换行符转为HTML换行
    result = result.replace(/\n/g, '<br>');
    // 还原img标签占位符（这些是安全的，是我们自己提取的）
    for (var i = 0; i < imgTags.length; i++) {
        var tag = imgTags[i];
        // 确保img标签有样式，display:block确保独占一行
        if (tag.indexOf('style=') === -1) {
            tag = tag.replace(/<img/, '<img style="max-height:60px;border:1px solid #ddd;border-radius:4px;padding:2px;margin:4px;display:block;"');
        } else if (tag.indexOf('display:') === -1) {
            tag = tag.replace(/style="/, 'style="display:block;');
        }
        result = result.replace('%%IMG_PLACEHOLDER_' + i + '%%', tag);
    }
    return result;
}

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
 * 存储提示消息到sessionStorage，页面刷新后自动显示
 * 用于操作成功后需要刷新页面的场景：先存储消息，再刷新页面
 */
function storeMessage(message, type) {
    type = type || 'info';
    sessionStorage.setItem('_pending_msg', JSON.stringify({message: message, type: type}));
}

/**
 * 刷新页面并显示提示消息
 * 替代 showMessage + setTimeout + location.reload 的模式
 */
function reloadWithMessage(message, type) {
    storeMessage(message, type);
    location.reload();
}

/**
 * 通知父窗口任务已完成，刷新铃铛数量
 */
function notifyTaskCompleted() {
    if (window.parent && window.parent !== window) {
        window.parent.postMessage({type: 'taskCompleted'}, '*');
    }
}

/**
 * 使用fetch API提交表单
 */
function submitForm(url, data, callback) {
    var btn = document.activeElement;
    var origHtml = null;
    if (btn && btn.tagName === 'BUTTON') {
        origHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 处理中...';
    }
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
        if (btn && origHtml) { btn.disabled = false; btn.innerHTML = origHtml; }
        if (callback) {
            callback(result);
        }
    })
    .catch(function(error) {
        if (btn && origHtml) { btn.disabled = false; btn.innerHTML = origHtml; }
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
        if (!response.ok) {
            throw new Error('HTTP ' + response.status);
        }
        return response.json();
    })
    .then(function(result) {
        if (callback) {
            callback(result);
        }
    })
    .catch(function(error) {
        console.error('请求失败:', error);
        showMessage('加载数据失败: ' + error.message, 'danger');
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
    // 触发change事件，以便进行日期联动校验
    if (typeof input.onchange === 'function') input.onchange();
    else if (input.getAttribute('onchange')) {
        var fn = new Function(input.getAttribute('onchange'));
        fn.call(input);
    }
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

// 点击遮罩不关闭模态框（防止误操作丢失编辑内容）
// 只能通过关闭按钮关闭模态框
document.addEventListener('mousedown', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.preventDefault();
        e.stopPropagation();
    }
}, true);

document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.preventDefault();
        e.stopPropagation();
    }
}, true);

document.addEventListener('mouseup', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.preventDefault();
        e.stopPropagation();
    }
}, true);

// ========================================
// 页面加载完成后执行
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    // 检查是否有待显示的提示消息（页面刷新后恢复）
    try {
        var pendingMsg = sessionStorage.getItem('_pending_msg');
        if (pendingMsg) {
            sessionStorage.removeItem('_pending_msg');
            var msgObj = JSON.parse(pendingMsg);
            showMessage(msgObj.message, msgObj.type);
        }
    } catch(e) {}

    // 初始化日期输入框默认值
    var dateInputs = document.querySelectorAll('input[type="date"]');
    dateInputs.forEach(function(input) {
        if (!input.value && input.hasAttribute('data-default-today')) {
            input.value = new Date().toISOString().split('T')[0];
        }
    });
});
