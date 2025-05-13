package com.dreamteam.alter.application.posting;

import com.dreamteam.alter.adapter.inbound.general.posting.dto.CreatePostingRequestDto;
import com.dreamteam.alter.domain.posting.entity.Posting;
import com.dreamteam.alter.domain.posting.port.inbound.CreatePostingUseCase;
import com.dreamteam.alter.domain.posting.port.outbound.PostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("createPosting")
@RequiredArgsConstructor
@Transactional
public class CreatePosting implements CreatePostingUseCase {

    private final PostingRepository postingRepository;

    @Override
    public void execute(CreatePostingRequestDto request) {
        Posting posting = Posting.create(request);
        postingRepository.save(posting);
    }

}
