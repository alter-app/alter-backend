package com.dreamteam.alter.adapter.outbound.posting.persistence.readonly;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostingListForMapMarkerResponse {

    private Long id;
    private BigDecimal latitude;
    private BigDecimal longitude;

}
