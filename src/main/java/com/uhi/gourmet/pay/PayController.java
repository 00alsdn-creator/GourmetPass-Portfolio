package com.uhi.gourmet.pay;

import com.siot.IamportRestClient.exception.IamportResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * Handles requests for the application home page.
 */
@RestController
@RequestMapping("/pay")
public class PayController {


    @Autowired
    private PaymentService service;




    /* V1 가격이 일치하는지 확인 */
//	@PostMapping("/api/v1/payment/complete")
//	public ResponseEntity<?> paymentVal(@RequestParam String impUid, String apiKey, String apiSecret) throws IamportResponseException, IOException{
//		System.out.println("Controller paymentVal()..................");
//		
//		if(service.paymentVal(impUid)) {
//			int payId = service.getPayIdByImpUid(impUid);	// impUid로 payId를 가져옴
//			return ResponseEntity.ok(payId);		// 가격이 같을경우 js로 payId보냄
//		} else {
//			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);	// 가격이 다를경우
//		}
//		
//	}

    // 가격이 일치하는지 확인
    @PostMapping("/api/v2/payment/complete")
    public ResponseEntity<?> paymentVal(@RequestBody Map<String, String> body) throws IamportResponseException, IOException {
        System.out.println("Controller paymentVal()..............");
        String paymentId = body.get("paymentId");
        System.out.println("전달받은 paymentId: " + paymentId);

        if (service.paymentVal(paymentId)) {
            int payId = service.getPayIdByImpUid(paymentId);
            return ResponseEntity.ok(payId);        // 가격이 같을경우
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);    // 가격이 다를경우
        }

    }


    /* v1 환불 */
//	@PostMapping(value = "/api/v1/payment/refund", consumes = "application/json")
//	public ResponseEntity<?> refund(@RequestBody Map<String, ?> body) throws IamportResponseException, IOException{
//		
//		System.out.println("Controller refund()..................");
//		System.out.println("pay_id 값 : "+ body.get("pay_id"));		// pay_id의 값
//		PayVO pay_vo;
//		pay_vo = service.getPayById((int) body.get("pay_id"));	// pay_id로 환불할 Pay값 가져와 pay_vo에 넣기
//		
//		if(service.refund(pay_vo.getImp_uid())) {	// pay_vo에 있는 impUid 값으로 환불 Service 실행
//			
//			return ResponseEntity.ok().build();		// 환불 성공
//		} else {
//			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);	// 환불 안될 경우
//		}
//		
//		
//	}	

    @PostMapping(value = "/api/v2/payment/refund", consumes = "application/json")
    public ResponseEntity<?> refund(@RequestBody Map<String, Object> body) throws IamportResponseException, IOException {
        System.out.println("Controller refund()..................");
        System.out.println("pay_id 값 : " + body.get("pay_id"));        // pay_id의 값
        PayVO pay_vo;
        pay_vo = service.getPayById((int) body.get("pay_id"));    // pay_id로 환불할 Pay값 가져와 pay_vo에 넣기

        if (service.refund(pay_vo.getPayment_id())) {    // pay_vo에 있는 payment_id 값으로 환불 Service 실행

            return ResponseEntity.ok().build();           // 환불 성공
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);    // 환불 안될 경우
        }


    }


}
