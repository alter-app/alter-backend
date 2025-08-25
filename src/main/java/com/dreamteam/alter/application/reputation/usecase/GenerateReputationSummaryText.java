package com.dreamteam.alter.application.reputation.usecase;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.ReputationSummaryData;
import com.dreamteam.alter.domain.reputation.port.inbound.GenerateReputationSummaryTextUseCase;
import com.dreamteam.alter.domain.reputation.type.ReputationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service("generateReputationSummaryText")
@RequiredArgsConstructor
public class GenerateReputationSummaryText implements GenerateReputationSummaryTextUseCase {

    private final ChatClient chatClient;

    @Override
    public String execute(ReputationSummaryData summaryData) {
        String prompt = buildPrompt(summaryData);

        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }

    private String buildPrompt(ReputationSummaryData summaryData) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("다음 평판 데이터를 바탕으로 간결하고 자연스러운 요약을 생성해주세요.\n\n");

        if (ReputationType.USER.equals(summaryData.getTargetType())) {
            prompt.append("대상: 알바생\n");
            prompt.append("설명: 이 데이터는 알바생에 대한 동료나 고용주의 평판을 나타냅니다.\n\n");
        } else if (ReputationType.WORKSPACE.equals(summaryData.getTargetType())) {
            prompt.append("대상: 알바 업장\n");
            prompt.append("설명: 이 데이터는 알바생의 입장에서 본 알바 업장(고용주)에 대한 평판을 나타냅니다.\n\n");
        }

        prompt.append("총 평판 개수: ")
            .append(summaryData.getTotalReputationCount())
            .append("개\n\n");
        prompt.append("주요 키워드 및 빈도:\n");

        String keywordsText = summaryData.getTopKeywords()
            .stream()
            .map(kf -> {
                return String.format(
                    "- %s (%s): %d회 (%.1f%%)",
                    kf.getKeywordName(),
                    kf.getKeywordDescription(),
                    kf.getCount(),
                    kf.getPercentage()
                );
            })
            .collect(Collectors.joining("\n"));

        prompt.append(keywordsText)
            .append("\n\n");

        boolean hasUserDescriptions = summaryData.getTopKeywords()
            .stream()
            .anyMatch(kf -> kf.getUserDescriptions() != null && !kf.getUserDescriptions()
                .isEmpty());

        if (hasUserDescriptions) {
            prompt.append("평판 설명 예시:\n");
            summaryData.getTopKeywords()
                .stream()
                .filter(kf -> kf.getUserDescriptions() != null && !kf.getUserDescriptions()
                    .isEmpty())
                .forEach(kf -> {
                    prompt.append("- ")
                        .append(kf.getKeywordName())
                        .append(": ");
                    prompt.append(String.join(", ", kf.getUserDescriptions()));
                    prompt.append("\n");
                });
            prompt.append("\n");
        }

        prompt.append("요구사항:\n");
        prompt.append("1. 300자 이내의 간결한 요약\n");
        prompt.append("2. 자연스러운 한국어 문장\n");
        prompt.append("3. 긍정적인 톤으로 작성\n");
        prompt.append("4. 키워드 빈도를 고려한 균형잡힌 표현\n");
        prompt.append("5. 이모지 사용 금지\n");
        prompt.append("6. 알바생/알바 업장 맥락에 맞는 표현 사용\n");
        prompt.append("7. '이 평판은', '이 알바생은', '이 업장은' 등의 접두사 없이 순수한 요약 내용만 작성\n\n");
        prompt.append("요약:");

        return prompt.toString();
    }
}
