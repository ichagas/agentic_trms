package com.trms.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ChatModel Provider Configuration for TRMS AI Backend
 *
 * Enables runtime selection between different AI providers (Ollama, OpenAI)
 * based on the app.ai-provider configuration property.
 *
 * Usage:
 * - Default (Ollama): mvn spring-boot:run
 * - OpenAI: AI_PROVIDER=openai OPENAI_API_KEY=sk-xxx mvn spring-boot:run
 */
@Configuration
public class ChatModelConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ChatModelConfiguration.class);

    @Value("${app.ai-provider:ollama}")
    private String aiProvider;

    /**
     * Primary ChatModel bean that selects the appropriate provider based on configuration.
     *
     * @param ollamaChatModel Auto-configured Ollama ChatModel (optional)
     * @param openAiChatModel Auto-configured OpenAI ChatModel (optional)
     * @return The selected ChatModel based on app.ai-provider property
     * @throws IllegalStateException if selected provider is not configured
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(
            @Autowired(required = false) @Qualifier("ollamaChatModel") ChatModel ollamaChatModel,
            @Autowired(required = false) @Qualifier("openAiChatModel") ChatModel openAiChatModel) {

        logger.info("Configuring primary ChatModel with provider: {}", aiProvider);

        if ("openai".equalsIgnoreCase(aiProvider)) {
            if (openAiChatModel != null) {
                logger.info("✓ Using OpenAI ChatModel as primary AI provider");
                logger.debug("OpenAI provider successfully configured and available");
                return openAiChatModel;
            } else {
                String errorMsg = "OpenAI provider selected but OpenAI ChatModel bean is not available. " +
                                "Please ensure OPENAI_API_KEY is set and spring-ai-openai-spring-boot-starter is on the classpath.";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
        } else if ("ollama".equalsIgnoreCase(aiProvider)) {
            if (ollamaChatModel != null) {
                logger.info("✓ Using Ollama ChatModel as primary AI provider");
                logger.debug("Ollama provider successfully configured and available");
                return ollamaChatModel;
            } else {
                String errorMsg = "Ollama provider selected but Ollama ChatModel bean is not available. " +
                                "Please ensure Ollama is running at the configured base-url and spring-ai-ollama-spring-boot-starter is on the classpath.";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
        } else {
            String errorMsg = String.format("Unknown AI provider: '%s'. Valid options are: 'ollama', 'openai'", aiProvider);
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }
}
