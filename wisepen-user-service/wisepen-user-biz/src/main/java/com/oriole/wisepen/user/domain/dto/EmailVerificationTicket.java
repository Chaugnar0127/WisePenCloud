package com.oriole.wisepen.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationTicket implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String email;
    private String emailDomain;
    private String university;
}
