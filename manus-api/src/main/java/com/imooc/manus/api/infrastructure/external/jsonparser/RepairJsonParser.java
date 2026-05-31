package com.imooc.manus.api.infrastructure.external.jsonparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.manus.api.domain.external.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON修复解析器。
 * <p>
 * 使用正则表达式清理LLM生成的非标准JSON（如Markdown代码块、前导文字等），
 * 然后用Jackson解析。
 * </p>
 * @author zhuang03@qq.com
 * @date 2026-05-31 11:22:37
 */
public class RepairJsonParser implements JsonParser {

    private static final Logger logger = LoggerFactory.getLogger(RepairJsonParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 用于提取Markdown代码块中JSON的正则
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*(\\{[\\s\\S]*?})\\s*```");
    // 用于提取裸JSON对象的正则
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("(\\{[\\s\\S]*})");

    /**
     * 解析文本，修复可能的非标准JSON格式。
     *
     * @param text         要解析的文本（可能包含Markdown标记、非法JSON等）
     * @param defaultValue 解析失败时的默认值（可为null）
     * @return 解析后的对象
     */
    @Override
    public Object invoke(String text, Object defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }

        // 1.尝试直接解析
        try {
            return OBJECT_MAPPER.readValue(text.trim(), Object.class);
        } catch (Exception e) {
            // 继续尝试清理后解析
        }

        // 2.尝试从Markdown代码块中提取JSON
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(text);
        if (codeBlockMatcher.find()) {
            String extracted = codeBlockMatcher.group(1);
            try {
                return OBJECT_MAPPER.readValue(extracted.trim(), Object.class);
            } catch (Exception e) {
                // 继续尝试
            }
        }

        // 3.尝试提取裸JSON对象
        Matcher jsonMatcher = JSON_OBJECT_PATTERN.matcher(text);
        if (jsonMatcher.find()) {
            String extracted = jsonMatcher.group(1);
            try {
                return OBJECT_MAPPER.readValue(extracted.trim(), Object.class);
            } catch (Exception e) {
                // 继续尝试
            }
        }

        // 4.尝试清理并解析
        String cleaned = cleanJson(text);
        try {
            return OBJECT_MAPPER.readValue(cleaned, Object.class);
        } catch (Exception e) {
            logger.error("修复JSON解析器无法解析: {}, 使用默认值", text.substring(0, Math.min(100, text.length())));
            return defaultValue;
        }
    }

    /**
     * 解析文本为指定类型的对象
     *
     * @param text  要解析的文本
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 解析后的对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T invoke(String text, Class<T> clazz) {
        Object result = invoke(text, null);
        if (result == null) return null;
        try {
            return OBJECT_MAPPER.convertValue(result, clazz);
        } catch (Exception e) {
            logger.error("类型转换失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 清理JSON文本中的常见问题
     *
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String cleanJson(String text) {
        // 1.去除Markdown代码块标记
        String cleaned = text.replaceAll("```(?:json)?\\s*", "").replaceAll("```", "");

        // 2.去除首尾空白
        cleaned = cleaned.trim();

        // 3.尝试修复常见问题
        // 去除尾部逗号
        cleaned = cleaned.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");

        return cleaned;
    }
}

