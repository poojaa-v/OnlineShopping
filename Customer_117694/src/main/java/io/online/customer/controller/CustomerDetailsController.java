package io.online.customer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import io.online.customer.domain.Customer;
import io.online.customer.repository.CustomerDetailsRepository;

@RestController
public class CustomerDetailsController {

	@Autowired
	private CustomerDetailsRepository customerDetailsRepository;
	
	@Autowired
	private RabbitTemplate template;
	
		
	//	@Autowired
	//	public CustomerDetailsController (CustomerDetailsRepository customerDetailsRepository) {
	//		this.customerDetailsRepository = customerDetailsRepository;		
	//	}

	@GetMapping("/customers")
	public List<Customer> getCustomerDetails() {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<Inside getCustomerDetails Controller>>>>>>>>>>>>>>>>>>>>>");
		return customerDetailsRepository.getCustomerDetails();	        
	}
	
	@PostMapping("/customer")
    public ResponseEntity<?> add(@RequestBody Customer customer) {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<Inside getCustomerDetails Controller ADDD>>>>>>>>>>>>>>>>>>>>>");
		Customer cust = customerDetailsRepository.saveCustDetails(customer);
		
		//Rabbit MQ event creation starts
		String routingKey = "CustomerCreated";
		String message = "CustomerCreated";
		// rabbitTemplate.convertAndSend(exchange.getName(), routingKey, message);
		System.out.println("<<<<<<<<<Inside Service Save function BEFORE Convert and send>>>>>>>>>>");
		template.convertAndSend(cust);
		System.out.println("<<<<<<<<<Inside Service Save function AFTER Convert and send>>>>>>>>>>");
		//Rabbit MQ event creation ends
		
		HttpHeaders httpHeaders = null;
        if(cust!= null){
	        httpHeaders = new HttpHeaders();
	        httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/" + cust.getId()).buildAndExpand().toUri());
        }
        return new ResponseEntity<>(cust, httpHeaders, HttpStatus.CREATED);
    }

}
