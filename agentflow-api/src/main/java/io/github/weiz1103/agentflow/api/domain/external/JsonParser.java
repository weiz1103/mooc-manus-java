package io.github.weiz1103.agentflow.api.domain.external;

/**
 * JSON解析器，用于解析json字符串并修复。
 * @author zhuang03@qq.com
 * @date 2026-05-26 13:19:38
 */
public interface JsonParser {

    /**
     * 调用函数，用于将传递进来的文本进行解析并返。
     *
     * @param text         要解析的文本（可能包含Markdown标记、非法JSON等）
     * @param defaultValue 解析失败时的默认值（可为null。
     * @return 解析后的对象（Map、List或基本类型）
     */
    Object invoke(String text, Object defaultValue);

    /**
     * 解析文本为指定类型的对象
     *
     * @param text   要解析的文本
     * @param clazz  目标类型
     * @param <T>    目标类型泛型
     * @return 解析后的对象
     */
    <T> T invoke(String text, Class<T> clazz);
}


