package io.github.weiz1103.agentflow.springai.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.weiz1103.agentflow.springai.domain.port.SandboxPort;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件工具服务，提供文件读写、替换、搜索、查找等能力。
 * <p>
 * 使用 Spring AI {@code @Tool} 注解暴露。LLM 可调用工具，
 * </p>
 *
 * <p><b>扩展点：</b> 继承此类并覆盖对应方法可自定义工具行为，
 * 或直接向 {@link SandboxPort} 注入不同实现以切换底层存储。</p>
 * @author zhuang03@qq.com
 * @date 2026-05-31 13:21:00
 */
public class FileToolService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SandboxPort sandbox;

    public FileToolService(SandboxPort sandbox) {
        this.sandbox = sandbox;
    }

    @Tool(name = "read_file", description = "读取文件内容。用于检查文件内容、分析日志或读取配置文件")
    public String readFile(
            @ToolParam(description = "文件路径") String filepath,
            @ToolParam(description = "起始行号（可选）", required = false) Integer startLine,
            @ToolParam(description = "结束行号（可选）", required = false) Integer endLine,
            @ToolParam(description = "是否使用 sudo 权限", required = false) Boolean sudo,
            @ToolParam(description = "最大读取长度，默认 10000", required = false) Integer maxLength
    ) {
        return serialize(sandbox.readFile(
                filepath, startLine, endLine,
                Boolean.TRUE.equals(sudo),
                maxLength != null ? maxLength : 10000));
    }

    @Tool(name = "write_file", description = "对文件进行覆盖或追加写入。用于创建新文件、追加内容或修改现有文件")
    public String writeFile(
            @ToolParam(description = "文件路径") String filepath,
            @ToolParam(description = "写入内容") String content,
            @ToolParam(description = "是否追加模式", required = false) Boolean append,
            @ToolParam(description = "是否在内容开头添加换行符", required = false) Boolean leadingNewline,
            @ToolParam(description = "是否在内容结尾添加换行符", required = false) Boolean trailingNewline,
            @ToolParam(description = "是否使用 sudo 权限", required = false) Boolean sudo
    ) {
        return serialize(sandbox.writeFile(
                filepath, content,
                Boolean.TRUE.equals(append),
                Boolean.TRUE.equals(leadingNewline),
                Boolean.TRUE.equals(trailingNewline),
                Boolean.TRUE.equals(sudo)));
    }

    @Tool(name = "replace_in_file", description = "在文件中替换指定的字符串。用于更新文件中的特定内容或修复代码中的错误")
    public String replaceInFile(
            @ToolParam(description = "文件路径") String filepath,
            @ToolParam(description = "要替换的旧字符串") String oldStr,
            @ToolParam(description = "替换后的新字符串") String newStr,
            @ToolParam(description = "是否使用 sudo 权限", required = false) Boolean sudo
    ) {
        return serialize(sandbox.replaceInFile(filepath, oldStr, newStr, Boolean.TRUE.equals(sudo)));
    }

    @Tool(name = "search_in_file", description = "在文件内容中搜索匹配的文本。用于查找文件中的特定内容或模式")
    public String searchInFile(
            @ToolParam(description = "文件路径") String filepath,
            @ToolParam(description = "正则表达") String regex,
            @ToolParam(description = "是否使用 sudo 权限", required = false) Boolean sudo
    ) {
        return serialize(sandbox.searchInFile(filepath, regex, Boolean.TRUE.equals(sudo)));
    }

    @Tool(name = "find_files", description = "在指定目录中根据名称模式查找文件。用于定位具有特定命名模式的文件")
    public String findFiles(
            @ToolParam(description = "要搜索的目录路径") String dirPath,
            @ToolParam(description = "Glob 模式，例。*.py") String globPattern
    ) {
        return serialize(sandbox.findFiles(dirPath, globPattern));
    }

    // ---- 工具名称常量（供 ToolCallbackRegistry 引用。----
    public static final String TOOL_READ_FILE       = "read_file";
    public static final String TOOL_WRITE_FILE      = "write_file";
    public static final String TOOL_REPLACE_IN_FILE = "replace_in_file";
    public static final String TOOL_SEARCH_IN_FILE  = "search_in_file";
    public static final String TOOL_FIND_FILES      = "find_files";

    private String serialize(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"序列化失败\"}";
        }
    }
}


