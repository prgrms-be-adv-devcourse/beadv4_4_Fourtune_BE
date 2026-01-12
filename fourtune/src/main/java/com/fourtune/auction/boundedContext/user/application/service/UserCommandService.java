package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.port.in.UserCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Command Service (Application Layer)
 * 사용자 명령 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService implements UserCommandUseCase {
    
    // TODO: 의존성 주입
    // private final UserRepository userRepository;
    
    // TODO: Use Case 구현
}

