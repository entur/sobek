package org.rutebanken.sobek.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelCastConfig {

    @Value("${sobek.hazelcast.service-port:5701}")
    private String hazelcastServicePort;

    @Value("${sobek.hazelcast.service-name:sobek}")
    private String hazelcastServiceName;

    @Value("${sobek.hazelcast.namespace:sobek}")
    private String hazelcastNamespace;

    @Value("${sobek.hazelcast.kubernetes.enabled:false}")
    private boolean kubernetesEnabled;

    @Value("${sobek.hazelcast.cluster.name:sobek}")
    private String hazelcastClusterName;


    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();

        // Set cluster name
        config.setClusterName(hazelcastClusterName);

        // Configure network settings
        config.getNetworkConfig().setPortAutoIncrement(false);

        // Configure Kubernetes discovery
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        joinConfig.getKubernetesConfig().setEnabled(kubernetesEnabled);
        joinConfig.getKubernetesConfig().setProperty("namespace", hazelcastNamespace);
        joinConfig.getKubernetesConfig().setProperty("service-name", hazelcastServiceName);
        joinConfig.getKubernetesConfig().setProperty("service-port", hazelcastServicePort);
        return config;
    }
}
