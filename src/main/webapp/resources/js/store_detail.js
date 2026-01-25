/**
 * 고메패스 맛집 상세 페이지 전용 스크립트 [v1.1.7]
 * 리팩토링: 전역 함수 안정화 및 결제 로직 통합
 */

// ================================================================
// [A] 전역 함수 영역
// ================================================================

/**
 * 1. 실시간 예약 가능 시간 슬롯 로드
 */
window.loadAvailableSlots = function() {
    const app = document.getElementById('storeDetailApp');
    if (!app) return;

    const contextPath = app.dataset.context;
    const storeId = app.dataset.storeId;
    const openTime = app.dataset.openTime;
    const closeTime = app.dataset.closeTime;
    const resUnit = parseInt(app.dataset.resUnit) || 30;

    const bookDate = $("#bookDate").val();
    const container = $("#timeSlotContainer");

    if (!bookDate || !container.length) return;

    // 현재 시간 기준 마감 처리
    const now = new Date();
    const todayStr = now.getFullYear() + "-" + String(now.getMonth() + 1).padStart(2, '0') + "-" + String(now.getDate()).padStart(2, '0');
    const bufferTime = new Date(now.getTime() + 10 * 60000); 
    const currentTimeStr = String(bufferTime.getHours()).padStart(2, '0') + ":" + String(bufferTime.getMinutes()).padStart(2, '0');

    container.html("<p class='status-text'>조회 중...</p>");
    $("#selectedTime").val(""); 

    $.ajax({
        url: contextPath + "/store/api/timeSlots",
        type: "GET",
        data: { store_id: storeId, book_date: bookDate },
        dataType: "json",
        success: function(availableList) {
            const allSlots = generateAllSlots(openTime, closeTime, resUnit); 
            let html = "";

            allSlots.forEach(time => {
                const isBooked = !availableList.includes(time);
                const isPast = (bookDate === todayStr && time <= currentTimeStr);

                if (isBooked || isPast) {
                    const reason = isPast ? "마감" : "예약됨";
                    html += `<button type="button" class="time-btn disabled" disabled title="${reason}">${time}</button>`;
                } else {
                    html += `<button type="button" class="time-btn" data-time="${time}">${time}</button>`;
                }
            });
            container.html(html || "<p>영업 시간이 설정되지 않았습니다.</p>");
        },
        error: function() {
            container.html("<p class='error-text'>정보 로드 실패</p>");
        }
    });
};

/**
 * 2. 영업시간 기반 슬롯 배열 생성
 */
window.generateAllSlots = function(open, close, unit) {
    const slots = [];
    let current = open;
    if(!current || !close) return slots;

    while (current <= close) {
        slots.push(current);
        let [h, m] = current.split(':').map(Number);
        m += unit;
        if (m >= 60) { h++; m -= 60; }
        current = String(h).padStart(2, '0') + ":" + String(m).padStart(2, '0');
        if (current > close) break;
    }
    return slots;
};

/**
 * 3. 예약/웨이팅 섹션 전환
 */
window.showInteraction = function(type) {
    $(".interaction-card").hide();
    const target = $("#" + type + "-area");
    if (target.length) {
        target.fadeIn();
        $('html, body').animate({ scrollTop: target.offset().top - 100 }, 500);
    }
    $(".btn-main-wire").removeClass("active");
    $(".btn-" + type).addClass("active");
};


// ================================================================
// [B] 문서 로드 완료 후 실행 영역
// ================================================================

$(document).ready(function() {
    const app = document.getElementById('storeDetailApp');
    if (!app) return;

    // 1. 날짜 초기값 설정
    const now = new Date();
    const today = now.getFullYear() + "-" + String(now.getMonth() + 1).padStart(2, '0') + "-" + String(now.getDate()).padStart(2, '0');
    $("#bookDate").val(today).attr("min", today);

    // 2. 지도 초기화 (Kakao Maps API)
    if (app.dataset.lat && app.dataset.lng) {
        const container = document.getElementById('map');
        if (container) {
            const options = {
                center: new kakao.maps.LatLng(app.dataset.lat, app.dataset.lng),
                level: 3
            };
            const map = new kakao.maps.Map(container, options);
            new kakao.maps.Marker({ position: options.center }).setMap(map);
        }
    }

    // 3. 시간 버튼 클릭 처리
    $(document).on("click", ".time-btn:not([disabled])", function() {
	    $(".time-btn").removeClass("active");
	    $(this).addClass("active");
	    $("#selectedTime").val($(this).data("time")); 
    });

    // 4. 예약 및 결제 프로세스
    $("#bookForm").on("submit", function(e) {
        e.preventDefault();
        const form = this;
        const selectedTime = $("#selectedTime").val();
        if(!selectedTime) { alert("방문 시간을 선택해 주세요!"); return; }

        if(!confirm("예약을 위해 결제를 진행하시겠습니까?")) return;

        // 아임포트 결제 호출
        const IMP = window.IMP;
        IMP.init(window.loginUserInfo.impInit);
        IMP.request_pay({
            pg: window.loginUserInfo.pg,
            pay_method: "card",
            merchant_uid: "pay-" + new Date().getTime(),
            name: "예약 보증금",
            amount: 1,
            buyer_email: window.loginUserInfo.email,
            buyer_name:  window.loginUserInfo.name
        }, function (response) {
            if (response.success) {
                $("#payIdField").val(response.imp_uid);
                alert("결제가 완료되었습니다!");
                form.submit(); 
            } else {
                alert("결제 실패: " + response.error_msg);
            }
        });
    });

    // 5. 초기 슬롯 실행
    window.loadAvailableSlots();
});