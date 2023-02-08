package com.kakashi.orderservice.service;


import com.kakashi.orderservice.dto.InventoryResponse;
import com.kakashi.orderservice.dto.OrderLineItemsDto;
import com.kakashi.orderservice.dto.OrderRequest;
import com.kakashi.orderservice.model.Order;
import com.kakashi.orderservice.model.OrderLineItems;
import com.kakashi.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {


    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(orderLineItems1 -> orderLineItems1.getSkuCode()).toList();

        // Call Inventory service, and place order if product is in stock
        InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory", uriBuilder ->
                        uriBuilder.queryParam(
                                "skuCode", skuCodes
                        ).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();// for making synchronous call

        boolean allProductsInStock = Arrays.stream(inventoryResponsesArray).allMatch(inventoryResponse ->
                inventoryResponse.isInStock());

        if (allProductsInStock) {
            orderRepository.save(order);
            return "Order Placed Successfully" ;
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later");

        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        return orderLineItems;
    }
}
