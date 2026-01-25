package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementPolicy;
import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class CollectSettlementItemChunkUseCase {
    // 시연때는 yml에서 0으로 변경
    @Value("${settlement.policy.waiting-days:7}")
    private int waitingDays;

    private final SettlementCandidatedItemRepository settlementCandidatedItemRepository;
    private final SettlementRepository settlementRepository;

    public int collectSettlementItemChunk(int size){
        List<SettlementCandidatedItem> settlementReadyCandidateItems = findCandidatedItemsToFinalize(size);

        if (settlementReadyCandidateItems.isEmpty()){
            return 0;
        }

        // 1. payee 별로 아이템들을 그룹핑
        Map<SettlementUser, List<SettlementCandidatedItem>> itemsByUserMap = new HashMap<>();

        for (SettlementCandidatedItem item : settlementReadyCandidateItems) {
            SettlementUser payeeUser = item.getPayee();

            // 맵에 해당 유저의 리스트가 없으면 새로 만들어서 넣음
            if (!itemsByUserMap.containsKey(payeeUser)) {
                itemsByUserMap.put(payeeUser, new ArrayList<>());
            }

            // 해당 유저 리스트에 아이템 추가
            itemsByUserMap.get(payeeUser).add(item);
        }

        // 2. 그룹핑된 맵을 순회하며 비즈니스 로직 수행
        for (Map.Entry<SettlementUser, List<SettlementCandidatedItem>> entry : itemsByUserMap.entrySet()) {
            SettlementUser payeeUser = entry.getKey();
            List<SettlementCandidatedItem> candidateItems = entry.getValue();

            // 해당 유저의 활성 정산서(Settlement) 조회
            Settlement settlement = findActiveSettlement(payeeUser).orElseThrow(
                    () -> new BusinessException(ErrorCode.SETTLEMENT_ACTIVE_NOT_FOUND)
                    );

            // 아이템 리스트를 순회하며 SettlementItem 생성 및 연결
            for (SettlementCandidatedItem item : candidateItems) {

                // SettlementItem 생성
                SettlementItem settlementItem = settlement.addItem(
                        item.getSettlementEventType(),
                        item.getRelTypeCode(),
                        item.getRelId(),
                        item.getPaymentDate(),
                        item.getPayer(),
                        item.getPayee(),
                        item.getAmount()
                );

                // 정산 후보 아이템에 생성된 정산 아이템 연결
                item.setSettlementItem(settlementItem);
            }
        }

        return settlementReadyCandidateItems.size();
    }

    /**
     * @param size 한번에 조회할 데이터
     * @return 구매 확정시킬 정산 후보를 가져오기
     */
    public List<SettlementCandidatedItem> findCandidatedItemsToFinalize(int size){
        LocalDateTime minDate = LocalDateTime
                .now()
                .minusDays(SettlementPolicy.SETTLEMENT_WAITING_DAYS.getValue());
//                .toLocalDate()
//                .atStartOfDay();
        // 아직 정산과 연결되지 않고, 결제일 =< (현재-구매확정소요일) 구매확정일이 지나거나 된, 정산 후보를 payee와 id 오름차순
        return settlementCandidatedItemRepository
                .findBySettlementItemIsNullAndPaymentDateIsBeforeOrderByPayeeAscIdAsc(
                        minDate,
                        PageRequest.of(0, size)
                );

//        return settlementCandidatedItemRepository
//                .findBySettlementItemIsNullAndPaymentDateIsBeforeOrderByPayeeAscIdAsc(
//                        LocalDateTime.now(),
//                        PageRequest.of(0, size)
//                );

    }


    /**
     * @param payee 정산 받는 사람
     * @return 정산받을 유저의 정산완료 안된 active settlement
     */
    public Optional<Settlement> findActiveSettlement(SettlementUser payee){
        return settlementRepository.findByPayeeAndSettledAtIsNull(payee);
    }

}
