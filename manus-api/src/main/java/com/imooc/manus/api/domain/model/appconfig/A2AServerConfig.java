package com.imooc.manus.api.domain.model.appconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * A2A服务配置。
 * 对应Python中的 A2AServerConfig Pydantic BaseModel。
 *
 * @author thezehui@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class A2AServerConfig {

    /** 唯一标识 */
    @JsonProperty("id")
    private String id = UUID.randomUUID().toString();

    /** 服务基础URL */
    @JsonProperty("base_url")
    private String baseUrl;

    /** 服务是否开启 */
    @JsonProperty("enabled")
    private boolean enabled = true;

    public A2AServerConfig() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

