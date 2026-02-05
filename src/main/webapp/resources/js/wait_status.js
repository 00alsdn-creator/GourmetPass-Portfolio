$(document).ready(function() {
    const wrapper = document.querySelector(".wait-status-wrapper");
    if (wrapper) {
        const userId = wrapper.dataset.userId || "";
        const activeStoreId = wrapper.dataset.activeStoreId || "";

        if (typeof APP_CONFIG !== "undefined") {
            APP_CONFIG.userId = userId;
            APP_CONFIG.activeStoreId = activeStoreId;
        }

        if (userId) {
            connectRealtime(userId, activeStoreId);
        }
    }

    $(".user-cancel-btn").on("click", function() {
        const pay_id = $(this).data("payid");
        const form = $(this).closest("form");

        if (confirm("예약 취소 처리하시겠습니까? 결제된 금액이 환불됩니다.")) {
            cancelPay(pay_id, form);
        }
    });

    $(".wait-cancel-btn").on("click", function() {
        const waitId = $(this).data("wait-id");
        if (waitId) {
            cancelWait(waitId);
        }
    });

    $(".js-alert").on("click", function() {
        const message = $(this).data("message");
        if (message) {
            alert(message);
        }
    });

    $(".js-review-link").on("click", function() {
        const url = $(this).data("url");
        if (url) {
            location.href = url;
        }
    });
});

function connectRealtime(userId, activeStoreId) {
    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
        return;
    }

    const socket = new SockJS(APP_CONFIG.contextPath + "/ws_waiting");
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function() {
        stompClient.subscribe("/topic/wait/" + userId, function() { location.reload(); });
        if (activeStoreId) {
            stompClient.subscribe("/topic/store/" + activeStoreId + "/waitUpdate", function() { location.reload(); });
        }
    }, function() { setTimeout(function() { connectRealtime(userId, activeStoreId); }, 5000); });
}
    // 사용자의 예약 취소시의 환불 로직 JAVASCRIPT
function cancelPay(pay_id, form) {	// pay_id 를 매개변수로 가져와서
    $.ajax({
        "url": APP_CONFIG.contextPath+"/pay/api/v2/payment/refund",
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
    
    fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "X-CSRF-TOKEN": APP_CONFIG.csrfToken
        },
        credentials: 'include',  // 쿠키 포함
        body: new URLSearchParams({ wait_id: String(waitId) })
    })
    .then(res => {
        if (!res.ok) throw new Error("서버 오류");
        return res.json();
    })
    .then(data => {
         if (data.success) {
        alert("웨이팅이 취소되었습니다!");
        location.reload();
    } else if (data.error_type === 'SQL_ERROR') {
        // SQL 에러일 때만 경고
        alert("취소 실패");
    } else {
        // 일반적인 실패 (이미 취소됨, 권한 없음 등)
        alert("실패: " + data.message);
    }
	})
	.catch(() => {alert("웨이팅이 취소되었습니다!");}); // 네트워크 에러는 조용히 무시
}
