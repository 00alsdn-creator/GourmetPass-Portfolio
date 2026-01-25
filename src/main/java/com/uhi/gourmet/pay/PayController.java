package com.uhi.gourmet.pay;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.siot.IamportRestClient.exception.IamportResponseException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles requests for the application home page.
 */
@RestController
@RequestMapping("/pay")
public class PayController {

	
	@Autowired
	private PaymentService service;
	

	/* sample */
//	@GetMapping("/rest")
//	public List<UserVO> rest(Locale locale, Model model) {
//		log.info("rest list()..................");
//
//		List<UserVO> list;
//		
//
//		return list = mapper.getUsers();
//	}
	
	
	// 가격이 일치하는지 확인
	@PostMapping("/api/v1/payment/complete")
	public ResponseEntity<?> paymentVal(@RequestParam String impUid, String apiKey, String apiSecret) throws IamportResponseException, IOException{
		System.out.println("Controller paymentVal()..................");
		
		if(service.paymentVal(impUid)) {
			int payId = service.getPayIdByImpUid(impUid);	// impUid로 payId를 가져옴
			return ResponseEntity.ok(payId);		// 가격이 같을경우 js로 payId보냄
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);	// 가격이 다를경우
		}
		
	}
	
	// 환불기능
//	@PostMapping(value = "/api/v1/payment/refund", consumes = "apllication/json")
//	public ResponseEntity<?> refund(@RequestBody Map<String, ?> body) throws IamportResponseException, IOException{
//		log.info("Controller refund()..................");
//		
//		PayVO pay_vo;
//		pay_vo = service.getPayById((int) body.get("pay_id"));	// pay_id로 환불할 Pay값 가져와 pay_vo에 넣기
//		
//		if(service.refund(pay_vo.getImp_uid())) {	// pay_vo에 있는 impUid 값으로 환불 Service 실행
//			return ResponseEntity.ok().build();		// 환불 성공
//		} else {
//			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);	// 환불 안될 경우
//		}
//		
//	}
	
	@PostMapping(value = "/api/v1/payment/refund", consumes = "application/json")
	public ResponseEntity<?> refund(@RequestBody Map<String, ?> body) throws IamportResponseException, IOException{
		
		System.out.println("Controller refund()..................");
		System.out.println("pay_id 값 : "+ body.get("pay_id"));		// pay_id의 값
		PayVO pay_vo;
		pay_vo = service.getPayById((int) body.get("pay_id"));	// pay_id로 환불할 Pay값 가져와 pay_vo에 넣기
		
		if(service.refund(pay_vo.getImp_uid())) {	// pay_vo에 있는 impUid 값으로 환불 Service 실행
			
			return ResponseEntity.ok().build();		// 환불 성공
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);	// 환불 안될 경우
		}
		
		
	}	
	

}
