package com.imooc.manus.api.domain.model.appconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * A2A配置。
 *

 */
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * 类说明。
 * @author zhuang03@qq.com
 * @date 2026-05-30 11:43:43
 */
public class A2AConfig {

    @JsonProperty("a2a_servers")
    private List<A2AServerConfig> a2aServers = new ArrayList<>();

    public A2AConfig() {}

    public List<A2AServerConfig> getA2aServers() { return a2aServers; }
    public void setA2aServers(List<A2AServerConfig> a2aServers) { this.a2aServers = a2aServers; }
}

