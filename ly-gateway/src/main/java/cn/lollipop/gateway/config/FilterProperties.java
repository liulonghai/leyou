package cn.lollipop.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties {
    private Set<String> allowPaths;
}
