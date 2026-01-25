


$(document).ready(function() {

	APP_CONFIG.userId = "<sec:authentication property='principal.username'/>";
    APP_CONFIG.role = "ROLE_OWNER";
	console.log("현재 설정:", APP_CONFIG);


    // 노쇼 버튼 클릭 시 이벤트
    $(".noshow-btn").on("click", function() {
        const pay_id = $(this).data("payid");   // 버튼에 심어둔 pay_id 가져오기
        const form = $(this).closest("form");   // 부모 폼 요소

        if (confirm("노쇼 처리하시겠습니까? 결제된 금액이 환불됩니다.")) {
            // 1. 환불 함수 호출
            cancelPay(pay_id, form);
            // 취소 누르면 그냥 끝
        }
        
    });
    
    
    $(".confirm-btn").on("click", function() {
        const pay_id = $(this).data("payid");   // 버튼에 심어둔 pay_id 가져오기
        const form = $(this).closest("form");   // 부모 폼 요소

        if (confirm("식사완료 처리하시겠습니까? 결제된 금액이 환불됩니다.")) {
            // 1. 환불 함수 호출
            cancelPay(pay_id, form);
            // 취소 누르면 그냥 끝
        }
        
    });
});
    // 식사완료, 노쇼시의 환불 로직 JAVASCRIPT
function cancelPay(pay_id, form) {	// pay_id 를 매개변수로 가져와서
    $.ajax({
        "url": APP_CONFIG.contextPath+"/pay/api/v1/payment/refund",
        "type": "POST",
        "contentType": "application/json",
        "data": JSON.stringify({
            "pay_id" : pay_id, // Controller에 변수로 보낸다.
        }),
        //  post 수행하기위해 토큰만 주입
        beforeSend: function(xhr) {
        	console.log("AJAX 발송 직전 헤더 설정 시도 중...");
    console.log("설정할 토큰 값:", APP_CONFIG.csrfToken);
    
    if (!APP_CONFIG.csrfToken) {
        console.error("보낼 토큰이 없습니다! APP_CONFIG를 확인하세요.");
    }
        
            xhr.setRequestHeader("X-CSRF-TOKEN", APP_CONFIG.csrfToken);
        },
        success: function () {	// response를 괄호에 넣어서 controller에서 값을 가져올 수 있음
            alert("환불 성공");
            
            // 폼 안에 hidden을 만들어서 값을 넣어줌
		    $('<input>').attr({
		        type: 'hidden',
		        name: 'status', 
		        value: 'NOSHOW'  
		    }).appendTo(form);
            form.submit();	// book status noshow form submit
            // 필요하면 페이지 새로고침
            // location.reload();
        },

        error: function (xhr, status, error) {
            alert("환불 실패");
            console.log(APP_CONFIG.csrfToken);
            console.error(xhr.responseText);
        }	
    });
}
    
