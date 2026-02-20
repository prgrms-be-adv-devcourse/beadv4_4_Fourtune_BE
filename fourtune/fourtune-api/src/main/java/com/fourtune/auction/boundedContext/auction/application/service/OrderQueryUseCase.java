package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.boundedContext.auction.mapper.OrderMapper;
import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.auction.dto.OrderDetailResponse;
import com.fourtune.shared.auction.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 주문 조회 UseCase
 * - 주문 상세 조회
 * - 사용자별 주문 목록 조회
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryUseCase {

    private final OrderSupport orderSupport;
    private final AuctionSupport auctionSupport;
    private final UserFacade userFacade;
    /**
     * 주문번호로 주문 조회
     */
    public OrderDetailResponse getOrderByOrderId(String orderId) {
        Order order = orderSupport.findByOrderIdOrThrow(orderId);
        AuctionItem auctionItem = auctionSupport.findById(order.getAuctionId()).orElse(null);
        var userIds = Stream.of(order.getWinnerId(), order.getSellerId()).collect(Collectors.toSet());
        var nicknames = userFacade.getNicknamesByIds(userIds);
        return OrderMapper.fromDetail(order, auctionItem,
                nicknames.get(order.getWinnerId()), nicknames.get(order.getSellerId()));
    }

    /**
     * 주문 ID(PK)로 주문 조회
     */
    public OrderDetailResponse getOrderById(Long id) {
        Order order = orderSupport.findByIdOrThrow(id);
        AuctionItem auctionItem = auctionSupport.findById(order.getAuctionId()).orElse(null);
        var userIds = Stream.of(order.getWinnerId(), order.getSellerId()).collect(Collectors.toSet());
        var nicknames = userFacade.getNicknamesByIds(userIds);
        return OrderMapper.fromDetail(order, auctionItem,
                nicknames.get(order.getWinnerId()), nicknames.get(order.getSellerId()));
    }

    /**
     * 사용자의 주문 목록 조회 (구매자)
     */
    public List<OrderResponse> getUserOrders(Long winnerId) {
        List<Order> orders = orderSupport.findByWinnerId(winnerId);
        var nicknames = userFacade.getNicknamesByIds(Set.of(winnerId));
        String winnerNickname = nicknames.get(winnerId);
        return orders.stream()
                .map(order -> {
                    AuctionItem auctionItem = auctionSupport.findById(order.getAuctionId()).orElse(null);
                    return OrderMapper.from(order, auctionItem, winnerNickname);
                })
                .toList();
    }

    /**
     * 경매의 주문 조회
     */
    public OrderDetailResponse getOrderByAuctionId(Long auctionId) {
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        Optional<Order> orderOpt = orderSupport.findByAuctionId(auctionId);
        if (orderOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        Order order = orderOpt.get();
        var userIds = Stream.of(order.getWinnerId(), order.getSellerId()).collect(Collectors.toSet());
        var nicknames = userFacade.getNicknamesByIds(userIds);
        return OrderMapper.fromDetail(order, auctionItem,
                nicknames.get(order.getWinnerId()), nicknames.get(order.getSellerId()));
    }

    /**
     * 주문 존재 여부 확인
     */
    public boolean existsByAuctionId(Long auctionId) {
        return orderSupport.existsByAuctionId(auctionId);
    }

}
