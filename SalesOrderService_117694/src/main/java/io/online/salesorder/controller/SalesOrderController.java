package io.online.salesorder.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.online.salesorder.domain.Item;
import io.online.salesorder.domain.SalesOrderDetails;
import io.online.salesorder.entity.OrderLineItem;
import io.online.salesorder.entity.SalesOrder;
import io.online.salesorder.repository.OrderLineItemRepository;
import io.online.salesorder.repository.SalesOrderRepository;

@RestController
public class SalesOrderController {

	@Autowired
	SalesOrderRepository salesOrderRepository;

	@Autowired
	OrderLineItemRepository orderLineItemRepository;

	@PostMapping("/orders")
	public ResponseEntity<?> insertSalesOrderDetails(@RequestBody SalesOrderDetails salesOrderDetails) {
		System.out.println("Inside Controller getOrderDesc >>>>" + salesOrderDetails.getOrderDesc());

		//Validate customer by verifying the table “customer_sos” with cust_id --- Starts -- TO DO

		//Validate customer by verifying the table “customer_sos” with cust_id --- Ends -- TO DO

		// REST call to validate items by calling item service with item name -- itemByName ---Starts 
		System.out.println("Before REST CALL >>>>");		
		RestTemplate restTemplate = new RestTemplate();
		String fetchItemUrl = "http://localhost:8082/items";
		ResponseEntity<Item> response = null;

		String itemName = "";
		double totalPrice = 0.0;
		Long orderId = 0L;
		List <Item> fetchedItemList = null;
		
		if (salesOrderDetails.getItemNameList() != null && salesOrderDetails.getItemNameList().size() >0 ) {
			fetchedItemList = new ArrayList<Item>();
			Item item = null;
			for (int i=0 ; i < salesOrderDetails.getItemNameList().size() ; i++) {
				item = new Item();
				itemName = salesOrderDetails.getItemNameList().get(i).getItemName();
				item.setItemName(itemName);
				response = restTemplate.getForEntity(fetchItemUrl + "/"+itemName, Item.class);
				System.out.println("After REST CALL >>>>"+response.getBody());
				itemName = response.getBody().getItemName();
				totalPrice = totalPrice + response.getBody().getItemPrice();
				fetchedItemList.add(item);
				if(itemName.equals(null) || itemName.equals("")){
					//Item details not available -- Exception to be thrown
				}
			}
		} else {
			//Item details not available -- Exception to be thrown
		}

		// REST call to validate items by calling item service with item name -- itemByName --- Ends

		// create order by inserting the order details in sales_order table --- Starts
		if(salesOrderDetails != null ) {

			SalesOrder salesOrder = new SalesOrder();
			if (salesOrderDetails.getOrderDate() != null ) {
				salesOrder.setOrderDate(salesOrderDetails.getOrderDate());
			}
			if (salesOrderDetails.getCustId() != null ) {
				salesOrder.setCustId(salesOrderDetails.getCustId() );
			}
			if (salesOrderDetails.getOrderDesc() != null ) {
				salesOrder.setOrderDesc(salesOrderDetails.getOrderDesc() );
			}
			if (totalPrice != 0.0 ) {
				salesOrder.setTotalPrice(totalPrice);
			}

			salesOrder = salesOrderRepository.save(salesOrder);
			orderId = salesOrder.getOrderId();
			// create order by inserting the order details in sales_order table --- Ends

			assert orderId != null;

			// create order line by inserting the order details in order_line_item table --- Starts
			if (fetchedItemList != null && fetchedItemList.size() >0 ) {
				for (int i=0 ; i < fetchedItemList.size() ; i++) {
					if (fetchedItemList.get(i).getItemName() != null && fetchedItemList.get(i).getItemName() != ""){
						OrderLineItem orderLineItem = new OrderLineItem();
						orderLineItem.setItemName(fetchedItemList.get(i).getItemName());
						orderLineItem.setItemQuantity(fetchedItemList.size());// Check
						orderLineItem.setOrderId(orderId);
		
						orderLineItemRepository.save(orderLineItem);
						
					} else {
						//Item details not available -- Exception to be thrown
					}
				}
			}
			// create order line by inserting the order details in order_line_item table --- Ends
		}
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setLocation(ServletUriComponentsBuilder
				.fromCurrentRequest().path("/" + orderId)
				.buildAndExpand().toUri());

		return new ResponseEntity<>(orderId, httpHeaders, HttpStatus.CREATED);
	}
}
