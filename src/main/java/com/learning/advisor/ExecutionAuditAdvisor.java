package com.learning.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * Simple advisor to measure execution time and token usage for both sync and streaming calls.
 */
@Slf4j
@Component
public class ExecutionAuditAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long startTime = System.currentTimeMillis();

        ChatClientResponse response = chain.nextCall(request);

        long latency = System.currentTimeMillis() - startTime;
        Usage usage = response.chatResponse() != null && response.chatResponse().getMetadata() != null
                ? response.chatResponse().getMetadata().getUsage()
                : null;

        log.info("[Audit] Latency: {} ms | Tokens: {}",
                latency,
                usage != null ? usage.getTotalTokens() : 0);

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long startTime = System.currentTimeMillis();

        return chain.nextStream(request)
                .doFinally(signalType -> {
                    long latency = System.currentTimeMillis() - startTime;
                    log.info("[Audit Stream] Finished in {} ms (signal: {})", latency, signalType);
                });
    }

    @Override
    public String getName() {
        return "ExecutionAuditAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}


