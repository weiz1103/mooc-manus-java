package io.github.weiz1103.agentflow.api.domain.external;

import io.github.weiz1103.agentflow.api.domain.model.toolresult.ToolResult;

import java.io.InputStream;
import java.util.Optional;

/**
 * 沙箱服务扩展协议，涵盖：文件工具协议、Shell工具协议以及沙箱本身的扩展。
 * <p>
 * 调用沙箱服务的HTTP API接口。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-25 22:35:28
 */
public interface Sandbox {

    /**
     * 根据传递的会话id+目录+命令执行对应的命。
     *
     * @param sessionId Shell会话id
     * @param execDir   执行目录（绝对路径）
     * @param command   要执行的Shell命令
     * @return 工具结果
     */
    ToolResult<Object> execCommand(String sessionId, String execDir, String command);

    /**
     * 根据传递的会话id+是否返回控制台记录获取shell结果
     *
     * @param sessionId Shell会话id
     * @param console   是否返回控制台记。
     * @return 工具结果
     */
    ToolResult<Object> readShellOutput(String sessionId, boolean console);

    /**
     * 根据传递的会话id+秒数等待程序执行
     *
     * @param sessionId Shell会话id
     * @param seconds   等待秒数（可为null。
     * @return 工具结果
     */
    ToolResult<Object> waitProcess(String sessionId, Integer seconds);

    /**
     * 根据传递会话id+文本内容+是否回车键写入内容到进程。
     *
     * @param sessionId  Shell会话id
     * @param inputText  文本内容
     * @param pressEnter 是否回车。
     * @return 工具结果
     */
    ToolResult<Object> writeShellInput(String sessionId, String inputText, boolean pressEnter);

    /**
     * 根据传递的会话id杀死对应的进程
     *
     * @param sessionId Shell会话id
     * @return 工具结果
     */
    ToolResult<Object> killProcess(String sessionId);

    /**
     * 根据传递的文件路径+写入内容+追加模式+前后内容换行+超级权限写入对应的文。
     *
     * @param filepath        文件路径
     * @param content         写入内容
     * @param append          是否使用追加模式
     * @param leadingNewline  是否在内容开头添加换行符
     * @param trailingNewline 是否在内容结尾添加换行符
     * @param sudo            是否使用sudo权限
     * @return 工具结果
     */
    ToolResult<Object> writeFile(String filepath, String content, boolean append,
                                 boolean leadingNewline, boolean trailingNewline, boolean sudo);

    /**
     * 根据传递的文件路径+起始终点行数+超级权限读取对应的文件内。
     *
     * @param filepath  文件路径
     * @param startLine 起始行（可为null。
     * @param endLine   终点行（可为null。
     * @param sudo      是否使用sudo权限
     * @param maxLength 读取文件内容的最大长。
     * @return 工具结果
     */
    ToolResult<Object> readFile(String filepath, Integer startLine, Integer endLine,
                                boolean sudo, int maxLength);

    /**
     * 根据传递的文件路径判断文件是否存在
     *
     * @param filepath 文件路径
     * @return 工具结果
     */
    ToolResult<Object> checkFileExists(String filepath);

    /**
     * 根据传递的文件路径删除指定文件
     *
     * @param filepath 文件路径
     * @return 工具结果
     */
    ToolResult<Object> deleteFile(String filepath);

    /**
     * 根据传递的文件夹路径列出该路径下的所有文。
     *
     * @param dirPath 文件夹路。
     * @return 工具结果
     */
    ToolResult<Object> listFiles(String dirPath);

    /**
     * 根据传递文件路。新旧内容+超级权限完成文件内容替换
     *
     * @param filepath 文件路径
     * @param oldStr   旧内。
     * @param newStr   新内。
     * @param sudo     是否使用sudo权限
     * @return 工具结果
     */
    ToolResult<Object> replaceInFile(String filepath, String oldStr, String newStr, boolean sudo);

    /**
     * 根据传递的文件路径+正则+超级权限完成文件内容检。
     *
     * @param filepath 文件路径
     * @param regex    正则表达式模。
     * @param sudo     是否使用sudo权限
     * @return 工具结果
     */
    ToolResult<Object> searchInFile(String filepath, String regex, boolean sudo);

    /**
     * 根据传递的文件夹路。匹配规则查找文件
     *
     * @param dirPath     文件夹路。
     * @param globPattern 匹配规则
     * @return 工具结果
     */
    ToolResult<Object> findFiles(String dirPath, String globPattern);

    /**
     * 根据文件源数。路径/文件名将文件上传至沙箱中
     *
     * @param fileData 文件数据。
     * @param filepath 目标路径
     * @param filename 文件名（可为null。
     * @return 工具结果
     */
    ToolResult<Object> uploadFile(InputStream fileData, String filepath, String filename);

    /**
     * 根据传递的文件路径下载沙箱中的文件
     *
     * @param filepath 文件路径
     * @return 文件数据。
     */
    InputStream downloadFile(String filepath);

    /**
     * 确保当前沙箱存在，如果不存在会创。
     */
    void ensureSandbox();

    /**
     * 销毁当前沙箱实。
     *
     * @return 是否销毁成。
     */
    boolean destroy();

    /**
     * 获取沙箱中的浏览器实。
     *
     * @return 浏览器实。
     */
    Browser getBrowser();

    /**
     * 只读属性，返回沙箱的id
     *
     * @return 沙箱id
     */
    String getId();

    /**
     * 只读属性，返回沙箱的CDP链接（操控浏览器的）
     *
     * @return CDP链接
     */
    String getCdpUrl();

    /**
     * 只读属性，获取沙箱的vnc链接（远程桌面链接）
     *
     * @return VNC链接
     */
    String getVncUrl();

    /**
     * 类方法，用于快速创建一个沙。
     *
     * @return 新创建的沙箱实例
     */
    static Sandbox create() {
        throw new UnsupportedOperationException("子类必须实现create()方法");
    }

    /**
     * 类方法，根据传递的id获取沙箱实例
     *
     * @param id 沙箱id
     * @return 沙箱实例（Optional。
     */
    static Optional<Sandbox> get(String id) {
        throw new UnsupportedOperationException("子类必须实现get()方法");
    }
}


