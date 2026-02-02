$(document).ready(function() {
    // 사용자 취소 이벤트
    $(".user-cancel-btn").on("click", function() {
        const pay_id = $(this).data("payid");   // 환불을위한 결제 ID 가져오기
        const form = $(this).closest("form");   // 부모 폼 요소
		
    
        if (confirm("예약 취소 처리하시겠습니까? 결제된 금액이 환불됩니다.")) {
            // 1. 환불 함수 호출
            cancelPay(pay_id, form);
            // 취소 누르면 그냥 끝
        }
        
    });
});
    // 사용자의 예약 취소시의 환불 로직 JAVASCRIPT
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
            xhr.setRequestHeader("X-CSRF-TOKEN", APP_CONFIG.csrfToken);
        },
        success: function () {	// response를 괄호에 넣어서 controller에서 값을 가져올 수 있음
            alert("환불 성공");
            
            // 폼 안에 hidden을 만들어서 값을 넣어줌
		    $('<input>').attr({
		        type: 'hidden',
		        name: 'status', 
		        value: 'CANCELED'  
		    }).appendTo(form);
            form.submit();	// book status cancel form submit
            // 필요하면 페이지 새로고침
            // location.reload();
        },

        error: function (xhr, status, error) {
            alert("환불 실패");
            console.error(xhr.responseText);
        }	
    });
}

    
function cancelWait(waitId) {
  if (!confirm("웨이팅을 취소하시겠습니까?")) return;

  const url = APP_CONFIG.contextPath + "/wait/cancel";
  console.log("CANCEL URL =", url, "waitId =", waitId);

  fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      "X-CSRF-TOKEN": APP_CONFIG.csrfToken
    },
    body: new URLSearchParams({ wait_id: String(waitId) })
  })
  .then(res => {
    console.log("cancel status =", res.status);
    if (!res.ok) throw new Error("취소 실패: " + res.status);
    alert("웨이팅이 취소되었습니다!");
    location.reload();
  })
  .catch(err => {
    console.error(err);
    alert(err.message);
  });
}

